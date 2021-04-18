package dev.sheldan.abstracto.core.models.provider;

import dev.sheldan.abstracto.core.models.template.provider.CoolDownChannelInformationModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Setter
@Getter
@Builder
public class CoolDownChannelInformation implements ChannelGroupInformation {
    private Long channelCoolDown;
    private Long memberCoolDown;

    @Override
    public String getTemplateName() {
        return "channel_group_provider_cool_down_display";
    }

    @Override
    public Object getTemplateModel() {
        return CoolDownChannelInformationModel
                .builder()
                .channelCoolDown(Duration.ofSeconds(this.channelCoolDown))
                .memberCoolDown(Duration.ofSeconds(this.memberCoolDown))
                .build();
    }
}
