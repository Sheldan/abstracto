package dev.sheldan.abstracto.core.interactive.setup.action.config;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.models.template.commands.PostTargetActionModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostTargetDelayedActionConfig implements DelayedActionConfig {

    private String postTargetKey;
    private Long serverId;
    private Long channelId;

    @Override
    public String getTemplateName() {
        return "feature_setup_post_target_action";
    }

    @Override
    public Object getTemplateModel() {
        return PostTargetActionModel
                .builder()
                .channelId(channelId)
                .postTargetKey(postTargetKey)
                .build();
    }
}
