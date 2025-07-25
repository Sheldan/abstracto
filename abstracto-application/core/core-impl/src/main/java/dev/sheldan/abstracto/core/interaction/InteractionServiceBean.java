package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.AllowedMentionService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.AttachedFile;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public List<CompletableFuture<Message>> sendMessageToInteraction(MessageToSend messageToSend, InteractionHook interactionHook) {
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        List<WebhookMessageCreateAction<Message>> allMessageActions = new ArrayList<>();
        int iterations = Math.min(messageToSend.getMessages().size(), messageToSend.getEmbeds().size());
        for (int i = 0; i < iterations; i++) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            String text = messageToSend.getMessages().get(i);
            MessageEmbed embed = messageToSend.getEmbeds().get(i);
            WebhookMessageCreateAction<Message> messageAction = interactionHook.sendMessage(text).addEmbeds(embed);
            allMessageActions.add(messageAction);
        }
        // one of these loops will get additional iterations, if the number is different, not both
        for (int i = iterations; i < messageToSend.getMessages().size(); i++) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            String text = messageToSend.getMessages().get(i);
            WebhookMessageCreateAction<Message> messageAction = interactionHook.sendMessage(text);
            allMessageActions.add(messageAction);
        }
        for (int i = iterations; i < messageToSend.getEmbeds().size(); i++) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            MessageEmbed embed = messageToSend.getEmbeds().get(i);
            WebhookMessageCreateAction<Message> messageAction = interactionHook.sendMessageEmbeds(embed);
            allMessageActions.add(messageAction);
        }

        List<ActionRow> actionRows = messageToSend.getActionRows();
        if(!actionRows.isEmpty()) {
            AServer server = serverManagementService.loadServer(interactionHook.getInteraction().getGuild());
            allMessageActions.set(0, allMessageActions.get(0).addComponents(actionRows));
            actionRows.forEach(components -> components.forEach(component -> {
                if(component instanceof ActionComponent) {
                    String id = ((ActionComponent)component).getId();
                    MessageToSend.ComponentConfig payload = messageToSend.getComponentPayloads().get(id);
                    if(payload != null && payload.getPersistCallback()) {
                        componentPayloadManagementService.createPayload(id, payload.getPayload(), payload.getPayloadType(), payload.getComponentOrigin(), server, payload.getComponentType());
                    }
                }
            }));
        }

        if(messageToSend.getEphemeral()) {
            Interaction interaction = interactionHook.getInteraction();
            interactionHook.setEphemeral(messageToSend.getEphemeral());
            if(ContextUtils.hasGuild(interaction)) {
                log.info("Sending ephemeral message to interaction in guild {} in channel {} for user {}.",
                        interaction.getGuild().getIdLong(), interaction.getChannel().getId(),
                        interaction.getUser().getIdLong());
            }
            metricService.incrementCounter(EPHEMERAL_MESSAGES_SEND);
        }

        if(messageToSend.hasFilesToSend()) {
            List<FileUpload> attachedFiles = messageToSend
                    .getAttachedFiles()
                    .stream()
                    .map(AttachedFile::convertToFileUpload)
                    .collect(Collectors.toList());
            if(!allMessageActions.isEmpty()) {
                // in case there has not been a message, we need to increment it
                allMessageActions.set(0, allMessageActions.get(0).setFiles(attachedFiles));
            } else {
                metricService.incrementCounter(MESSAGE_SEND_METRIC);
                allMessageActions.add(interactionHook.sendFiles(attachedFiles));
            }
        }
        Set<Message.MentionType> allowedMentions = allowedMentionService.getAllowedMentionsFor(interactionHook.getInteraction().getMessageChannel(), messageToSend);
        allMessageActions.forEach(messageAction -> futures.add(messageAction.setAllowedMentions(allowedMentions).setEphemeral(messageToSend.getEphemeral()).submit()));
        return futures;
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToInteraction(String templateKey, Object model, InteractionHook interactionHook) {
        Long serverId = ContextUtils.serverIdOrNull(interactionHook);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(templateKey, model, serverId);
        return sendMessageToInteraction(messageToSend, interactionHook);
    }

    @Override
    public CompletableFuture<InteractionHook> replyEmbed(String templateKey, Object model, IReplyCallback callback) {
        Long serverId = ContextUtils.serverIdOrNull(callback);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(templateKey, model, serverId);
        return replyMessageToSend(messageToSend, callback);
    }

    @Override
    public CompletableFuture<InteractionHook> replyString(String text, IReplyCallback callback) {
        return callback
                .reply(text)
                .setAllowedMentions(allowedMentionService.getAllowedMentionTypesForServer(callback.getGuild().getIdLong()))
                .submit();
    }

    @Override
    public CompletableFuture<InteractionHook> replyEmbed(String templateKey, IReplyCallback callback) {
        return replyEmbed(templateKey, new Object(), callback);
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbed(String templateKey, InteractionHook interactionHook) {
        Long serverId = ContextUtils.serverIdOrNull(interactionHook);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(templateKey, new Object(), serverId);
        return sendMessageToInteraction(messageToSend, interactionHook);
    }

    @Override
    public CompletableFuture<Message> replaceOriginal(MessageToSend messageToSend, InteractionHook interactionHook) {
        Long serverId = ContextUtils.serverIdOrNull(interactionHook);

        if(messageToSend.getEphemeral()) {
            Interaction interaction = interactionHook.getInteraction();
            if(ContextUtils.hasGuild(interaction)) {
                log.info("Sending ephemeral message to interaction in guild {} in channel {} for user {}.",
                        interaction.getGuild().getIdLong(), interaction.getChannel().getId(),
                        interaction.getUser().getIdLong());
            } else {
                log.info("Sending ephemeral message to interaction for user {}", interactionHook.getInteraction().getUser().getIdLong());
            }
            metricService.incrementCounter(EPHEMERAL_MESSAGES_SEND);
            interactionHook.setEphemeral(true);
        }

        WebhookMessageEditAction<Message> action = null;
        if(messageToSend.getMessages() != null && !messageToSend.getMessages().isEmpty()) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            action = interactionHook.editOriginal(messageToSend.getMessages().get(0));
        }

        if(messageToSend.getEmbeds() != null && !messageToSend.getEmbeds().isEmpty()) {
            if(action != null) {
                action = action.setEmbeds(messageToSend.getEmbeds().subList(0, Math.min(10, messageToSend.getEmbeds().size())));
            } else {
                action = interactionHook.editOriginalEmbeds(messageToSend.getEmbeds());
            }
        }

        if(messageToSend.hasFilesToSend()) {
            List<FileUpload> attachedFiles = messageToSend
                    .getAttachedFiles()
                    .stream()
                    .map(AttachedFile::convertToFileUpload)
                    .collect(Collectors.toList());
            if(action != null) {
                action.setFiles(attachedFiles);
            } else {
                metricService.incrementCounter(MESSAGE_SEND_METRIC);
                MessageEditData messageEditData = MessageEditData.fromFiles(attachedFiles);
                action = interactionHook.editOriginal(messageEditData);
            }
        }

        // this should be last, because we are "faking" a message, by inserting a ., in case there has not been a reply yet
        // we could also throw an exception, but we are allowing this to go through
        List<ActionRow> actionRows = messageToSend.getActionRows();
        if(actionRows != null && !actionRows.isEmpty()) {
            if(action == null) {
                action = interactionHook.editOriginal(".");
            }
            action = action.setComponents(actionRows);
            AServer server;
            if(ContextUtils.isGuildKnown(interactionHook.getInteraction())) {
                server = serverManagementService.loadServer(serverId);
            } else {
                server = null; }
            actionRows.forEach(components -> components.forEach(component -> {
                if(component instanceof ActionComponent) {
                    String id = ((ActionComponent)component).getId();
                    MessageToSend.ComponentConfig payload = messageToSend.getComponentPayloads().get(id);
                    if(payload != null && payload.getPersistCallback()) {
                        componentPayloadManagementService.createPayload(id, payload.getPayload(), payload.getPayloadType(), payload.getComponentOrigin(), server, payload.getComponentType());
                    }
                }
            }));
        }

        if(action == null) {
            throw new AbstractoRunTimeException("The callback did not result in any message.");
        }
        action.setReplace(true);
        return action.submit();
    }

    @Override
    public CompletableFuture<Message> replaceOriginal(String template, Object model, InteractionHook interactionHook) {
        Long serverId = interactionHook.getInteraction().getGuild().getIdLong();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(template, new Object(), serverId);
        return replaceOriginal(messageToSend, interactionHook);
    }

    @Override
    public CompletableFuture<Void> deleteMessage(InteractionHook interactionHook) {
        return interactionHook.deleteOriginal().submit();
    }

    public CompletableFuture<InteractionHook> replyMessageToSend(MessageToSend messageToSend, IReplyCallback callback) {
        ReplyCallbackAction action = null;
        if(messageToSend.getUseComponentsV2()) {
            action = callback.replyComponents(messageToSend.getComponents()).useComponentsV2();
        } else {
            if(messageToSend.getMessages() != null && !messageToSend.getMessages().isEmpty()) {
                metricService.incrementCounter(MESSAGE_SEND_METRIC);
                action = callback.reply(messageToSend.getMessages().get(0));
            }
            if(messageToSend.getEmbeds() != null && !messageToSend.getEmbeds().isEmpty()) {
                if(action != null) {
                    action = action.addEmbeds(messageToSend.getEmbeds().subList(0, Math.min(10, messageToSend.getEmbeds().size())));
                } else {
                    action = callback.replyEmbeds(messageToSend.getEmbeds());
                }
            }
            if(messageToSend.hasFilesToSend()) {
                List<FileUpload> attachedFiles = messageToSend
                    .getAttachedFiles()
                    .stream()
                    .map(AttachedFile::convertToFileUpload)
                    .collect(Collectors.toList());
                if(action != null) {
                    action.setFiles(attachedFiles);
                } else {
                    metricService.incrementCounter(MESSAGE_SEND_METRIC);
                    action = callback.replyFiles(attachedFiles);
                }
            }
            // this should be last, because we are "faking" a message, by inserting a ., in case there has not been a reply yet
            // we could also throw an exception, but we are allowing this to go through
            List<ActionRow> actionRows = messageToSend.getActionRows();
            if(actionRows != null && !actionRows.isEmpty()) {
                if(action == null) {
                    action = callback.reply(".");
                }
                action = action.setComponents(actionRows);
                AServer server;
                if(ContextUtils.isGuildKnown(callback)) {
                    server = serverManagementService.loadServer(callback.getGuild().getIdLong());
                } else {
                    server = null;
                }
                actionRows.forEach(components -> components.forEach(component -> {
                    if(component instanceof ActionComponent) {
                        String id = ((ActionComponent)component).getId();
                        MessageToSend.ComponentConfig payload = messageToSend.getComponentPayloads().get(id);
                        if(payload != null && payload.getPersistCallback()) {
                            componentPayloadManagementService.createPayload(id, payload.getPayload(), payload.getPayloadType(), payload.getComponentOrigin(), server, payload.getComponentType());
                        }
                    }
                }));
            }
        }

        if(messageToSend.getEphemeral()) {
            if(ContextUtils.hasGuild(callback)) {
                log.info("Sending ephemeral message to interaction in guild {} in channel {} for user {}.",
                        callback.getGuild().getIdLong(), callback.getChannel().getId(),
                        callback.getUser().getIdLong());
            } else {
                log.info("Sending ephemeral message to user {}.", callback.getUser().getIdLong());
            }
            metricService.incrementCounter(EPHEMERAL_MESSAGES_SEND);
            if(action != null) {
                action = action.setEphemeral(messageToSend.getEphemeral());
            }
        }
        if(ContextUtils.isGuildKnown(callback)) {
            Set<Message.MentionType> allowedMentions = allowedMentionService.getAllowedMentionsFor(callback.getMessageChannel(), messageToSend);
            if (action != null) {
                action = action.setAllowedMentions(allowedMentions);
            }
        }

        if(action == null) {
            throw new AbstractoRunTimeException("The callback did not result in any message.");
        }
        return action.submit();
    }

    @Override
    public CompletableFuture<InteractionHook> replyMessage(String templateKey, Object model, IReplyCallback callback) {
        Long serverId = ContextUtils.serverIdOrNull(callback);
        MessageToSend messageToSend = templateService.renderTemplateToMessageToSend(templateKey, model, serverId);
        return replyMessageToSend(messageToSend, callback);
    }

    @Override
    public CompletableFuture<Message> replyString(String text, InteractionHook interactionHook) {
        Set<Message.MentionType> allowedMentions = allowedMentionService.getAllowedMentionsFor(interactionHook.getInteraction().getMessageChannel(), null);
        return interactionHook.sendMessage(text).setAllowedMentions(allowedMentions).submit();
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(EPHEMERAL_MESSAGES_SEND, "Ephemeral messages send");
    }
}
