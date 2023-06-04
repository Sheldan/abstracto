package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.interaction.menu.SelectMenuConfigModel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.suggestion.config.PollFeatureMode;
import dev.sheldan.abstracto.suggestion.config.PollPostTarget;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.exception.PollCancellationNotPossibleException;
import dev.sheldan.abstracto.suggestion.exception.PollOptionAlreadyExistsException;
import dev.sheldan.abstracto.suggestion.model.payload.PollAddOptionButtonPayload;
import dev.sheldan.abstracto.suggestion.model.PollCreationRequest;
import dev.sheldan.abstracto.suggestion.model.database.*;
import dev.sheldan.abstracto.suggestion.model.payload.QuickPollSelectionMenuPayload;
import dev.sheldan.abstracto.suggestion.model.template.*;
import dev.sheldan.abstracto.suggestion.model.payload.ServerPollSelectionMenuPayload;
import dev.sheldan.abstracto.suggestion.service.management.PollManagementService;
import dev.sheldan.abstracto.suggestion.service.management.PollOptionManagementService;
import dev.sheldan.abstracto.suggestion.service.management.PollUserDecisionManagementService;
import dev.sheldan.abstracto.suggestion.service.management.PollUserDecisionOptionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PollServiceBean implements PollService {

    @Autowired
    private CounterService counterService;

    @Autowired
    private PollManagementService pollManagementService;

    @Autowired
    private PollOptionManagementService pollOptionManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private PollUserDecisionManagementService pollUserDecisionManagementService;

    @Autowired
    private PollUserDecisionOptionManagementService pollUserDecisionOptionManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PollServiceBean self;

    private static final String POLLS_COUNTER_KEY = "POLLS";
    public static final String SERVER_POLL_SELECTION_MENU_ORIGIN = "SERVER_POLL_SELECTION_MENU";
    public static final String SERVER_POLL_ADD_OPTION_ORIGIN = "SERVER_POLL_ADD_OPTION_BUTTON";
    private static final String SERVER_POLL_TEMPLATE_KEY = "poll_server_message";
    private static final String SERVER_POLL_CLOSE_MESSAGE = "poll_server_close_message";
    private static final String SERVER_POLL_REMINDER_TEMPLATE_KEY = "poll_server_reminder_message";
    private static final String SERVER_POLL_EVALUATION_UPDATE_TEMPLATE_KEY = "poll_server_evaluation_update_message";
    private static final String QUICK_POLLS_COUNTER_KEY = "QUICK_POLLS";
    public static final String QUICK_POLL_SELECTION_MENU_ORIGIN = "QUICK_POLL_SELECTION_MENU";
    private static final String QUICK_POLL_TEMPLATE_KEY = "poll_quick_message";
    private static final String QUICK_POLL_EVALUATION_UPDATE_TEMPLATE_KEY = "poll_quick_evaluation_update_message";

    @Value("${abstracto.feature.poll.removalMaxAge}")
    private Long removalMaxAgeSeconds;

    @Override
    @Transactional
    public CompletableFuture<Void> createServerPoll(Member creator, List<String> options, String description,
                                                    Boolean allowMultiple, Boolean allowAddition, Boolean showDecisions, Duration pollDuration) {
        Long serverId = creator.getGuild().getIdLong();
        HashSet<String> optionAsSet = new HashSet<>(options);
        if(optionAsSet.size() != options.size()) {
            throw new PollOptionAlreadyExistsException();
        }
        Long pollId = counterService.getNextCounterValue(serverId, POLLS_COUNTER_KEY);
        log.info("Creating server poll {} in server {} because of user {}.", pollId, serverId, creator.getIdLong());
        List<PollMessageOption> parsedOptions = parseOptions(options);
        String selectionMenuId = componentService.generateComponentId();
        String addOptionButtonId = componentService.generateComponentId();
        if(pollDuration == null) {
            Long pollDurationSeconds = configService.getLongValueOrConfigDefault(PollService.SERVER_POLL_DURATION_SECONDS, serverId);
            log.info("No duration provided - using {} seconds from configuration.", pollDurationSeconds);
            pollDuration = Duration.ofSeconds(pollDurationSeconds);
        }
        Instant targetDate = Instant.now().plus(pollDuration);
        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("serverId", serverId.toString());
        parameters.put("pollId", pollId.toString());
        JobParameters jobParameters = JobParameters.builder().parameters(parameters).build();
        String triggerKey = null;
        if(featureModeService.featureModeActive(SuggestionFeatureDefinition.POLL, serverId, PollFeatureMode.POLL_AUTO_EVALUATE)) {
            log.info("Creating scheduled job to evaluate poll {} in server {} at {}.", pollId, serverId, targetDate);
            triggerKey = schedulerService.executeJobWithParametersOnce("serverPollEvaluationJob", "poll", jobParameters, Date.from(targetDate));
        }
        String reminderTriggerKey = null;
        if(featureModeService.featureModeActive(SuggestionFeatureDefinition.POLL, serverId, PollFeatureMode.POLL_REMINDER)) {
            log.info("Creating scheduled job to remind about poll {} in server {} at {}.", pollId, serverId, targetDate);
            reminderTriggerKey = schedulerService.executeJobWithParametersOnce("serverPollReminderJob", "poll", jobParameters, Date.from(targetDate));
        }
        PollCreationRequest pollCreationRequest = PollCreationRequest
                .builder()
                .pollId(pollId)
                .type(PollType.STANDARD)
                .allowAddition(allowAddition)
                .allowMultiple(allowMultiple)
                .showDecisions(showDecisions)
                .addOptionButtonId(addOptionButtonId)
                .reminderJobTrigger(reminderTriggerKey)
                .selectionMenuId(selectionMenuId)
                .serverId(serverId)
                .evaluationJobTrigger(triggerKey)
                .targetDate(targetDate)
                .creatorId(creator.getIdLong())
                .description(description)
                .options(parsedOptions)
                .build();

        ServerPollMessageModel model = ServerPollMessageModel
                .builder()
                .creator(MemberDisplay.fromMember(creator))
                .description(description)
                .pollId(pollId)
                .state(PollState.NEW)
                .allowMultiple(allowMultiple)
                .showDecisions(showDecisions)
                .allowAdditions(allowAddition)
                .endDate(targetDate)
                .options(parsedOptions)
                .addOptionButtonId(addOptionButtonId)
                .selectionMenuId(selectionMenuId)
                .build();
        ServerPollSelectionMenuPayload payload = ServerPollSelectionMenuPayload
                .builder()
                .serverId(serverId)
                .pollId(pollId)
                .build();
        SelectMenuConfigModel selectMenuConfigModel = SelectMenuConfigModel
                .builder()
                .selectMenuId(selectionMenuId)
                .origin(SERVER_POLL_SELECTION_MENU_ORIGIN)
                .selectMenuPayload(payload)
                .payloadType(ServerPollSelectionMenuPayload.class)
                .build();
        componentPayloadManagementService.createStringSelectMenuPayload(selectMenuConfigModel, serverId);
        PollAddOptionButtonPayload buttonPayload = PollAddOptionButtonPayload
                .builder()
                .serverId(serverId)
                .pollId(pollId)
                .build();
        ButtonConfigModel buttonConfigModel = ButtonConfigModel
                .builder()
                .buttonId(addOptionButtonId)
                .buttonPayload(buttonPayload)
                .origin(SERVER_POLL_ADD_OPTION_ORIGIN)
                .payloadType(PollAddOptionButtonPayload.class)
                .build();
        componentPayloadManagementService.createButtonPayload(buttonConfigModel, serverId);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SERVER_POLL_TEMPLATE_KEY, model);
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, PollPostTarget.POLLS, serverId);
        return FutureUtils.toSingleFutureGeneric(messageFutures)
                .thenAccept(unused -> self.persistPoll(messageFutures.get(0).join(), pollCreationRequest));
    }

    @Override
    public CompletableFuture<Void> createQuickPoll(Member creator, List<String> options, String description,
                                                   Boolean allowMultiple, Boolean showDecisions, InteractionHook interactionHook, Duration pollDuration) {
        HashSet<String> optionAsSet = new HashSet<>(options);
        if(optionAsSet.size() != options.size()) {
            throw new PollOptionAlreadyExistsException();
        }
        Long serverId = creator.getGuild().getIdLong();
        Long pollId = counterService.getNextCounterValue(serverId, QUICK_POLLS_COUNTER_KEY);
        log.info("Creating quick poll {} in server {} because of user {}.", pollId, serverId, creator.getIdLong());
        List<PollMessageOption> parsedOptions = parseOptions(options);
        String selectionMenuId = componentService.generateComponentId();
        if(pollDuration == null) {
            Long pollDurationSeconds = configService.getLongValueOrConfigDefault(PollService.QUICK_POLL_DURATION_SECONDS, serverId);
            log.info("No duration provided - using {} seconds from configuration.", pollDurationSeconds);
            pollDuration = Duration.ofSeconds(pollDurationSeconds);
        }
        Instant targetDate = Instant.now().plus(pollDuration);
        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("serverId", serverId.toString());
        parameters.put("pollId", pollId.toString());
        JobParameters jobParameters = JobParameters.builder().parameters(parameters).build();
        String triggerKey = schedulerService.executeJobWithParametersOnce("quickPollEvaluationJob", "poll", jobParameters, Date.from(targetDate));
        log.info("Starting scheduled job to evaluate quick poll.");
        PollCreationRequest pollCreationRequest = PollCreationRequest
                .builder()
                .pollId(pollId)
                .type(PollType.QUICK)
                .allowMultiple(allowMultiple)
                .evaluationJobTrigger(triggerKey)
                .showDecisions(showDecisions)
                .selectionMenuId(selectionMenuId)
                .serverId(serverId)
                .allowAddition(false)
                .targetDate(targetDate)
                .creatorId(creator.getIdLong())
                .description(description)
                .options(parsedOptions)
                .build();

        QuickPollMessageModel model = QuickPollMessageModel
                .builder()
                .creator(MemberDisplay.fromMember(creator))
                .description(description)
                .pollId(pollId)
                .allowMultiple(allowMultiple)
                .showDecisions(showDecisions)
                .endDate(targetDate)
                .options(parsedOptions)
                .selectionMenuId(selectionMenuId)
                .build();
        QuickPollSelectionMenuPayload payload = QuickPollSelectionMenuPayload
                .builder()
                .serverId(serverId)
                .pollId(pollId)
                .build();
        SelectMenuConfigModel selectMenuConfigModel = SelectMenuConfigModel
                .builder()
                .selectMenuId(selectionMenuId)
                .origin(QUICK_POLL_SELECTION_MENU_ORIGIN)
                .selectMenuPayload(payload)
                .payloadType(QuickPollSelectionMenuPayload.class)
                .build();
        componentPayloadManagementService.createStringSelectMenuPayload(selectMenuConfigModel, serverId);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(QUICK_POLL_TEMPLATE_KEY, model);
        List<CompletableFuture<Message>> messageFutures = interactionService.sendMessageToInteraction(messageToSend, interactionHook);
        return FutureUtils.toSingleFutureGeneric(messageFutures)
                .thenAccept(unused -> self.persistPoll(messageFutures.get(0).join(), pollCreationRequest));

    }

    @Override
    public CompletableFuture<Void> setDecisionsInPollTo(Member voter, List<String> chosenValues, Long pollId, PollType pollType) {
        Poll poll = pollManagementService.getPollByPollId(pollId, voter.getGuild().getIdLong(), pollType);
        log.info("Adding decisions of user {} to poll {}.", voter.getIdLong(), poll.getPollId());
        AUserInAServer userInServer = userInServerManagementService.loadOrCreateUser(voter);
        Optional<PollUserDecision> decisionOptional = pollUserDecisionManagementService.getUserDecisionOptional(poll, userInServer);
        PollUserDecision decision;
        boolean needToSave = false;
        if(decisionOptional.isPresent()) {
            decision = decisionOptional.get();
        } else {
            needToSave = true;
            decision = pollUserDecisionManagementService.createUserDecision(poll, userInServer);
        }
        Long optionsAdded = 0L;
        for (PollOption pollOption : poll.getOptions()) {
            if (chosenValues.contains(pollOption.getValue()) &&
                    (decision.getOptions() == null || decision.getOptions().stream().noneMatch(pollUserDecisionOption -> pollUserDecisionOption.getPollOption().getLabel().equals(pollOption.getValue())))) {
                pollUserDecisionOptionManagementService.addDecisionForUser(decision, pollOption);
                optionsAdded += 1;
            }
        }
        log.info("Added {} options to poll {} for user {}.", optionsAdded, pollId, voter.getIdLong());

        if(decision.getOptions() != null) {
            List<PollUserDecisionOption> toRemove = decision
                    .getOptions()
                    .stream()
                    .filter(pollUserDecisionOption -> !chosenValues.contains(pollUserDecisionOption.getPollOption().getLabel()))
                    .collect(Collectors.toList());
            log.info("Removing {} options from poll {} for user {}.", toRemove.size(), pollId, voter.getIdLong());
            pollUserDecisionOptionManagementService.deleteDecisionOptions(decision, toRemove);
        }
        if(needToSave) {
            pollUserDecisionManagementService.savePollUserDecision(decision);
        }
        if(poll.getShowDecisions()) {
            return updatePollMessage(poll, voter.getGuild());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> addOptionToServerPoll(Long pollId, Long serverId, Member adder, String label, String description) {
        Poll poll = pollManagementService.getPollByPollId(pollId, serverId, PollType.STANDARD);
        log.info("Adding option to server poll {} in server {}.", pollId, serverId);
        pollOptionManagementService.addOptionToPoll(poll, label, description);
        List<PollMessageOption> options = getOptionsOfPoll(poll);
        ServerPollMessageModel model = ServerPollMessageModel.fromPoll(poll, options);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SERVER_POLL_TEMPLATE_KEY, model);
        MessageChannel pollChannel = adder.getGuild().getChannelById(MessageChannel.class, poll.getChannel().getId());
        List<CompletableFuture<Message>> messageFutures = channelService.editMessagesInAChannelFuture(messageToSend, pollChannel, Arrays.asList(poll.getMessageId()));
        return FutureUtils.toSingleFutureGeneric(messageFutures);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> evaluateServerPoll(Long pollId, Long serverId) {
        Poll poll = pollManagementService.getPollByPollId(pollId, serverId, PollType.STANDARD);
        log.info("Evaluating server poll {} in server {}.", pollId, serverId);
        poll.setState(PollState.FINISHED);
        List<PollMessageOption> allOptions = getOptionsOfPoll(poll);
        List<PollMessageOption> topOptions = allOptions;
        if(!allOptions.isEmpty()) {
            Integer mostVotes = allOptions
                    .stream()
                    .sorted(Comparator.comparingInt(PollMessageOption::getVotes).reversed())
                    .collect(Collectors.toList()).get(0).getVotes();
            topOptions = allOptions
                    .stream()
                    .filter(pollMessageOption -> pollMessageOption.getVotes().equals(mostVotes))
                    .collect(Collectors.toList());
        }
        ServerPollEvaluationModel model = ServerPollEvaluationModel
                .builder()
                .pollId(pollId)
                .options(allOptions)
                .pollMessageId(poll.getMessageId())
                .topOptions(topOptions)
                .description(poll.getDescription())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SERVER_POLL_EVALUATION_UPDATE_TEMPLATE_KEY, model);
        log.info("Sending update message for poll evaluation of server poll {} in server {}.", pollId, serverId);
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, PollPostTarget.POLLS, serverId);
        GuildMessageChannel channel = channelService.getMessageChannelFromServer(serverId, poll.getChannel().getId());
        log.info("Cleaning existing components in message {} for server poll {} in server {}.", poll.getMessageId(), pollId, serverId);
        CompletableFuture<Message> cleanMessageFuture = channelService.removeComponents(channel, poll.getMessageId());
        return CompletableFuture.allOf(FutureUtils.toSingleFutureGeneric(messageFutures), cleanMessageFuture)
                .thenAccept(unused -> self.updateFinalPollMessage(pollId, channel.getGuild()));
    }

    @Override
    @Transactional
    public CompletableFuture<Void> remindServerPoll(Long pollId, Long serverId) {
        Poll poll = pollManagementService.getPollByPollId(pollId, serverId, PollType.STANDARD);
        log.info("Reminding about server poll {} in server {}.", pollId, serverId);
        List<PollMessageOption> allOptions = getOptionsOfPoll(poll);
        List<PollMessageOption> topOptions = allOptions;
        if(!allOptions.isEmpty()) {
            Integer mostVotes = allOptions
                    .stream()
                    .sorted(Comparator.comparingInt(PollMessageOption::getVotes).reversed())
                    .collect(Collectors.toList()).get(0).getVotes();
            topOptions = allOptions
                    .stream()
                    .filter(pollMessageOption -> pollMessageOption.getVotes().equals(mostVotes))
                    .collect(Collectors.toList());
        }
        ServerPollReminderModel model = ServerPollReminderModel
                .builder()
                .pollId(pollId)
                .options(allOptions)
                .topOptions(topOptions)
                .messageLink(MessageUtils.buildMessageUrl(serverId, poll.getChannel().getId(), poll.getMessageId()))
                .description(poll.getDescription())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SERVER_POLL_REMINDER_TEMPLATE_KEY, model);
        log.info("Sending poll reminder about server poll {} in server {}.", pollId, serverId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, PollPostTarget.POLL_REMINDER, serverId));
    }

    @Override
    @Transactional
    public CompletableFuture<Void> evaluateQuickPoll(Long pollId, Long serverId) {
        Poll poll = pollManagementService.getPollByPollId(pollId, serverId, PollType.QUICK);
        log.info("Evaluating quick poll {} in server {}.", pollId, serverId);
        poll.setState(PollState.FINISHED);
        List<PollMessageOption> allOptions = getOptionsOfPoll(poll);
        List<PollMessageOption> topOptions = allOptions;
        if(!allOptions.isEmpty()) {
            Integer mostVotes = allOptions
                    .stream()
                    .sorted(Comparator.comparingInt(PollMessageOption::getVotes).reversed())
                    .collect(Collectors.toList()).get(0).getVotes();
            topOptions = allOptions
                    .stream()
                    .filter(pollMessageOption -> pollMessageOption.getVotes().equals(mostVotes))
                    .collect(Collectors.toList());
        }
        QuickPollEvaluationModel model = QuickPollEvaluationModel
                .builder()
                .pollId(pollId)
                .options(allOptions)
                .pollMessageId(poll.getMessageId())
                .topOptions(topOptions)
                .description(poll.getDescription())
                .build();
        MessageChannel channel = channelService.getMessageChannelFromServer(serverId, poll.getChannel().getId());
        CompletableFuture<Message> removeComponentFuture = channelService.removeComponents(channel, poll.getMessageId());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(QUICK_POLL_EVALUATION_UPDATE_TEMPLATE_KEY, model);
        CompletableFuture<Void> updateMessageFuture = FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, channel));
        return CompletableFuture.allOf(removeComponentFuture, updateMessageFuture)
                .thenApply(message -> null);
    }

    @Override
    public CompletableFuture<Void> closePoll(Long pollId, Long serverId, String text, Member cause) {
        Poll poll = pollManagementService.getPollByPollId(pollId, serverId, PollType.STANDARD);
        log.info("Member {} closes poll {} in server {}.", cause.getIdLong(), pollId, serverId);
        PollClosingMessageModel model = PollClosingMessageModel
                .builder()
                .pollMessageId(poll.getMessageId())
                .cause(MemberNameDisplay.fromMember(cause))
                .pollId(pollId)
                .text(text)
                .serverId(serverId)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SERVER_POLL_CLOSE_MESSAGE, model);
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, PollPostTarget.POLLS, serverId);
        MessageChannel channel = channelService.getMessageChannelFromServer(serverId, poll.getChannel().getId());
        CompletableFuture<Message> removeComponentsFuture = channelService.removeComponents(channel, poll.getMessageId());
        return CompletableFuture.allOf(FutureUtils.toSingleFutureGeneric(messageFutures), removeComponentsFuture);
    }

    @Override
    public CompletableFuture<Void> cancelPoll(Long pollId, Long serverId, Member cause) {
        Poll poll = pollManagementService.getPollByPollId(pollId, serverId, PollType.STANDARD);
        log.info("Member {} cancelled poll {} in server {}.", cause.getIdLong(), pollId, serverId);
        if(!poll.getCreator().getUserReference().getId().equals(cause.getIdLong()) ||
                poll.getCreated().isBefore(Instant.now().minus(Duration.ofSeconds(removalMaxAgeSeconds)))) {
            throw new PollCancellationNotPossibleException();
        }
        if(poll.getReminderJobTriggerKey() != null) {
            schedulerService.stopTrigger(poll.getReminderJobTriggerKey());
        }
        if(poll.getEvaluationJobTriggerKey() != null) {
            schedulerService.stopTrigger(poll.getEvaluationJobTriggerKey());
        }
        poll.setState(PollState.CANCELLED);
        return messageService.deleteMessageInChannelInServer(serverId, poll.getChannel().getId(), poll.getMessageId());
    }

    @Transactional
    public CompletableFuture<Void> updateFinalPollMessage(Long pollId, Guild guild) {
        Poll poll = pollManagementService.getPollByPollId(pollId, guild.getIdLong(), PollType.STANDARD);
        List<PollMessageOption> options = getOptionsOfPoll(poll);
        ServerPollMessageModel model = ServerPollMessageModel.fromPoll(poll, options);
        model.setAllowAdditions(false);
        model.setShowDecisions(true);
        model.setAllowMultiple(false);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SERVER_POLL_TEMPLATE_KEY, model);
        MessageChannel pollChannel = guild.getChannelById(MessageChannel.class, poll.getChannel().getId());
        return channelService.editEmbedMessageInAChannel(messageToSend.getEmbeds().get(0), pollChannel, poll.getMessageId())
                .thenApply(message -> null);
    }

    public CompletableFuture<Void> updatePollMessage(Poll poll, Guild guild) {
        List<PollMessageOption> options = getOptionsOfPoll(poll);
        ServerPollMessageModel model = ServerPollMessageModel.fromPoll(poll, options);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SERVER_POLL_TEMPLATE_KEY, model);
        MessageChannel pollChannel = guild.getChannelById(MessageChannel.class, poll.getChannel().getId());
        return channelService.editEmbedMessageInAChannel(messageToSend.getEmbeds().get(0), pollChannel, poll.getMessageId())
                .thenApply(message -> null);
    }

    @Transactional
    public void persistPoll(Message message, PollCreationRequest pollCreationRequest) {
        if(message == null) {
            log.info("Post target was not setup - no message created.");
            return;
        }
        pollCreationRequest.setPollMessageId(message.getIdLong());
        pollCreationRequest.setPollChannelId(message.getChannel().getIdLong());
        log.info("Persisting poll {} shown in message {} in channel {} in server {}.",
                pollCreationRequest.getPollId(), pollCreationRequest.getPollMessageId(), pollCreationRequest.getPollChannelId(),
                pollCreationRequest.getServerId());
        Poll createdPoll = pollManagementService.createPoll(pollCreationRequest);
        log.info("Adding {} options to poll {}.", pollCreationRequest.getOptions().size(), pollCreationRequest.getPollId());
        pollOptionManagementService.addOptionsToPoll(createdPoll, pollCreationRequest);
    }

    private List<PollMessageOption> parseOptions(List<String> options) {
        return options.stream().map(s -> {
            String label = s;
            String description = "";
            if(s.contains(";")) {
                String[] splitOption = s.split(";");
                label = splitOption[0];
                description = splitOption[1];
            }
            return PollMessageOption
                    .builder()
                    .label(label)
                    .value(label)
                    .votes(0)
                    .percentage(0f)
                    .description(description)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<PollMessageOption> getOptionsOfPoll(Poll poll) {
        Integer totalVotes = poll
                .getDecisions()
                .stream()
                .map(userDecision -> userDecision.getOptions().size())
                .mapToInt(Integer::intValue)
                .sum();
        return poll.getOptions().stream().map(option -> {
            Long voteCount = poll
                    .getDecisions()
                    .stream()
                    .filter(decision -> decision.getOptions().stream().anyMatch(pollUserDecisionOption -> pollUserDecisionOption.getPollOption().equals(option)))
                    .count();
            return PollMessageOption
                    .builder()
                    .value(option.getValue())
                    .label(option.getLabel())
                    .votes(voteCount.intValue())
                    .percentage(totalVotes > 0 ? (voteCount / (float) totalVotes) * 100 : 0)
                    .description(option.getDescription())
                    .build();
        }).collect(Collectors.toList());
    }

}
