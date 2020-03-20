package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ConfigurationException;
import dev.sheldan.abstracto.core.management.PostTargetManagement;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class PostTargetServiceBean implements PostTargetService {

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private Bot botService;

    @Override
    public void sendTextInPostTarget(String text, PostTarget target) {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        textChannelForPostTarget.sendMessage(text).queue();
    }

    @Override
    public void sendEmbedInPostTarget(MessageEmbed embed, PostTarget target) {
        TextChannel textChannelForPostTarget = getTextChannelForPostTarget(target);
        textChannelForPostTarget.sendMessage(embed).queue();
    }

    private TextChannel getTextChannelForPostTarget(PostTarget target) {
        Guild guild = botService.getInstance().getGuildById(target.getServerReference().getId());
        if(guild != null) {
            TextChannel textChannelById = guild.getTextChannelById(target.getChannelReference().getId());
            if(textChannelById != null) {
                return textChannelById;
            } else {
                log.warn("Incorrect post target configuration: {} points to {} on server {}", target.getName(),
                        target.getChannelReference().getId(), target.getServerReference().getId());
            }
        } else {
            log.warn("Incorrect post target configuration: Guild id {} was not found.", target.getServerReference().getId());
        }
        throw new ConfigurationException("Incorrect post target configuration.");
    }

    private PostTarget getPostTarget(String postTargetName, Long serverId) {
        PostTarget postTarget = postTargetManagement.getPostTarget(postTargetName, serverId);
        if(postTarget != null) {
            return postTarget;
        } else {
            log.warn("PostTarget {} in server {} was not found!", postTargetName, serverId);
            throw new ConfigurationException(String.format("Incorrect post target configuration: Post target %s was not found.", postTargetName));
        }
    }

    @Override
    public void sendTextInPostTarget(String text, String postTargetName, Long serverId) {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        this.sendTextInPostTarget(text, postTarget);
    }

    @Override
    public void sendEmbedInPostTarget(MessageEmbed embed, String postTargetName, Long serverId) {
        PostTarget postTarget = this.getPostTarget(postTargetName, serverId);
        this.sendEmbedInPostTarget(embed, postTarget);
    }
}
