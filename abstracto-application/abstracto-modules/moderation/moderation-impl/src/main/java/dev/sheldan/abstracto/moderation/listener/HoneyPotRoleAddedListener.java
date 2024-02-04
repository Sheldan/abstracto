package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.sync.jda.RoleAddedListener;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.RoleAddedModel;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.ConditionService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.SystemCondition;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.feature.HoneyPotFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.listener.HoneyPotReasonModel;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class HoneyPotRoleAddedListener implements RoleAddedListener {

    @Autowired
    private ConfigService configService;

    @Autowired
    private BanService banService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ConditionService conditionService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    private static final String HONEYPOT_BAN_REASON_TEMPLATE = "honeypot_ban_reason";

    private static final String LEVEL_CONDITION_USER_ID_PARAMETER = "userId";
    private static final String LEVEL_CONDITION_LEVEL_PARAMETER = "level";
    private static final String LEVEL_CONDITION_SERVER_PARAMETER = "serverId";

    private static final String LEVEL_CONDITION_NAME = "HAS_LEVEL";

    @Override
    public DefaultListenerResult execute(RoleAddedModel model) {
        Long roleId = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_ROLE_ID, model.getServerId());
        if(roleId == 0) {
            log.info("Server {} has honeypot feature enabled, but still default honeypot role config - Ignoring.", model.getServerId());
            return DefaultListenerResult.IGNORED;
        }
        if(roleId.equals(model.getRoleId())) {
            Integer levelToSkipBan = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_IGNORED_LEVEL, model.getServerId()).intValue();
            boolean allowed = userHasLevel(model.getTargetMember(), levelToSkipBan);
            if(allowed) {
                log.info("User {} in server {} has at least level {} and will not get banned by honey pot.",
                        model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), levelToSkipBan);
            } else  {
                log.info("Banning user {} in guild {} due to role {}.", model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), model.getRoleId());
                HoneyPotReasonModel reasonModel = HoneyPotReasonModel
                        .builder()
                        .memberDisplay(MemberDisplay.fromMember(model.getTargetMember()))
                        .roleDisplay(RoleDisplay.fromRole(model.getRole()))
                        .build();
                String banReason = templateService.renderTemplate(HONEYPOT_BAN_REASON_TEMPLATE, reasonModel);
                banService.banUserWithNotification(model.getTargetMember().getUser(), banReason, model.getTargetMember().getGuild().getSelfMember(), null).thenAccept(banResult -> {
                    log.info("Banned user {} in guild {} due to role {}.", model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), model.getRoleId());
                }).exceptionally(throwable -> {
                    log.error("Failed to ban user {} in guild {} due to role {}.", model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), model.getRoleId(), throwable);
                    return null;
                });
            }
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }

    private boolean userHasLevel(Member member, Integer level) {
        log.info("Checking if member {} is ignored to click on the honeypot in server {}.", member.getIdLong(),member.getGuild().getIdLong());
        Map<String, Object> parameters = new HashMap<>();
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
        parameters.put(LEVEL_CONDITION_USER_ID_PARAMETER, userInAServer.getUserInServerId());
        parameters.put(LEVEL_CONDITION_LEVEL_PARAMETER, level);
        parameters.put(LEVEL_CONDITION_SERVER_PARAMETER, member.getGuild().getIdLong());
        ConditionContextInstance contextInstance = ConditionContextInstance
                .builder()
                .conditionName(LEVEL_CONDITION_NAME)
                .parameters(parameters)
                .build();
        SystemCondition.Result result = conditionService.checkConditions(contextInstance);
        return SystemCondition.Result.isSuccessful(result);
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.HONEYPOT;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

}
