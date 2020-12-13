package dev.sheldan.abstracto.utility.models.template.commands;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class LoveCalcResponseModel extends SlimUserInitiatedServerContext {
    private String firstPart;
    private String secondPart;
    private Integer rolled;
}
