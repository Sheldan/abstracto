package dev.sheldan.abstracto.core.command.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class ChannelGroupExistsExceptionModel implements Serializable {
    private String name;
}
