package dev.sheldan.abstracto.core.templating.service.management;


import dev.sheldan.abstracto.core.templating.model.database.AutoLoadMacro;

import java.util.List;

public interface AutoLoadMacroManagementService {
    List<AutoLoadMacro> loadAllMacros();
}
