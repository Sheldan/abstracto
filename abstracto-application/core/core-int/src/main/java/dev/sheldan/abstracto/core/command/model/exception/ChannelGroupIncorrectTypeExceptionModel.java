package dev.sheldan.abstracto.core.command.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class ChannelGroupIncorrectTypeExceptionModel implements Serializable {
    private String name;
    private String correctType;
}
