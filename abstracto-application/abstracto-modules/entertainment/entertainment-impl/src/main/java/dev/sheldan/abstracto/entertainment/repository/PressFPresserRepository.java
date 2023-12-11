package dev.sheldan.abstracto.entertainment.repository;

import dev.sheldan.abstracto.entertainment.model.database.PressFPresser;
import dev.sheldan.abstracto.entertainment.model.database.embed.PressFPresserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PressFPresserRepository extends JpaRepository<PressFPresser, PressFPresserId> {
}
