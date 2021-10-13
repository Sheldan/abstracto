package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FutureMemberPair;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.WarningDecayFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.mode.WarnDecayMode;
import dev.sheldan.abstracto.moderation.config.feature.mode.WarningMode;
import dev.sheldan.abstracto.moderation.config.posttarget.WarnDecayPostTarget;
import dev.sheldan.abstracto.moderation.config.posttarget.WarningPostTarget;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import dev.sheldan.abstracto.moderation.model.template.command.WarnContext;
import dev.sheldan.abstracto.moderation.model.template.command.WarnNotification;
import dev.sheldan.abstracto.moderation.model.template.job.WarnDecayLogModel;
import dev.sheldan.abstracto.moderation.model.template.job.WarnDecayWarning;
import dev.sheldan.abstracto.moderation.model.template.listener.WarnDecayMemberNotificationModel;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WarnServiceBean implements WarnService {

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private WarnManagementService warnManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private CounterService counterService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private WarnServiceBean self;

    public static final String WARN_LOG_TEMPLATE = "warn_log";
    public static final String WARN_NOTIFICATION_TEMPLATE = "warn_notification";
    public static final String WARNINGS_COUNTER_KEY = "WARNINGS";
    public static final String WARN_DECAY_LOG_TEMPLATE_KEY = "warn_decay_log";
    public static final String WARN_DECAY_NOTIFICATION_TEMPLATE_KEY = "warn_decay_member_notification";

    @Override
    public CompletableFuture<Void> notifyAndLogFullUserWarning(WarnContext context)  {
        AServer server = serverManagementService.loadOrCreate(context.getGuild().getIdLong());
        Long warningId = counterService.getNextCounterValue(server, WARNINGS_COUNTER_KEY);
        context.setWarnId(warningId);
        Member warnedMember = context.getWarnedMember();
        Member warningMember = context.getMember();
        Guild guild = warnedMember.getGuild();
        log.info("User {} is warning {} in server {}", warnedMember.getId(), warningMember.getId(), guild.getIdLong());
        WarnNotification warnNotification = WarnNotification.builder().reason(context.getReason()).warnId(warningId).serverName(guild.getName()).build();
        String warnNotificationMessage = templateService.renderTemplate(WARN_NOTIFICATION_TEMPLATE, warnNotification, server.getId());
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        futures.add(messageService.sendMessageToUser(warnedMember.getUser(), warnNotificationMessage));
        log.debug("Logging warning for server {}.", server.getId());
        MessageToSend message = templateService.renderEmbedTemplate(WARN_LOG_TEMPLATE, context, server.getId());
        futures.addAll(postTargetService.sendEmbedInPostTarget(message, WarningPostTarget.WARN_LOG, context.getGuild().getIdLong()));

        return FutureUtils.toSingleFutureGeneric(futures);
    }

    @Override
    public CompletableFuture<Void> warnUserWithLog(WarnContext context) {
        return notifyAndLogFullUserWarning(context).thenAccept(aVoid ->
            self.persistWarning(context)
        );
    }

    @Transactional
    public void persistWarning(WarnContext context) {
        log.info("Persisting warning {} in server {} for user {} by user {}.",
                context.getWarnId(), context.getGuild().getId(), context.getWarnedMember().getId(), context.getMember().getId());
        AUserInAServer warnedUser = userInServerManagementService.loadOrCreateUser(context.getWarnedMember());
        AUserInAServer warningUser = userInServerManagementService.loadOrCreateUser(context.getMember());
        warnManagementService.createWarning(warnedUser, warningUser, context.getReason(), context.getWarnId());

    }

    @Override
    @Transactional
    public CompletableFuture<Void> decayWarningsForServer(AServer server) {
        Long defaultDays = defaultConfigManagementService.getDefaultConfig(WarningDecayFeatureConfig.DECAY_DAYS_KEY).getLongValue();
        Long days = configService.getLongValue(WarningDecayFeatureConfig.DECAY_DAYS_KEY, server.getId(), defaultDays);
        Instant cutOffDay = Instant.now().minus(days, ChronoUnit.DAYS);
        log.info("Decaying warnings on server {} which are older than {}.", server.getId(), cutOffDay);
        List<Warning> warningsToDecay = warnManagementService.getActiveWarningsInServerOlderThan(server, cutOffDay);
        List<Long> userIds = new ArrayList<>(getUserIdsForWarnings(warningsToDecay));
        List<Long> warningIds = flattenWarnings(warningsToDecay);
        Long serverId = server.getId();
        CompletableFuture<Void> completableFuture;
        if(featureModeService.featureModeActive(ModerationFeatureDefinition.AUTOMATIC_WARN_DECAY, server, WarnDecayMode.AUTOMATIC_WARN_DECAY_LOG)) {
            log.debug("Sending log messages for automatic warn decay in server {}.", server.getId());
            completableFuture = logDecayedWarnings(server, warningsToDecay);
        } else {
            log.debug("Not logging automatic warn decay, because feature {} has its mode {} disabled in server {}.", ModerationFeatureDefinition.AUTOMATIC_WARN_DECAY, WarnDecayMode.AUTOMATIC_WARN_DECAY_LOG, server.getId());
            completableFuture = CompletableFuture.completedFuture(null);
        }
        if(featureModeService.featureModeActive(ModerationFeatureDefinition.AUTOMATIC_WARN_DECAY, server, WarnDecayMode.NOTIFY_MEMBER_WARNING_DECAYS)) {
            CompletableFuture<List<Member>> membersInServerAsync = memberService.getMembersInServerAsync(server.getId(), userIds);
            membersInServerAsync
                    .thenAccept(members -> self.sendMemberNotifications(serverId, warningIds, members, cutOffDay)).exceptionally(throwable -> {
                        log.error("Failed to notify members about warn decays.", throwable);
                        return null;
                    });
            log.info("Notifying members about warn decay in server {}.", server.getId());
        } else {
            log.info("Not notifying members about warn decay in server {}.", server.getId());
        }
        return completableFuture.thenAccept(aVoid ->
            self.decayWarnings(warningIds, serverId)
        );
    }

    @Transactional
    public CompletableFuture<Void> sendMemberNotifications(Long serverId, List<Long> warnIds, List<Member> members, Instant cutOffDay) {
        if(warnIds.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        List<Warning> decayingWarnings = warnManagementService.getWarningsViaId(warnIds, serverId);
        AServer server = decayingWarnings.get(0).getWarnedUser().getServerReference();
        List<CompletableFuture<Message>> notificationFutures = new ArrayList<>();
        Map<Long, Member> userIdToMember = members.stream().collect(Collectors.toMap(ISnowflake::getIdLong, Function.identity()));
        decayingWarnings.forEach(warning -> {
            Long userId = warning.getWarnedUser().getUserReference().getId();
            Long warningId = warning.getWarnId().getId();
            if(userIdToMember.containsKey(userId)) {
                Member memberToSendTo = userIdToMember.get(userId);
                List<Warning> remainingWarnings = warnManagementService.getActiveWarningsFromUserYoungerThan(warning.getWarnedUser(), cutOffDay);
                WarnDecayMemberNotificationModel model =
                        WarnDecayMemberNotificationModel
                                .builder()
                                .warnDate(warning.getWarnDate())
                                .warnReason(warning.getReason())
                                .remainingWarningsCount(remainingWarnings.size())
                                .build();
                MessageToSend messageToSend = templateService.renderEmbedTemplate(WARN_DECAY_NOTIFICATION_TEMPLATE_KEY, model, serverId);
                log.info("Notifying user {} in server {} about decayed warning {}.", userId, serverId, warningId);
                notificationFutures.add(messageService.sendMessageToSendToUser(memberToSendTo.getUser(), messageToSend).exceptionally(throwable -> {
                    log.error("Failed to send warn decay message to user {} in server {} to notify about decay warning {}.", userId, server.getId(), warningId, throwable);
                    return null;
                }));
            } else {
                log.warn("Could not find user {} in server {}. Not notifying about decayed warning {}.", userId, serverId, warningId);
            }
        });
        CompletableFuture<Void> future = new CompletableFuture<>();
        FutureUtils.toSingleFutureGeneric(notificationFutures)
                .whenComplete((unused, throwable) -> future.complete(null))
        .exceptionally(throwable -> {
            future.completeExceptionally(throwable);
            return null;
        });
        return future;
    }

    private List<Long> flattenWarnings(List<Warning> warningsToDecay) {
        List<Long> warningIds = new ArrayList<>();
        warningsToDecay.forEach(warning ->
            warningIds.add(warning.getWarnId().getId())
        );
        return warningIds;
    }

    private Set<Long> getUserIdsForWarnings(List<Warning> warnings) {
        Set<Long> userIds = new HashSet<>();
        warnings.forEach(warning -> userIds.add(warning.getWarnedUser().getUserReference().getId()));
        return userIds;
    }

    @Transactional
    public void decayWarnings(List<Long> warningIds, Long serverId) {
        Instant now = Instant.now();
        log.info("Decaying {} warnings.", warningIds.size());
        warningIds.forEach(warningId -> {
            Optional<Warning> warningOptional = warnManagementService.findByIdOptional(warningId, serverId);
            warningOptional.ifPresent(warning ->
                decayWarning(warning, now)
            );
            if(!warningOptional.isPresent()) {
                log.warn("Warning with id {} in server {} not found. Was not decayed.", warningId, serverId);
            }
        });
    }

    @Override
    public void decayWarning(Warning warning, Instant now) {
        log.debug("Decaying warning {} in server {} with date {}.", warning.getWarnId().getId(), warning.getWarnId().getServerId(), now);
        warning.setDecayDate(now);
        warning.setDecayed(true);
    }

    private CompletableFuture<Void> logDecayedWarnings(AServer server, List<Warning> warningsToDecay) {
        log.debug("Loading members decaying {} warnings in server {}.", warningsToDecay.size(), server.getId());
        HashMap<ServerSpecificId, FutureMemberPair> warningMembers = new HashMap<>();
        List<CompletableFuture<Member>> allFutures = new ArrayList<>();
        Long serverId = server.getId();
        warningsToDecay.forEach(warning -> {
            CompletableFuture<Member> warningMember = memberService.getMemberInServerAsync(warning.getWarningUser());
            CompletableFuture<Member> warnedMember = memberService.getMemberInServerAsync(warning.getWarnedUser());
            FutureMemberPair futurePair = FutureMemberPair
                    .builder()
                    .firstMember(warningMember)
                    .secondMember(warnedMember)
                    .firstUser(ServerUser.fromAUserInAServer(warning.getWarningUser()))
                    .secondUser(ServerUser.fromAUserInAServer(warning.getWarnedUser()))
                    .build();
            warningMembers.put(warning.getWarnId(), futurePair);
            allFutures.add(warningMember);
            allFutures.add(warnedMember);
        });
        CompletableFuture<Void> sendingFuture = new CompletableFuture<>();
        FutureUtils.toSingleFutureGeneric(allFutures).handle((aVoid, throwable) -> {
            self.renderAndSendWarnDecayLogs(serverId, warningMembers).thenAccept(aVoid1 ->
               sendingFuture.complete(null)
            ).exceptionally(throwable1 -> {
                sendingFuture.completeExceptionally(throwable1);
                return null;
            });
            return null;
        }).exceptionally(throwable -> {
            sendingFuture.completeExceptionally(throwable);
            return null;
        });

        return sendingFuture;

    }

    @Transactional
    public CompletionStage<Void> renderAndSendWarnDecayLogs(Long serverId, Map<ServerSpecificId, FutureMemberPair> warningMembers) {
        AServer server = serverManagementService.loadServer(serverId);
        List<WarnDecayWarning> warnDecayWarnings = new ArrayList<>();
        warningMembers.keySet().forEach(serverSpecificId -> {
            try {
                Warning warning = warnManagementService.findById(serverSpecificId.getId(), serverSpecificId.getServerId());
                FutureMemberPair pair = warningMembers.get(serverSpecificId);
                Member warningMember = !pair.getFirstMember().isCompletedExceptionally() ? pair.getFirstMember().join() : null;
                Member warnedMember = !pair.getSecondMember().isCompletedExceptionally() ? pair.getSecondMember().join() : null;
                WarnDecayWarning warnDecayWarning = WarnDecayWarning
                        .builder()
                        .warningMember(warningMember)
                        .warningUser(pair.getFirstUser())
                        .warnedMember(warnedMember)
                        .warnedUser(pair.getSecondUser())
                        .warning(warning)
                        .build();
                warnDecayWarnings.add(warnDecayWarning);
            } catch (Exception ex) {
                log.error("exception.", ex);
            }
        });
        WarnDecayLogModel warnDecayLogModel = WarnDecayLogModel
                .builder()
                .guild(guildService.getGuildById(server.getId()))
                .warnings(warnDecayWarnings)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(WARN_DECAY_LOG_TEMPLATE_KEY, warnDecayLogModel, serverId);
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, WarnDecayPostTarget.DECAY_LOG, server.getId());
        return FutureUtils.toSingleFutureGeneric(messageFutures);
    }

    @Override
    public CompletableFuture<Void> decayAllWarningsForServer(AServer server) {
        List<Warning> warningsToDecay = warnManagementService.getActiveWarningsInServerOlderThan(server, Instant.now());
        List<Long> warnIds = flattenWarnings(warningsToDecay);
        log.info("Decaying ALL warning in server {}.", server.getId());
        Long serverId = server.getId();
        if(featureModeService.featureModeActive(ModerationFeatureDefinition.WARNING, server, WarningMode.WARN_DECAY_LOG)) {
            log.debug("Logging warn decays in server {}", serverId);
            return logDecayedWarnings(server, warningsToDecay).thenAccept(aVoid ->
                self.decayWarnings(warnIds, serverId)
            );
        } else {
            log.debug("Not logging warn decays for manual decay in server {} because feature {} with feature mode: {}", serverId, ModerationFeatureDefinition.WARNING, WarningMode.WARN_DECAY_LOG);
            self.decayWarnings(warnIds, serverId);
            return CompletableFuture.completedFuture(null);
        }
    }
}
