package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.moderation.model.database.InfractionParameter;
import dev.sheldan.abstracto.moderation.model.database.embedded.InfractionParameterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfractionParameterRepository extends JpaRepository<InfractionParameter, InfractionParameterId> {
}
