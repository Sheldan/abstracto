package dev.sheldan.abstracto.templating.repository;

import dev.sheldan.abstracto.templating.model.database.AutoLoadMacro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoLoadMacroRepository extends JpaRepository<AutoLoadMacro, String> {
}
