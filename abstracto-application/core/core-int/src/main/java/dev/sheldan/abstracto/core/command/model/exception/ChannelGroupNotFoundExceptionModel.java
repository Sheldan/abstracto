package dev.sheldan.abstracto.core.command.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class ChannelGroupNotFoundExceptionModel implements Serializable {
    private String name;
    private List<String> available;
}
