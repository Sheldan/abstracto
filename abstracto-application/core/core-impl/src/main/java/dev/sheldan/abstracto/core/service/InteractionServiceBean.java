package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.core.config.MetricConstants.DISCORD_API_INTERACTION_METRIC;
import static dev.sheldan.abstracto.core.config.MetricConstants.INTERACTION_TYPE;
import static dev.sheldan.abstracto.core.service.MessageServiceBean.MESSAGE_SEND_METRIC;

@Component
@Slf4j
public class InteractionServiceBean implements InteractionService {

    @Autowired
    private MetricService metricService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private AllowedMentionService allowedMentionService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private TemplateService templateService;

    public static final CounterMetric EPHEMERAL_MESSAGES_SEND = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "message.ephemeral.send")))
            .build();

    @Override
    public List<CompletableFuture<Message>> sendMessageToInteraction(MessageToSend messageToSend, InteractionHook interactionHook){
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        List<WebhookMessageAction<Message>> allMessageActions = new ArrayList<>();
        int iterations = Math.min(messageToSend.getMessages().size(), messageToSend.getEmbeds().size());
        for (int i = 0; i < iterations; i++) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            String text = messageToSend.getMessages().get(i);
            MessageEmbed embed = messageToSend.getEmbeds().get(i);
            WebhookMessageAction<Message> messageAction = interactionHook.sendMessage(text).addEmbeds(embed);
            allMessageActions.add(messageAction);
        }
        // one of these loops will get additional iterations, if the number is different, not both
        for (int i = iterations; i < messageToSend.getMessages().size(); i++) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            String text = messageToSend.getMessages().get(i);
            WebhookMessageAction<Message> messageAction = interactionHook.sendMessage(text);
            allMessageActions.add(messageAction);
        }
        for (int i = iterations; i < messageToSend.getEmbeds().size(); i++) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            MessageEmbed embed = messageToSend.getEmbeds().get(i);
            WebhookMessageAction<Message> messageAction = interactionHook.sendMessageEmbeds(embed);
            allMessageActions.add(messageAction);
        }

        List<ActionRow> actionRows = messageToSend.getActionRows();
        if(!actionRows.isEmpty()) {
            AServer server = serverManagementService.loadServer(interactionHook.getInteraction().getGuild());
            allMessageActions.set(0, allMessageActions.get(0).addActionRows(actionRows));
            actionRows.forEach(components -> components.forEach(component -> {
                if(component instanceof ActionComponent) {
                    String id = ((ActionComponent)component).getId();
                    MessageToSend.ComponentConfig payload = messageToSend.getComponentPayloads().get(id);
                    if(payload.getPersistCallback()) {
                        componentPayloadManagementService.createPayload(id, payload.getPayload(), payload.getPayloadType(), payload.getComponentOrigin(), server, payload.getComponentType());
                    }
                }
            }));
        }

        if(messageToSend.getEphemeral()) {
            Interaction interaction = interactionHook.getInteraction();
            log.info("Sending ephemeral message to interaction in guild {} in channel {} for user {}.",
                    interaction.getGuild().getIdLong(), interaction.getChannel().getId(),
                    interaction.getMember().getIdLong());
            metricService.incrementCounter(EPHEMERAL_MESSAGES_SEND);
        }

        if(messageToSend.hasFileToSend()) {
            if(!allMessageActions.isEmpty()) {
                // in case there has not been a message, we need to increment it
                allMessageActions.set(0, allMessageActions.get(0).addFile(messageToSend.getFileToSend()));
            } else {
                metricService.incrementCounter(MESSAGE_SEND_METRIC);
                allMessageActions.add(interactionHook.sendFile(messageToSend.getFileToSend()));
            }
        }
        Set<Message.MentionType> allowedMentions = allowedMentionService.getAllowedMentionsFor(interactionHook.getInteraction().getMessageChannel(), messageToSend);
        allMessageActions.forEach(messageAction -> futures.add(messageAction.allowedMentions(allowedMentions).setEphemeral(messageToSend.getEphemeral()).submit()));
        return futures;
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToInteraction(String templateKey, Object model, InteractionHook interactionHook) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate(templateKey, model, interactionHook.getInteraction().getGuild().getIdLong());
        return sendMessageToInteraction(messageToSend, interactionHook);
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(EPHEMERAL_MESSAGES_SEND, "Ephemeral messages send");
    }
}
