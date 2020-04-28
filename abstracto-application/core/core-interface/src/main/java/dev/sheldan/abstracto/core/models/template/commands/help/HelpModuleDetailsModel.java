package dev.sheldan.abstracto.core.models.template.commands.help;

import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.config.SingleLevelPackedModule;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class HelpModuleDetailsModel extends UserInitiatedServerContext {
    private SingleLevelPackedModule module;
    private List<ModuleInterface> subModules;
}
