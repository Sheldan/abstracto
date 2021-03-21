package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncLeaveListener;
import dev.sheldan.abstracto.core.models.listener.MemberLeaveModel;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.LoggingPostTarget;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;

@Service
@Slf4j
public class LeaveLogger implements AsyncLeaveListener {

    public static final String USER_LEAVE_TEMPLATE = "user_leave";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LeaveLogger self;

    @NotNull
    private HashMap<String, Object> getUserParameter(@Nonnull User user) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("user", user);
        parameters.put("userMention",  user.getAsMention());
        return parameters;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.LOGGING;
    }

    @Override
    public DefaultListenerResult execute(MemberLeaveModel model) {
        String text = templateService.renderTemplateWithMap(USER_LEAVE_TEMPLATE, getUserParameter(model.getMember().getUser()), model.getLeavingUser().getServerId());
        postTargetService.sendTextInPostTarget(text, LoggingPostTarget.LEAVE_LOG, model.getServerId());
        return DefaultListenerResult.PROCESSED;
    }
}
