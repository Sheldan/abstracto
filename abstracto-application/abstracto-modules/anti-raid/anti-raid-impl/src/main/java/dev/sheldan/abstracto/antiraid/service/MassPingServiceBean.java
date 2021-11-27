package dev.sheldan.abstracto.antiraid.service;

import dev.sheldan.abstracto.antiraid.config.AntiRaidPostTarget;
import dev.sheldan.abstracto.antiraid.model.MassPingNotificationModel;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ConditionService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.SystemCondition;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.service.MuteService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MassPingServiceBean implements MassPingService {

    private static final String MASS_PING_MUTE_NOTIFICATION_TEMPLATE_KEY = "massPing_mute_notification";

    private static final String LEVEL_CONDITION_NAME = "HAS_LEVEL";
    private static final String LEVEL_CONDITION_USER_ID_PARAMETER = "userId";
    private static final String LEVEL_CONDITION_LEVEL_PARAMETER = "level";
    private static final String LEVEL_CONDITION_SERVER_PARAMETER = "serverId";

    @Autowired
    private ConfigService configService;

    @Autowired
    private MuteService muteService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MassPingServiceBean self;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private ConditionService conditionService;

    @Value("${abstracto.massPing.maxAllowedMentions}")
    private Integer maxAllowedMentions;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public CompletableFuture<Void> processMessage(Message message) {
        if(message.getMentionedMembers().size() > maxAllowedMentions) {
            Integer level = configService.getLongValueOrConfigDefault(MassPingService.MAX_AFFECTED_LEVEL_KEY, message.getGuild().getIdLong()).intValue();
            boolean allowed = allowedToMassMention(message, level);
            if(!allowed) {
                return muteService.muteMemberWithoutContext(message.getMember())
                        .thenAccept(unused -> self.sendMassPingMuteNotification(message))
                        .thenAccept(unused -> log.info("Muted member {} in server {} because of too many member mentions. (> {}).",
                                message.getMember().getIdLong(), message.getGuild().getIdLong(), maxAllowedMentions));
            } else {
                log.info("User {} in server {} is allowed to mass mention, because of level (or lack of level configuration).",
                        message.getMember().getIdLong(), message.getGuild().getIdLong());
                return CompletableFuture.completedFuture(null);
            }
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private boolean allowedToMassMention(Message message, Integer level) {
        log.info("Checking if member {} is allowed to mention a lot of users in server {}.", message.getAuthor().getIdLong(), message.getGuild().getIdLong());
        Map<String, Object> parameters = new HashMap<>();
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(message.getMember());
        parameters.put(LEVEL_CONDITION_USER_ID_PARAMETER, userInAServer.getUserInServerId());
        parameters.put(LEVEL_CONDITION_LEVEL_PARAMETER, level);
        parameters.put(LEVEL_CONDITION_SERVER_PARAMETER, message.getGuild().getIdLong());
        ConditionContextInstance contextInstance = ConditionContextInstance
                .builder()
                .conditionName(LEVEL_CONDITION_NAME)
                .parameters(parameters)
                .build();
        SystemCondition.Result result = conditionService.checkConditions(contextInstance);
        return SystemCondition.Result.consideredSuccessful(result);
    }

    @Transactional
    public CompletableFuture<Void> sendMassPingMuteNotification(Message message) {
        Member member = message.getMember();
        MassPingNotificationModel model = MassPingNotificationModel
                .builder()
                .messageLink(message.getJumpUrl())
                .mentionCount(message.getMentionedMembers().size())
                .messageContent(message.getContentRaw())
                .memberDisplay(MemberDisplay.fromMember(member))
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MASS_PING_MUTE_NOTIFICATION_TEMPLATE_KEY, model, member.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, AntiRaidPostTarget.MASS_PING_LOG, member.getGuild().getIdLong()));
    }
}
