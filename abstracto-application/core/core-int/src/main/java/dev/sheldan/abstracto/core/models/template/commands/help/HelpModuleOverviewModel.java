package dev.sheldan.abstracto.core.models.template.commands.help;

import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class HelpModuleOverviewModel {
    private List<ModuleDefinition> modules;
}
