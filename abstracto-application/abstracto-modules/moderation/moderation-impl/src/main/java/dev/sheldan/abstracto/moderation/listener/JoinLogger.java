package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncJoinListener;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.LoggingPostTarget;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class JoinLogger implements AsyncJoinListener {

    public static final String USER_JOIN_TEMPLATE = "user_join";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JoinLogger self;

    private HashMap<String, Object> getUserParameter(User user) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("user", user);
        return parameters;
    }

    @Override
    public DefaultListenerResult execute(MemberJoinModel serverUser) {
        HashMap<String, Object> parameters = getUserParameter(serverUser.getMember().getUser());
        String text = templateService.renderTemplateWithMap(USER_JOIN_TEMPLATE, parameters, serverUser.getServerId());
        postTargetService.sendTextInPostTarget(text, LoggingPostTarget.JOIN_LOG, serverUser.getServerId());
        return DefaultListenerResult.PROCESSED;
    }


    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.LOGGING;
    }

}
