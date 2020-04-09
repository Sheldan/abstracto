package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.models.AChannelGroup;
import dev.sheldan.abstracto.core.models.AChannelGroupCommand;
import dev.sheldan.abstracto.core.models.ACommand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelGroupCommandRepository extends JpaRepository<AChannelGroupCommand, Long> {
    AChannelGroupCommand findByCommandAndGroup(ACommand command, AChannelGroup group);
    List<AChannelGroupCommand> findByCommand(ACommand command);
}
