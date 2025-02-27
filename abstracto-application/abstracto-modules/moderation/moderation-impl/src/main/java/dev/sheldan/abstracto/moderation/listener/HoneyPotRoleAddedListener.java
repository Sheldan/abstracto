package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.sync.jda.RoleAddedListener;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.RoleAddedModel;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.ConditionService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.SystemCondition;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.feature.HoneyPotFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.listener.HoneyPotReasonModel;
import dev.sheldan.abstracto.moderation.service.BanService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private RoleService roleService;

    private static final String HONEYPOT_BAN_REASON_TEMPLATE = "honeypot_ban_reason";

    private static final String LEVEL_CONDITION_USER_ID_PARAMETER = "userId";
    private static final String LEVEL_CONDITION_LEVEL_PARAMETER = "level";
    private static final String LEVEL_CONDITION_SERVER_PARAMETER = "serverId";

    private static final String LEVEL_CONDITION_NAME = "HAS_LEVEL";

    @Override
    public DefaultListenerResult execute(RoleAddedModel model) {
        Long honeyPotRoleId = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_ROLE_ID, model.getServerId());
        if(honeyPotRoleId == 0) {
            log.info("Server {} has honeypot feature enabled, but still default honeypot role config - Ignoring.", model.getServerId());
            return DefaultListenerResult.IGNORED;
        }
        if(honeyPotRoleId.equals(model.getRoleId())) {
            Integer levelToSkipBan = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_IGNORED_LEVEL, model.getServerId()).intValue();
            Long amountOfSecondsToIgnore = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_IGNORED_JOIN_DURATION_SECONDS, model.getServerId());
            boolean allowed = userHasLevel(model.getTargetMember(), levelToSkipBan) || userJoinedLongerThanSeconds(model.getTargetMember(), amountOfSecondsToIgnore);
            if(allowed) {
                log.info("User {} in server {} has at least level {} or joined more than {} seconds ago and will not get banned by honeypot. All existing roles besides {} will be removed.",
                        model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), levelToSkipBan, amountOfSecondsToIgnore, honeyPotRoleId);
                cleanupRolesBesidesHoneyPot(model, honeyPotRoleId);
            } else  {
                log.info("Banning user {} in guild {} due to role {}.", model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), model.getRoleId());
                HoneyPotReasonModel reasonModel = HoneyPotReasonModel
                        .builder()
                        .memberDisplay(MemberDisplay.fromMember(model.getTargetMember()))
                        .roleDisplay(RoleDisplay.fromRole(model.getRole()))
                        .build();
                String banReason = templateService.renderTemplate(HONEYPOT_BAN_REASON_TEMPLATE, reasonModel, model.getServerId());
                banService.banUserWithNotification(model.getTargetUser(), banReason, ServerUser.fromMember(model.getTargetMember().getGuild().getSelfMember()),
                        model.getTargetMember().getGuild(), Duration.ofDays(7)).thenAccept(banResult -> {
                    log.info("Banned user {} in guild {} due to role {}.", model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), model.getRoleId());
                }).exceptionally(throwable -> {
                    log.error("Failed to ban user {} in guild {} due to role {}.", model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), model.getRoleId(), throwable);
                    return null;
                });
            }
            return DefaultListenerResult.PROCESSED;
        } else {
            boolean targetMemberHasHoneypotRole = model.getTargetMember().getRoles().stream().anyMatch(role -> role.getIdLong() == honeyPotRoleId);
            log.info("User {} in server {} received another role, which was not honeypot role -> remove all other roles.", model.getTargetUser().getUserId(), model.getTargetUser().getServerId());
            if(targetMemberHasHoneypotRole) {
                cleanupRolesBesidesHoneyPot(model, honeyPotRoleId);
            }
        }
        return DefaultListenerResult.IGNORED;
    }

    private void cleanupRolesBesidesHoneyPot(RoleAddedModel model, Long roleId) {
        List<Long> rolesToRemove = model
            .getTargetMember()
            .getRoles()
            .stream().map(ISnowflake::getIdLong)
            .filter(idLong -> !idLong.equals(roleId))
            .collect(Collectors.toList());
        if(!rolesToRemove.isEmpty()) {
            roleService.updateRolesIds(model.getTargetMember(), rolesToRemove, new ArrayList<>()).thenAccept(unused -> {
                log.info("Removed {} roles from user {} in server {}.", rolesToRemove.size(), model.getTargetUser().getUserId(), model.getTargetUser().getServerId());
            }).exceptionally(throwable -> {
                log.warn("Failed to cleanup roles {} from user {} in server {}.", rolesToRemove.size(), model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), throwable);
                return null;
            });
        } else {
            log.info("No other roles found.");
        }
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

    private boolean userJoinedLongerThanSeconds(Member member, Long seconds) {
        log.info("Checking if member {} joined the server more than {} seconds ago.", member.getIdLong(), seconds);
        // the incorrectness of timejoined should not matter, we chunk anyway
        return member.getTimeJoined().toInstant().isBefore(Instant.now().minus(seconds, ChronoUnit.SECONDS));
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
