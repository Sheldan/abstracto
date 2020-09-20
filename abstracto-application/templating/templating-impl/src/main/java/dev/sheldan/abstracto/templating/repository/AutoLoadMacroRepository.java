package dev.sheldan.abstracto.templating.repository;

import dev.sheldan.abstracto.templating.model.database.AutoLoadMacro;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface AutoLoadMacroRepository extends JpaRepository<AutoLoadMacro, String> {
    @NotNull
    @Override
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<AutoLoadMacro> findAll();
}
