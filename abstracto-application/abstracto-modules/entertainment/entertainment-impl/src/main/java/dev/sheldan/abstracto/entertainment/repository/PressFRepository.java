package dev.sheldan.abstracto.entertainment.repository;

import dev.sheldan.abstracto.entertainment.model.database.PressF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PressFRepository extends JpaRepository<PressF, Long> {
}
