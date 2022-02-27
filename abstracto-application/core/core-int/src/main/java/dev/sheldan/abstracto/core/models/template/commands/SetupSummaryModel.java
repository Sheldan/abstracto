package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfigContainer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SetupSummaryModel {
    private List<DelayedActionConfigContainer> actionConfigs;
    private String confirmButtonId;
    private String cancelButtonId;
}
