package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelGroupCommandRepository extends JpaRepository<AChannelGroupCommand, Long> {

    Optional<AChannelGroupCommand> findByCommandAndGroup(ACommand command, AChannelGroup group);

    List<AChannelGroupCommand> findByCommand(ACommand command);
}
