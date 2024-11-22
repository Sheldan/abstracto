package dev.sheldan.abstracto.giveaway.service;

import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.CounterService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.giveaway.config.GiveawayPostTarget;
import dev.sheldan.abstracto.giveaway.exception.GiveawayNotFoundException;
import dev.sheldan.abstracto.giveaway.model.GiveawayCreationRequest;
import dev.sheldan.abstracto.giveaway.model.JoinGiveawayPayload;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import dev.sheldan.abstracto.giveaway.model.database.GiveawayParticipant;
import dev.sheldan.abstracto.giveaway.model.template.GiveawayMessageModel;
import dev.sheldan.abstracto.giveaway.model.template.GiveawayResultMessageModel;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayManagementService;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayParticipantManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
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
    private GiveawayServiceBean self;

    @Override
    public CompletableFuture<Void> createGiveaway(GiveawayCreationRequest giveawayCreationRequest) {
        String componentId = componentService.generateComponentId();
        Instant targetDate = Instant.now().plus(giveawayCreationRequest.getDuration());
        Long serverId = giveawayCreationRequest.getCreator().getGuild().getIdLong();
        Long giveawayId = counterService.getNextCounterValue(serverId, GIVEAWAY_COUNTER);
        GiveawayMessageModel model = GiveawayMessageModel
                .builder()
                .title(giveawayCreationRequest.getTitle())
                .description(giveawayCreationRequest.getDescription())
                .giveawayId(giveawayId)
                .benefactor(giveawayCreationRequest.getBenefactor() != null ? MemberDisplay.fromMember(giveawayCreationRequest.getBenefactor()) : null)
                .creator(MemberDisplay.fromMember(giveawayCreationRequest.getCreator()))
                .winnerCount(giveawayCreationRequest.getWinnerCount())
                .targetDate(targetDate)
                .joinComponentId(componentId)
                .build();
        List<CompletableFuture<Message>> messageFutures;
        log.info("Rendering giveaway message in server {} by user {}", serverId, giveawayCreationRequest.getCreator().getIdLong());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(GIVEAWAY_MESSAGE_TEMPLATE_KEY, model, serverId);
        if(giveawayCreationRequest.getTargetChannel() == null) {
            log.info("Sending giveaway to post target in server {}", serverId);
            postTargetService.validatePostTarget(GiveawayPostTarget.GIVEAWAYS, giveawayCreationRequest.getCreator().getGuild().getIdLong());
            messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, GiveawayPostTarget.GIVEAWAYS, serverId);
        } else {
            log.info("Sending giveaway to channel {} in server {}.", giveawayCreationRequest.getTargetChannel().getId(), serverId);
            messageFutures = channelService.sendMessageToSendToChannel(messageToSend, giveawayCreationRequest.getTargetChannel());
        }
        CompletableFutureList<Message> messageFutureList = new CompletableFutureList<>(messageFutures);
        return messageFutureList.getMainFuture().thenAccept(o -> {
            Message createdMessage = messageFutureList.getFutures().get(0).join();
            giveawayCreationRequest.setTargetChannel(createdMessage.getGuildChannel());
            self.persistGiveaway(giveawayCreationRequest, giveawayId, createdMessage.getIdLong(), componentId);
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
        Optional<Giveaway> giveAwayOptional = giveawayManagementService.loadGiveawayById(giveawayId, serverId);
        if(giveAwayOptional.isEmpty()) {
            throw new GiveawayNotFoundException();
        }
        log.info("Evaluating giveaway {} in server {}.", giveawayId, serverId);
        Giveaway giveaway = giveAwayOptional.get();
        Set<Long> winners = new HashSet<>();
        Integer winnerCount = giveaway.getWinnerCount();
        giveaway.getParticipants().forEach(giveawayParticipant -> giveawayParticipant.setWon(false));
        List<Long> potentialWinners = new ArrayList<>(giveaway
                .getParticipants()
                .stream()
                .map(giveawayParticipant -> giveawayParticipant.getParticipant().getUserInServerId())
                .toList());

        if(potentialWinners.size() <= winnerCount) {
            winners.addAll(potentialWinners);
            log.debug("Less participants than total winners - selecting all for giveaway {} in server {}.", giveawayId, serverId);
        } else {
            for (int i = 0; i < winnerCount; i++) {
                int winnerIndex = secureRandom.nextInt(potentialWinners.size());
                Long winner = potentialWinners.get(winnerIndex);
                potentialWinners.remove(winnerIndex);
                winners.add(winner);
            }
        }
        List<GiveawayParticipant> winningParticipants = giveaway
                .getParticipants()
                .stream()
                .filter(giveawayParticipant -> winners.contains(giveawayParticipant.getParticipant().getUserInServerId()))
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

        GiveawayMessageModel giveawayMessageModel = GiveawayMessageModel.fromGiveaway(giveaway);
        giveawayMessageModel.setWinners(winnerDisplays);
        giveawayMessageModel.setEnded(true);
        MessageToSend giveawayMessageToSend = templateService.renderEmbedTemplate(GIVEAWAY_MESSAGE_TEMPLATE_KEY, giveawayMessageModel, serverId);
        log.info("Updating original giveaway message for giveaway {} in server {}.", giveawayId, serverId);
        GuildMessageChannel messageChannel = channelService.getMessageChannelFromServer(giveaway.getServer().getId(), giveaway.getGiveawayChannel().getId());
        CompletableFuture<Message> giveawayUpdateFuture = channelService.editMessageInAChannelFuture(giveawayMessageToSend, messageChannel, giveaway.getMessageId());
        resultFutures.add(giveawayUpdateFuture);
        return new CompletableFutureList<>(resultFutures).getMainFuture();
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
        Member creatorMember = giveawayCreationRequest.getCreator();
        log.info("Persisting giveaway in server {} with message id {}.", creatorMember.getGuild().getIdLong(), messageId);
        Instant targetDate = Instant.now().plus(giveawayCreationRequest.getDuration());
        AChannel targetChannel = channelManagementService.loadChannel(giveawayCreationRequest.getTargetChannel().getIdLong());
        AUserInAServer creator = userInServerManagementService.loadOrCreateUser(creatorMember);
        AUserInAServer benefactor;
        if(giveawayCreationRequest.getBenefactor() != null) {
            benefactor = userInServerManagementService.loadOrCreateUser(giveawayCreationRequest.getBenefactor());
        } else {
            benefactor = null;
        }

        Giveaway giveaway = giveawayManagementService.createGiveaway(creator, benefactor, targetChannel, targetDate,
                giveawayCreationRequest.getTitle(), giveawayCreationRequest.getDescription(), giveawayCreationRequest.getWinnerCount(),
                messageId, componentId, giveawayId);

        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("giveawayId", giveaway.getGiveawayId().getId().toString());
        parameters.put("serverId", giveaway.getGiveawayId().getServerId().toString());
        JobParameters jobParameters = JobParameters
                .builder()
                .parameters(parameters)
                .build();
        log.info("Scheduling giveaway reminder for giveaway {} originating from message {} in server {}.", giveaway.getGiveawayId().getId(), messageId, creatorMember.getGuild().getIdLong());
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
