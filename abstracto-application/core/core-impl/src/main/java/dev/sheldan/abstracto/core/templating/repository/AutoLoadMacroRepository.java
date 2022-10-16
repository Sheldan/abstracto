package dev.sheldan.abstracto.core.templating.repository;

import dev.sheldan.abstracto.core.templating.model.database.AutoLoadMacro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoLoadMacroRepository extends JpaRepository<AutoLoadMacro, String> {
    @Override
    List<AutoLoadMacro> findAll();
}
