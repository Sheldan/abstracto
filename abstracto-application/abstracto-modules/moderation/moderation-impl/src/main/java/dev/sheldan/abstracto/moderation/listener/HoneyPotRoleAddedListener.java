package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.sync.jda.RoleAddedListener;
import dev.sheldan.abstracto.core.models.listener.RoleAddedModel;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class HoneyPotRoleAddedListener implements RoleAddedListener {

    @Autowired
    private RoleService roleService;

    @Autowired
    private HoneyPotServiceBean honeyPotServiceBean;

    @Override
    public DefaultListenerResult execute(RoleAddedModel model) {
        Long honeyPotRoleId = honeyPotServiceBean.getHoneyPotRoleId(model.getServerId());
        if(honeyPotRoleId == 0) {
            log.info("Server {} has honeypot feature enabled, but still default honeypot role config - Ignoring.", model.getServerId());
            return DefaultListenerResult.IGNORED;
        }
        if(honeyPotRoleId.equals(model.getRoleId())) {
            boolean fellIntoHoneyPot = honeyPotServiceBean.fellIntoHoneyPot(model.getServerId(), model.getTargetMember());
            if (fellIntoHoneyPot) {
                log.info("Banning user {} in guild {} due to role {}.", model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), model.getRoleId());
                honeyPotServiceBean.banForHoneyPot(model.getTargetMember(), model.getRole());
            } else {
                log.info("User {} in server {}  will not get banned by honeypot. All existing roles besides {} will be removed.",
                        model.getTargetUser().getUserId(), model.getTargetUser().getServerId(), honeyPotRoleId);
                cleanupRolesBesidesHoneyPot(model, honeyPotRoleId);
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

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.HONEYPOT;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

}
