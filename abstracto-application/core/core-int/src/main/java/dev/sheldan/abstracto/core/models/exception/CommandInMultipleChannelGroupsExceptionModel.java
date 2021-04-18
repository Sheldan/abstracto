package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommandInMultipleChannelGroupsExceptionModel {
    private String channelGroupName;
}
