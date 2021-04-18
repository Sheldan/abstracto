package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.model.database.CoolDownChannelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoolDownChannelGroupRepository extends JpaRepository<CoolDownChannelGroup, Long> {
}
