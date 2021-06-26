package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.service.JSONValidationService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.everit.json.schema.ValidationException;

import java.util.List;

@Getter
@Setter
@Builder
public class JSONValidationResult {
    private JSONValidationService.Result result;
    private List<ValidationException> exceptions;
}


