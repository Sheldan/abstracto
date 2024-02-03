package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.sync.jda.RoleAddedListener;
import dev.sheldan.abstracto.core.models.listener.RoleAddedModel;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.feature.HoneyPotFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.listener.HoneyPotReasonModel;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HoneyPotRoleAddedListener implements RoleAddedListener {

    @Autowired
    private ConfigService configService;

    @Autowired
    private BanService banService;

    @Autowired
    private TemplateService templateService;

    private static final String HONEYPOT_BAN_REASON_TEMPLATE = "honeypot_ban_reason";

    @Override
    public DefaultListenerResult execute(RoleAddedModel model) {
        Long roleId = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_ROLE_ID, model.getServerId());
        if(roleId == 0) {
            log.info("Server {} has honeypot feature enabled, but still default honeypot role config - Ignoring.", model.getServerId());
            return DefaultListenerResult.IGNORED;
        }
        if(roleId.equals(model.getRoleId())) {
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
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
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
