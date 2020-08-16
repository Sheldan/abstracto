package dev.sheldan.abstracto.templating.service.management;

import dev.sheldan.abstracto.templating.model.database.AutoLoadMacro;
import dev.sheldan.abstracto.templating.repository.AutoLoadMacroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AutoLoadMacroManagementServiceBean implements AutoLoadMacroManagementService {

    @Autowired
    private AutoLoadMacroRepository repository;

    @Override
    public List<AutoLoadMacro> loadAllMacros() {
        return repository.findAll();
    }
}
