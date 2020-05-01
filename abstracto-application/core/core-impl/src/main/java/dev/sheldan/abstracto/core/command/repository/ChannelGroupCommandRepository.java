package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.List;

public interface ChannelGroupCommandRepository extends JpaRepository<AChannelGroupCommand, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    AChannelGroupCommand findByCommandAndGroup(ACommand command, AChannelGroup group);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<AChannelGroupCommand> findByCommand(ACommand command);
}
