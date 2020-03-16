package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.PostTarget;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.ServerService;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.HashMap;

@Service
@Slf4j
public class JoinLeaveListener extends ListenerAdapter {

    public static final String USER_JOIN_TEMPLATE = "user_join";
    public static final String USER_LEAVE_TEMPLATE = "user_leave";
    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private TemplateService templateService;

    @Override
    @Transactional
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        AServer server = serverService.loadServer(event.getGuild().getIdLong());
        PostTarget target = postTargetService.getPostTarget(PostTarget.JOIN_LOG, server);
        TextChannel textChannelById = event.getGuild().getTextChannelById(target.getChannelReference().getId());
        if(textChannelById != null){
            HashMap<String, Object> parameters = getUserParameter(event.getUser());
            String text = templateService.renderTemplate(USER_JOIN_TEMPLATE, parameters);
            textChannelById.sendMessage(text).queue();
        } else {
            log.warn("{} post target is not defined for server {}", PostTarget.JOIN_LOG, server.getId());
        }
    }

    @NotNull
    private HashMap<String, Object> getUserParameter(@Nonnull User user) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("user", user);
        parameters.put("userMention",  user.getAsMention());
        return parameters;
    }

    @Override
    @Transactional
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
        AServer server = serverService.loadServer(event.getGuild().getIdLong());
        PostTarget target = postTargetService.getPostTarget(PostTarget.LEAVE_LOG, server);
        TextChannel textChannelById = event.getGuild().getTextChannelById(target.getChannelReference().getId());
        if(textChannelById != null){
            HashMap<String, Object> parameters = getUserParameter(event.getUser());
            String text = templateService.renderTemplate(USER_LEAVE_TEMPLATE, parameters);
            textChannelById.sendMessage(text).queue();
        } else {
            log.warn("{} post target is not defined for server {}", PostTarget.LEAVE_LOG, server.getId());
        }
    }
}
