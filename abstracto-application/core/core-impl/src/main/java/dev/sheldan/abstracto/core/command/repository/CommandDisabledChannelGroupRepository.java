package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.model.database.CommandDisabledChannelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandDisabledChannelGroupRepository extends JpaRepository<CommandDisabledChannelGroup, Long> {
}
