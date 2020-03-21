package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
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

    private static final String USER_JOIN_TEMPLATE = "user_join";
    private static final String USER_LEAVE_TEMPLATE = "user_leave";

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    @Transactional
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        String text = getRenderedEvent(event.getUser(), USER_JOIN_TEMPLATE);
        postTargetService.sendTextInPostTarget(text, PostTarget.JOIN_LOG, event.getGuild().getIdLong());
    }

    @Override
    @Transactional
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
        String text = getRenderedEvent(event.getUser(), USER_LEAVE_TEMPLATE);
        postTargetService.sendTextInPostTarget(text, PostTarget.LEAVE_LOG, event.getGuild().getIdLong());
    }

    @NotNull
    private HashMap<String, Object> getUserParameter(@Nonnull User user) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("user", user);
        parameters.put("userMention",  user.getAsMention());
        return parameters;
    }

    private String getRenderedEvent(User user, String templateName) {
        HashMap<String, Object> parameters = getUserParameter(user);
        return templateService.renderTemplate(templateName, parameters);
    }
}
