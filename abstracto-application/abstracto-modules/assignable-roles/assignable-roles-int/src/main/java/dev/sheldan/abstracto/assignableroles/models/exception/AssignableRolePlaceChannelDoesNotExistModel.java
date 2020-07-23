package dev.sheldan.abstracto.assignableroles.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class AssignableRolePlaceChannelDoesNotExistModel implements Serializable {
    private Long channelId;
    private String placeName;
}
