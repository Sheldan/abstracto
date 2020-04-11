package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.DynamicKeyLoader;
import dev.sheldan.abstracto.core.exception.ChannelException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class PostTargetServiceBean implements PostTargetService {

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private BotService botService;

    @Autowired
    private DynamicKeyLoader dynamicKeyLoader;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<Message> sendTextInPostTarget(String text, PostTarget target)  {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        return textChannelForPostTarget.sendMessage(text).submit();
    }

    @Override
    public CompletableFuture<Message>  sendEmbedInPostTarget(MessageEmbed embed, PostTarget target)  {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        return textChannelForPostTarget.sendMessage(embed).submit();
    }

    private TextChannel getTextChannelForPostTarget(PostTarget target)  {
        Guild guild = botService.getInstance().getGuildById(target.getServerReference().getId());
        if(guild != null) {
            TextChannel textChannelById = guild.getTextChannelById(target.getChannelReference().getId());
            if(textChannelById != null) {
                return textChannelById;
            } else {
                log.error("Incorrect post target configuration: {} points to {} on server {}", target.getName(),
                        target.getChannelReference().getId(), target.getServerReference().getId());
                throw new ChannelException(String.format("Incorrect post target configuration. The channel %s of target %s cannot be found",
                        target.getChannelReference().getId(), target.getChannelReference().getId()));
            }
        } else {
            throw new GuildException(String.format("Incorrect post target configuration. Guild %s cannot be found.", target.getServerReference().getId()));
        }
    }

    private PostTarget getPostTarget(String postTargetName, Long serverId) {
        PostTarget postTarget = postTargetManagement.getPostTarget(postTargetName, serverId);
        if(postTarget != null) {
            return postTarget;
        } else {
            log.error("PostTarget {} in server {} was not found!", postTargetName, serverId);
            throw new ChannelException(String.format("Incorrect post target configuration: Post target %s was not found.", postTargetName));
        }
    }

    @Override
    public CompletableFuture<Message>  sendTextInPostTarget(String text, String postTargetName, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return this.sendTextInPostTarget(text, postTarget);
    }

    @Override
    public CompletableFuture<Message>  sendEmbedInPostTarget(MessageEmbed embed, String postTargetName, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return this.sendEmbedInPostTarget(embed, postTarget);
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, String postTargetName, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return this.sendEmbedInPostTarget(message, postTarget);
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, PostTarget target)  {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        return channelService.sendMessageToEndInTextChannel(message, textChannelForPostTarget);
    }

    @Override
    public List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTarget target)  {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        String messageText = message.getMessage();
        if(StringUtils.isBlank(messageText)) {
            return Arrays.asList(textChannelForPostTarget.editMessageById(messageId, message.getEmbeds().get(0)).submit());
        } else {
            return Arrays.asList(textChannelForPostTarget.editMessageById(messageId, messageText).embed(message.getEmbeds().get(0)).submit());
        }
    }

    @Override
    public void editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTarget target, List<CompletableFuture<Message>> future)  {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        if(StringUtils.isBlank(messageToSend.getMessage().trim())) {
            textChannelForPostTarget
                    .retrieveMessageById(messageId)
                    .queue(
                            existingMessage -> existingMessage
                                    .editMessage(messageToSend.getEmbeds().get(0))
                                    .submit().thenAccept(message -> future.get(0).complete(message)),
                            throwable -> {
                                sendEmbedInPostTarget(messageToSend, target).get(0)
                                            .thenAccept(message -> future.get(0).complete(message));
                            });
        } else {
            textChannelForPostTarget
                    .retrieveMessageById(messageId)
                    .queue(
                            existingMessage -> existingMessage
                                    .editMessage(messageToSend.getMessage())
                                    .embed(messageToSend.getEmbeds().get(0))
                                    .submit().thenAccept(message -> future.get(0).complete(message)),
                            throwable -> {
                                sendEmbedInPostTarget(messageToSend, target).get(0)
                                            .thenAccept(message -> future.get(0).complete(message));
                            });
        }
    }

    @Override
    public void editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, String postTargetName, Long serverId, List<CompletableFuture<Message>> future)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        this.editOrCreatedInPostTarget(messageId, messageToSend, postTarget, future);
    }

    @Override
    public void throwIfPostTargetIsNotDefined(String name, Long serverId) throws ChannelException {
        PostTarget postTarget = postTargetManagement.getPostTarget(name, serverId);
        if(postTarget == null) {
            throw new ChannelException(String.format("Post target %s is not defined.", name));
        }
    }

    @Override
    public List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, String postTargetName, Long serverId)  {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        return editEmbedInPostTarget(messageId, message, postTarget);
    }

    @Override
    public boolean validPostTarget(String name) {
        List<String> possiblePostTargets = dynamicKeyLoader.getPostTargetsAsList();
        return possiblePostTargets.contains(name);
    }

    @Override
    public List<String> getAvailablePostTargets() {
        return dynamicKeyLoader.getPostTargetsAsList();
    }
}