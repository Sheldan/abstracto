package dev.sheldan.abstracto.templating.service.management;

import dev.sheldan.abstracto.templating.model.database.AutoLoadMacro;

import java.util.List;

public interface AutoLoadMacroManagementService {
    List<AutoLoadMacro> loadAllMacros();
}
