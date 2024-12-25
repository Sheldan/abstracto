package dev.sheldan.abstracto.giveaway.service;

import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.CounterService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureDefinition;
import dev.sheldan.abstracto.giveaway.config.GiveawayMode;
import dev.sheldan.abstracto.giveaway.config.GiveawayPostTarget;
import dev.sheldan.abstracto.giveaway.exception.GiveawayKeyNotFoundException;
import dev.sheldan.abstracto.giveaway.exception.GiveawayNotFoundException;
import dev.sheldan.abstracto.giveaway.model.GiveawayCreationRequest;
import dev.sheldan.abstracto.giveaway.model.JoinGiveawayPayload;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import dev.sheldan.abstracto.giveaway.model.database.GiveawayKey;
import dev.sheldan.abstracto.giveaway.model.database.GiveawayParticipant;
import dev.sheldan.abstracto.giveaway.model.template.GiveawayMessageModel;
import dev.sheldan.abstracto.giveaway.model.template.GiveawayResultMessageModel;
import dev.sheldan.abstracto.giveaway.model.template.GiveawayWinnerNotificationMessageModel;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayKeyManagementService;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayManagementService;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayParticipantManagementService;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class GiveawayServiceBean implements GiveawayService {

    private static final String GIVEAWAY_MESSAGE_TEMPLATE_KEY = "giveaway_post";
    private static final String GIVEAWAY_WINNER_MODMAIL_NOTIFICATION = "giveaway_winner_modmail_notification";
    private static final String GIVEAWAY_RESULT_MESSAGE_TEMPLATE_KEY = "giveaway_result";
    public static final String GIVEAWAY_JOIN_ORIGIN = "JOIN_GIVEAWAY";

    public static final String GIVEAWAY_COUNTER = "giveaways";

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private GiveawayManagementService giveawayManagementService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private GiveawayParticipantManagementService giveawayParticipantManagementService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private CounterService counterService;

    @Autowired
    private GiveawayKeyManagementService giveawayKeyManagementService;

    @Autowired
    private UserService userService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired(required = false)
    private ModMailThreadService modMailThreadService;

    @Autowired
    private GiveawayServiceBean self;

    @Override
    public CompletableFuture<Long> createGiveaway(GiveawayCreationRequest giveawayCreationRequest) {
        String componentId = componentService.generateComponentId();
        Instant targetDate = Instant.now().plus(giveawayCreationRequest.getDuration());
        Long serverId = giveawayCreationRequest.getServerId();
        Long giveawayId = counterService.getNextCounterValue(serverId, GIVEAWAY_COUNTER);
        GiveawayMessageModel model = GiveawayMessageModel
                .builder()
                .title(giveawayCreationRequest.getTitle())
                .description(giveawayCreationRequest.getDescription())
                .giveawayId(giveawayId)
                .benefactor(giveawayCreationRequest.getBenefactorId() != null ? MemberDisplay.fromIds(giveawayCreationRequest.getServerId(), giveawayCreationRequest.getBenefactorId()) : null)
                .creator(MemberDisplay.fromIds(giveawayCreationRequest.getServerId(), giveawayCreationRequest.getCreatorId()))
                .winnerCount(giveawayCreationRequest.getWinnerCount())
                .targetDate(targetDate)
                .joinComponentId(componentId)
                .build();
        List<CompletableFuture<Message>> messageFutures;
        log.info("Rendering giveaway message in server {} by user {}", serverId, giveawayCreationRequest.getCreatorId());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(GIVEAWAY_MESSAGE_TEMPLATE_KEY, model, serverId);
        if(giveawayCreationRequest.getTargetChannel() == null) {
            log.info("Sending giveaway to post target in server {}", serverId);
            postTargetService.validatePostTarget(GiveawayPostTarget.GIVEAWAYS, serverId);
            messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, GiveawayPostTarget.GIVEAWAYS, serverId);
        } else {
            log.info("Sending giveaway to channel {} in server {}.", giveawayCreationRequest.getTargetChannel().getId(), serverId);
            messageFutures = channelService.sendMessageToSendToChannel(messageToSend, giveawayCreationRequest.getTargetChannel());
        }
        CompletableFutureList<Message> messageFutureList = new CompletableFutureList<>(messageFutures);
        return messageFutureList.getMainFuture().thenApply(o -> {
            Message createdMessage = messageFutureList.getFutures().get(0).join();
            giveawayCreationRequest.setTargetChannel(createdMessage.getGuildChannel());
            self.persistGiveaway(giveawayCreationRequest, giveawayId, createdMessage.getIdLong(), componentId);
            return giveawayId;
        });
    }

    @Override
    public CompletableFuture<Void> addGiveawayParticipant(Giveaway giveaway, Member member, MessageChannel messageChannel) {
        GiveawayMessageModel giveawayMessageModel = GiveawayMessageModel.fromGiveaway(giveaway);
        giveawayMessageModel.setJoinedUserCount(giveaway.getParticipants().size() + 1L);
        Long giveawayId = giveaway.getGiveawayId().getId();
        log.info("Adding giveaway participating of user {} to giveaway {} in server {}.", member.getIdLong(), giveawayId, member.getGuild().getIdLong());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(GIVEAWAY_MESSAGE_TEMPLATE_KEY, giveawayMessageModel, member.getGuild().getIdLong());
        return channelService.editEmbedMessageInAChannel(messageToSend.getEmbeds().get(0), messageChannel, giveaway.getMessageId())
                .thenAccept(message -> {
            self.persistAddedParticipant(member, giveawayId);
        });
    }

    @Override
    @Transactional
    public CompletableFuture<Void> evaluateGiveaway(Long giveawayId, Long serverId) {
        Giveaway giveaway = giveawayManagementService.loadGiveawayById(giveawayId, serverId).orElseThrow(GiveawayNotFoundException::new);
        log.info("Evaluating giveaway {} in server {}.", giveawayId, serverId);
        Set<Long> winnerUserInServerIds = new HashSet<>();
        Integer winnerCount = giveaway.getWinnerCount();
        giveaway.getParticipants().forEach(giveawayParticipant -> giveawayParticipant.setWon(false));
        List<Long> potentialWinners = new ArrayList<>(giveaway
                .getParticipants()
                .stream()
                .map(giveawayParticipant -> giveawayParticipant.getParticipant().getUserInServerId())
                .toList());

        if(potentialWinners.size() <= winnerCount) {
            winnerUserInServerIds.addAll(potentialWinners);
            log.debug("Less participants than total winners - selecting all for giveaway {} in server {}.", giveawayId, serverId);
        } else {
            for (int i = 0; i < winnerCount; i++) {
                int winnerIndex = secureRandom.nextInt(potentialWinners.size());
                Long winner = potentialWinners.get(winnerIndex);
                potentialWinners.remove(winnerIndex);
                winnerUserInServerIds.add(winner);
            }
        }
        List<GiveawayParticipant> winningParticipants = giveaway
                .getParticipants()
                .stream()
                .filter(giveawayParticipant -> winnerUserInServerIds.contains(giveawayParticipant.getParticipant().getUserInServerId()))
                .toList();
        winningParticipants.forEach(giveawayParticipant -> giveawayParticipant.setWon(true));
        List<MemberDisplay> winnerDisplays = winningParticipants
                .stream()
                .map(giveawayParticipant -> MemberDisplay.fromAUserInAServer(giveawayParticipant.getParticipant()))
                .toList();
        GiveawayResultMessageModel resultModel = GiveawayResultMessageModel
                .builder()
                .messageId(giveaway.getMessageId())
                .title(giveaway.getTitle())
                .winners(winnerDisplays)
                .build();
        log.info("Sending result message for giveaway {} in server {}.", giveawayId, serverId);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(GIVEAWAY_RESULT_MESSAGE_TEMPLATE_KEY, resultModel, serverId);
        List<CompletableFuture<Message>> resultFutures = channelService.sendMessageEmbedToSendToAChannel(messageToSend, giveaway.getGiveawayChannel());
        long actualWinnerCount = winnerUserInServerIds.size();
        Long winnerUserId;
        if(giveaway.getGiveawayKey() != null && !winningParticipants.isEmpty()) {
            GiveawayParticipant winnerParticipant = winningParticipants.get(0);
            GiveawayKey giveawayKey = giveaway.getGiveawayKey();
            giveawayKey.setWinner(winnerParticipant.getParticipant());
            giveawayKey.setUsed(true);
            winnerUserId = winnerParticipant.getParticipant().getUserReference().getId();
        } else {
            winnerUserId = null;
        }
        GiveawayMessageModel giveawayMessageModel = GiveawayMessageModel.fromGiveaway(giveaway);
        giveawayMessageModel.setWinners(winnerDisplays);
        giveawayMessageModel.setEnded(true);
        boolean createGiveawayKeyNotification = giveaway.getGiveawayKey() != null
            && featureModeService.featureModeActive(GiveawayFeatureDefinition.GIVEAWAY, serverId, GiveawayMode.AUTO_NOTIFY_GIVEAWAY_KEY_WINNERS)
            && featureModeService.featureModeActive(GiveawayFeatureDefinition.GIVEAWAY, serverId, GiveawayMode.KEY_GIVEAWAYS)
            && actualWinnerCount > 0
            && modMailThreadService != null;
        MessageToSend giveawayMessageToSend = templateService.renderEmbedTemplate(GIVEAWAY_MESSAGE_TEMPLATE_KEY, giveawayMessageModel, serverId);
        log.info("Updating original giveaway message for giveaway {} in server {}.", giveawayId, serverId);
        GuildMessageChannel messageChannel = channelService.getMessageChannelFromServer(giveaway.getServer().getId(), giveaway.getGiveawayChannel().getId());
        CompletableFuture<Message> giveawayUpdateFuture = channelService.editMessageInAChannelFuture(giveawayMessageToSend, messageChannel, giveaway.getMessageId());
        resultFutures.add(giveawayUpdateFuture);
        return new CompletableFutureList<>(resultFutures).getMainFuture().thenCompose(unused -> {
            if(createGiveawayKeyNotification) {
                return userService.retrieveUserForId(winnerUserId)
                    .thenCompose(user -> self.handleKeyGiveawayNotifications(giveawayId, serverId, winnerUserInServerIds.iterator().next(), user))
                    .exceptionally(throwable -> {
                        log.error("Failed to notify winner of giveaway {} in server {}.", giveawayId, serverId, throwable);
                        return null;
                    });
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Transactional
    public CompletableFuture<Void> handleKeyGiveawayNotifications(Long giveawayId, Long serverId, Long winnerInServerId, User user) {
        if(modMailThreadService == null) {
            log.info("Modmail service not available - skipping notifications about giveaway {} in server {}.", giveawayId, serverId);
            return CompletableFuture.completedFuture(null);
        }
        Giveaway giveaway = giveawayManagementService.loadGiveawayById(giveawayId, serverId).orElseThrow(GiveawayNotFoundException::new);
        GiveawayWinnerNotificationMessageModel messageModel = GiveawayWinnerNotificationMessageModel.fromGiveaway(giveaway, giveaway.getGiveawayKey().getKey());
        AUserInAServer winner = userInServerManagementService.loadOrCreateUser(winnerInServerId);
        MessageToSend giveawayWinnerNotification = templateService.renderEmbedTemplate(GIVEAWAY_WINNER_MODMAIL_NOTIFICATION, messageModel, serverId);
        return modMailThreadService.sendMessageToUser(winner, giveawayWinnerNotification, user);
    }

    @Override
    public CompletableFuture<Void> cancelGiveaway(Long giveawayId, Long serverId) {
        Optional<Giveaway> giveAwayOptional = giveawayManagementService.loadGiveawayById(giveawayId, serverId);
        if(giveAwayOptional.isEmpty()) {
            throw new GiveawayNotFoundException();
        }
        Giveaway giveaway = giveAwayOptional.get();
        log.info("Cancelling giveaway with id {} in server {}.", giveawayId, serverId);
        GiveawayMessageModel giveawayMessageModel = GiveawayMessageModel.fromGiveaway(giveaway);
        giveawayMessageModel.setCancelled(true);
        schedulerService.stopTrigger(giveaway.getReminderTriggerKey());
        MessageToSend giveawayMessageToSend = templateService.renderEmbedTemplate(GIVEAWAY_MESSAGE_TEMPLATE_KEY, giveawayMessageModel, serverId);

        GuildMessageChannel messageChannel = channelService.getMessageChannelFromServer(giveaway.getServer().getId(), giveaway.getGiveawayChannel().getId());
        log.debug("Updating original giveaway message to consider cancellation for giveaway {} in server {}.", giveawayId, serverId);
        return channelService.editMessageInAChannelFuture(giveawayMessageToSend, messageChannel, giveaway.getMessageId())
        .thenAccept(message -> {
            self.persistGiveawayCancellation(giveawayId, serverId);
        });
    }

    @Transactional
    public void persistGiveawayCancellation(Long giveawayId, Long serverId) {
        Optional<Giveaway> giveAwayOptional = giveawayManagementService.loadGiveawayById(giveawayId, serverId);
        if(giveAwayOptional.isEmpty()) {
            throw new GiveawayNotFoundException();
        }
        log.info("Persisting cancellation of giveaway {} in server {}.", giveawayId, serverId);
        Giveaway giveaway = giveAwayOptional.get();
        giveaway.setCancelled(true);
    }

    @Transactional
    public void persistAddedParticipant(Member member, Long giveawayId) {
        log.info("Storing user {} as participant to giveaway {} in server {}.", member.getIdLong(), giveawayId, member.getGuild().getIdLong());
        Optional<Giveaway> giveAwayOptional = giveawayManagementService.loadGiveawayById(giveawayId, member.getGuild().getIdLong());
        giveAwayOptional.ifPresent(giveaway -> {
            AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
            giveawayParticipantManagementService.addParticipant(giveaway, aUserInAServer);
        });
    }

    @Transactional
    public void persistGiveaway(GiveawayCreationRequest giveawayCreationRequest, Long giveawayId, Long messageId, String componentId) {
        log.info("Persisting giveaway in server {} with message id {}.", giveawayCreationRequest.getServerId(), messageId);
        Instant targetDate = Instant.now().plus(giveawayCreationRequest.getDuration());
        AChannel targetChannel = channelManagementService.loadChannel(giveawayCreationRequest.getTargetChannel().getIdLong());
        AUserInAServer creator = userInServerManagementService.loadOrCreateUser(giveawayCreationRequest.getServerId(), giveawayCreationRequest.getCreatorId());
        AUserInAServer benefactor;
        if(giveawayCreationRequest.getBenefactorId() != null) {
            benefactor = userInServerManagementService.loadOrCreateUser(giveawayCreationRequest.getServerId(), giveawayCreationRequest.getBenefactorId());
        } else {
            benefactor = null;
        }

        Giveaway giveaway = giveawayManagementService.createGiveaway(creator, benefactor, targetChannel, targetDate,
                giveawayCreationRequest.getTitle(), giveawayCreationRequest.getDescription(), giveawayCreationRequest.getWinnerCount(),
                messageId, componentId, giveawayId);

        if(giveawayCreationRequest.getGiveawayKeyId() != null) {
            GiveawayKey giveawayKey = giveawayKeyManagementService.getById(giveawayCreationRequest.getGiveawayKeyId(), giveawayCreationRequest.getServerId())
                .orElseThrow(GiveawayKeyNotFoundException::new);
            giveawayKey.setGiveaway(giveaway);
            giveawayKeyManagementService.saveGiveawayKey(giveawayKey);
        }
        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("giveawayId", giveaway.getGiveawayId().getId().toString());
        parameters.put("serverId", giveaway.getGiveawayId().getServerId().toString());
        JobParameters jobParameters = JobParameters
                .builder()
                .parameters(parameters)
                .build();
        log.info("Scheduling giveaway reminder for giveaway {} originating from message {} in server {}.", giveaway.getGiveawayId().getId(), messageId, giveawayCreationRequest.getServerId());
        String triggerKey = schedulerService.executeJobWithParametersOnce("giveawayEvaluationJob", "giveaway", jobParameters, Date.from(giveaway.getTargetDate()));
        giveaway.setReminderTriggerKey(triggerKey);
        JoinGiveawayPayload joinPayload = JoinGiveawayPayload
                .builder()
                .giveawayId(giveaway.getGiveawayId().getId())
                .serverId(giveaway.getGiveawayId().getServerId())
                .build();
        componentPayloadService.createButtonPayload(componentId, joinPayload, GIVEAWAY_JOIN_ORIGIN, creator.getServerReference());
    }
}
