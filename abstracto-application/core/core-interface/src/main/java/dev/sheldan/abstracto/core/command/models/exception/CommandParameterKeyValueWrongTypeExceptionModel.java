package dev.sheldan.abstracto.core.command.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CommandParameterKeyValueWrongTypeExceptionModel {
    private List<String> expectedValues;
}
