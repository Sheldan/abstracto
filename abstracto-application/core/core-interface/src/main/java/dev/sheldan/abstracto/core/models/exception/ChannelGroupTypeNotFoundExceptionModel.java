package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
public class ChannelGroupTypeNotFoundExceptionModel implements Serializable {
    @Builder.Default
    private List<String> availableGroupTypeKeys = new ArrayList<>();
}
