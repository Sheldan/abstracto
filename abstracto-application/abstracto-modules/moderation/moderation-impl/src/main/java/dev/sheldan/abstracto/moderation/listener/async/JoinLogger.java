package dev.sheldan.abstracto.moderation.listener.async;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncJoinListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void execute(ServerUser serverUser) {
        log.info("User {} joined server {}.", serverUser.getUserId(), serverUser.getServerId());
        memberService.getMemberInServerAsync(serverUser.getServerId(), serverUser.getUserId()).thenAccept(member ->
            self.sendJoinLog(serverUser, member)
        );
    }

    @Transactional
    public void sendJoinLog(ServerUser serverUser, Member member) {
        HashMap<String, Object> parameters = getUserParameter(member.getUser());
        String text = templateService.renderTemplateWithMap(USER_JOIN_TEMPLATE, parameters, serverUser.getServerId());
        postTargetService.sendTextInPostTarget(text, LoggingPostTarget.JOIN_LOG, serverUser.getServerId());
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.LOGGING;
    }

}
