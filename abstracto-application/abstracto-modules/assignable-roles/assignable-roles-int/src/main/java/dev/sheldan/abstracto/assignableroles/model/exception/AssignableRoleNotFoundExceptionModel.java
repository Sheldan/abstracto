package dev.sheldan.abstracto.assignableroles.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class AssignableRoleNotFoundExceptionModel implements Serializable {
    private Long roleId;
    private String displayText;
}
