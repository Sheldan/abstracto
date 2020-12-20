package dev.sheldan.abstracto.moderation.listener.async;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncLeaveListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private BotService botService;

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
    public void execute(ServerUser serverUser) {
        log.info("User {} left server {}.", serverUser.getUserId(), serverUser.getServerId());
        botService.getMemberInServerAsync(serverUser.getServerId(), serverUser.getUserId()).thenAccept(member ->
            self.executeJoinLogging(serverUser, member)
        );
    }

    @Transactional
    public void executeJoinLogging(ServerUser serverUser, Member member) {
        String text = templateService.renderTemplateWithMap(USER_LEAVE_TEMPLATE, getUserParameter(member.getUser()));
        postTargetService.sendTextInPostTarget(text, LoggingPostTarget.LEAVE_LOG, serverUser.getServerId());
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.LOGGING;
    }

}
