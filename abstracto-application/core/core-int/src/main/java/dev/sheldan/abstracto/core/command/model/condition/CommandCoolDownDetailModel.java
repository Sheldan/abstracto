package dev.sheldan.abstracto.core.command.model.condition;

import dev.sheldan.abstracto.core.command.execution.CoolDownCheckResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class CommandCoolDownDetailModel implements Serializable {
    private CoolDownCheckResult reason;
}
