package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FutureMemberPair;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.features.WarnDecayMode;
import dev.sheldan.abstracto.moderation.config.features.WarningDecayFeature;
import dev.sheldan.abstracto.moderation.config.features.WarningMode;
import dev.sheldan.abstracto.moderation.config.posttargets.WarnDecayPostTarget;
import dev.sheldan.abstracto.moderation.config.posttargets.WarningPostTarget;
import dev.sheldan.abstracto.moderation.models.template.job.WarnDecayLogModel;
import dev.sheldan.abstracto.moderation.models.template.job.WarnDecayWarning;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnContext;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnNotification;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
    private BotService botService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private CounterService counterService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private WarnServiceBean self;

    public static final String WARN_LOG_TEMPLATE = "warn_log";
    public static final String WARN_NOTIFICATION_TEMPLATE = "warn_notification";
    public static final String WARNINGS_COUNTER_KEY = "WARNINGS";
    public static final String WARN_DECAY_LOG_TEMPLATE_KEY = "warn_decay_log";

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
        String warnNotificationMessage = templateService.renderTemplate(WARN_NOTIFICATION_TEMPLATE, warnNotification);
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        futures.add(messageService.sendMessageToUser(warnedMember.getUser(), warnNotificationMessage));
        if(featureModeService.featureModeActive(ModerationFeatures.WARNING, server.getId(), WarningMode.WARN_LOG)) {
            log.trace("Logging warning for server {}.", server.getId());
            MessageToSend message = templateService.renderEmbedTemplate(WARN_LOG_TEMPLATE, context);
            futures.addAll(postTargetService.sendEmbedInPostTarget(message, WarningPostTarget.WARN_LOG, context.getGuild().getIdLong()));
        } else {
            log.trace("Not logging warning because of feature {} with feature mode {} in server {}.", ModerationFeatures.WARNING, WarningMode.WARN_LOG, server.getId());
        }

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
        AUserInAServer warnedUser = userInServerManagementService.loadUser(context.getWarnedMember());
        AUserInAServer warningUser = userInServerManagementService.loadUser(context.getMember());
        warnManagementService.createWarning(warnedUser, warningUser, context.getReason(), context.getWarnId());

    }

    @Override
    @Transactional
    public CompletableFuture<Void>  decayWarningsForServer(AServer server) {
        Long days = configService.getLongValue(WarningDecayFeature.DECAY_DAYS_KEY, server.getId());
        Instant cutOffDay = Instant.now().minus(days, ChronoUnit.DAYS);
        log.info("Decaying warnings on server {} which are older than {}.", server.getId(), cutOffDay);
        List<Warning> warningsToDecay = warnManagementService.getActiveWarningsInServerOlderThan(server, cutOffDay);
        List<Long> warningIds = flattenWarnings(warningsToDecay);
        Long serverId = server.getId();
        CompletableFuture<Void> completableFuture;
        if(featureModeService.featureModeActive(ModerationFeatures.AUTOMATIC_WARN_DECAY, server, WarnDecayMode.AUTOMATIC_WARN_DECAY_LOG)) {
            log.trace("Sending log messages for automatic warn decay in server {}.", server.getId());
            completableFuture = logDecayedWarnings(server, warningsToDecay);
        } else {
            log.trace("Not logging automatic warn decay, because feature {} has its mode {} disabled in server {}.", ModerationFeatures.AUTOMATIC_WARN_DECAY, WarnDecayMode.AUTOMATIC_WARN_DECAY_LOG, server.getId());
            completableFuture = CompletableFuture.completedFuture(null);
        }
        return completableFuture.thenAccept(aVoid ->
            self.decayWarnings(warningIds, serverId)
        );
    }

    private List<Long> flattenWarnings(List<Warning> warningsToDecay) {
        List<Long> warningIds = new ArrayList<>();
        warningsToDecay.forEach(warning ->
            warningIds.add(warning.getWarnId().getId())
        );
        return warningIds;
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
        log.trace("Decaying warning {} in server {} with date {}.", warning.getWarnId().getId(), warning.getWarnId().getServerId(), now);
        warning.setDecayDate(now);
        warning.setDecayed(true);
    }

    private CompletableFuture<Void> logDecayedWarnings(AServer server, List<Warning> warningsToDecay) {
        log.trace("Loading members decaying {} warnings in server {}.", warningsToDecay.size(), server.getId());
        HashMap<ServerSpecificId, FutureMemberPair> warningMembers = new HashMap<>();
        List<CompletableFuture<Member>> allFutures = new ArrayList<>();
        Long serverId = server.getId();
        warningsToDecay.forEach(warning -> {
            CompletableFuture<Member> warningMember = botService.getMemberInServerAsync(warning.getWarningUser());
            CompletableFuture<Member> warnedMember = botService.getMemberInServerAsync(warning.getWarnedUser());
            FutureMemberPair futurePair = FutureMemberPair.builder().firstMember(warningMember).secondMember(warnedMember).build();
            warningMembers.put(warning.getWarnId(), futurePair);
        });
        CompletableFuture<Void> sendingFuture = new CompletableFuture<>();
        FutureUtils.toSingleFutureGeneric(allFutures).handle((aVoid, throwable) -> {
            self.renderAndSendWarnDecayLogs(serverId, warningMembers).thenAccept(aVoid1 ->
               sendingFuture.complete(null)
            );
            return null;
        });

        return sendingFuture;

    }

    @Transactional
    public CompletionStage<Void> renderAndSendWarnDecayLogs(Long serverId, Map<ServerSpecificId, FutureMemberPair> warningMembers) {
        AServer server = serverManagementService.loadServer(serverId);
        List<WarnDecayWarning> warnDecayWarnings = new ArrayList<>();
        warningMembers.keySet().forEach(serverSpecificId -> {
            Warning warning = warnManagementService.findById(serverSpecificId.getId(), serverSpecificId.getServerId());
            FutureMemberPair pair = warningMembers.get(serverSpecificId);
            // TODO add ids to render in case any member left the server
            WarnDecayWarning warnDecayWarning = WarnDecayWarning
                    .builder()
                    .warningMember(pair.getFirstMember().join())
                    .warnedMember(pair.getSecondMember().join())
                    .warning(warning)
                    .build();
            warnDecayWarnings.add(warnDecayWarning);
        });
        WarnDecayLogModel warnDecayLogModel = WarnDecayLogModel
                .builder()
                .guild(botService.getGuildById(server.getId()))
                .server(server)
                .warnings(warnDecayWarnings)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(WARN_DECAY_LOG_TEMPLATE_KEY, warnDecayLogModel);
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, WarnDecayPostTarget.DECAY_LOG, server.getId());
        return FutureUtils.toSingleFutureGeneric(messageFutures);
    }

    @Override
    public CompletableFuture<Void> decayAllWarningsForServer(AServer server) {
        List<Warning> warningsToDecay = warnManagementService.getActiveWarningsInServerOlderThan(server, Instant.now());
        List<Long> warnIds = flattenWarnings(warningsToDecay);
        log.info("Decaying ALL warning in server {}.", server.getId());
        Long serverId = server.getId();
        if(featureModeService.featureModeActive(ModerationFeatures.WARNING, server, WarningMode.WARN_DECAY_LOG)) {
            log.trace("Logging warn decays in server {}", serverId);
            return logDecayedWarnings(server, warningsToDecay).thenAccept(aVoid ->
                self.decayWarnings(warnIds, serverId)
            );
        } else {
            log.trace("Not logging warn decays for manual decay in server {} because feature {} with feature mode: {}", serverId, ModerationFeatures.WARNING, WarningMode.WARN_DECAY_LOG);
            decayWarnings(warnIds, serverId);
            return CompletableFuture.completedFuture(null);
        }
    }
}
