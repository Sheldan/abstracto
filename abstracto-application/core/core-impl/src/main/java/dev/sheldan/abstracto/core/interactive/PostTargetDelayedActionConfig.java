package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.models.template.commands.PostTargetActionModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@Builder
public class PostTargetDelayedActionConfig implements DelayedActionConfig {

    private String postTargetKey;
    private Long serverId;
    private Long channelId;
    private TextChannel textChannel;

    @Override
    public String getTemplateName() {
        return "feature_setup_post_target_action";
    }

    @Override
    public Object getTemplateModel() {
        return PostTargetActionModel
                .builder()
                .channelId(channelId)
                .channel(textChannel)
                .postTargetKey(postTargetKey)
                .build();
    }
}
