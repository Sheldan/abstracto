package dev.sheldan.abstracto.core.models.provider;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChannelGroupInformationRequest implements InformationRequest {
    private Long channelGroupId;
    private String channelGroupType;
}
