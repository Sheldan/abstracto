package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.JoinListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class JoinLogger implements JoinListener {

    public static final String USER_JOIN_TEMPLATE = "user_join";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;


    private HashMap<String, Object> getUserParameter(User user) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("user", user);
        return parameters;
    }

    @Override
    public void execute(Member member, Guild guild, AUserInAServer aUserInAServer) {
        log.info("User {} joined server {}.", aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        HashMap<String, Object> parameters = getUserParameter(member.getUser());
        String text = templateService.renderTemplateWithMap(USER_JOIN_TEMPLATE, parameters);
        postTargetService.sendTextInPostTarget(text, LoggingPostTarget.JOIN_LOG, guild.getIdLong());
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.LOGGING;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
