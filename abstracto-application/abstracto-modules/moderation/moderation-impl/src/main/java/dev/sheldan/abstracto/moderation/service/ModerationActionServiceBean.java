package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.ModerationActionButton;
import dev.sheldan.abstracto.moderation.model.template.listener.ModerationActionPayloadModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ModerationActionServiceBean implements ModerationActionService {

    public static final String WARN_ACTION = "warn";
    public static final String MUTE_ACTION = "mute";
    public static final String KICK_ACTION = "kick";
    public static final String BAN_ACTION = "ban";

    public static final String MODERATION_ACTION_ORIGIN = "moderationAction";


    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public List<ModerationActionButton> getModerationActionButtons(ServerUser serverUser) {
        AServer server = serverManagementService.loadServer(serverUser.getServerId());
        boolean mutingEnabled = featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.MUTING, serverUser.getServerId());
        boolean moderationEnabled = featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.MODERATION, serverUser.getServerId());
        boolean warningsEnabled = featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.WARNING, serverUser.getServerId());
        List<ModerationActionButton> buttons = new ArrayList<>();
        if(warningsEnabled) {
            String warnButtonId = componentService.generateComponentId();
            ModerationActionPayloadModel warnPayload = ModerationActionPayloadModel.forAction(WARN_ACTION, serverUser);
            componentPayloadService.createButtonPayload(warnButtonId, warnPayload, MODERATION_ACTION_ORIGIN, server);
            ModerationActionButton warnAction = ModerationActionButton
                    .builder()
                    .componentId(warnButtonId)
                    .action(WARN_ACTION)
                    .build();
            buttons.add(warnAction);
        }
        if(mutingEnabled) {
            String muteButtonId = componentService.generateComponentId();
            ModerationActionPayloadModel mutePayload = ModerationActionPayloadModel.forAction(MUTE_ACTION, serverUser);
            componentPayloadService.createButtonPayload(muteButtonId, mutePayload, MODERATION_ACTION_ORIGIN, server);
            ModerationActionButton muteAction = ModerationActionButton
                    .builder()
                    .componentId(muteButtonId)
                    .action(MUTE_ACTION)
                    .build();
            buttons.add(muteAction);
        }
        if(moderationEnabled) {
            String kickButtonId = componentService.generateComponentId();
            String banButtonId = componentService.generateComponentId();
            ModerationActionPayloadModel kickPayload = ModerationActionPayloadModel.forAction(KICK_ACTION, serverUser);
            ModerationActionPayloadModel banPayload = ModerationActionPayloadModel.forAction(BAN_ACTION, serverUser);
            componentPayloadService.createButtonPayload(kickButtonId, kickPayload, MODERATION_ACTION_ORIGIN, server);
            componentPayloadService.createButtonPayload(banButtonId, banPayload, MODERATION_ACTION_ORIGIN, server);
            ModerationActionButton kickAction = ModerationActionButton
                    .builder()
                    .componentId(kickButtonId)
                    .action(KICK_ACTION)
                    .build();
            buttons.add(kickAction);
            ModerationActionButton banAction = ModerationActionButton
                    .builder()
                    .componentId(banButtonId)
                    .action(BAN_ACTION)
                    .build();
            buttons.add(banAction);
        }
        log.info("Attaching {} buttons to moderation action for user {} in server {}.", buttons.size(), serverUser.getUserId(), serverUser.getServerId());
        return buttons;
    }
}
