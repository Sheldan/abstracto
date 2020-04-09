package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.AChannelGroup;
import dev.sheldan.abstracto.core.models.AServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelGroupRepository extends JpaRepository<AChannelGroup, Long> {
    AChannelGroup findByGroupNameAndServer(String name, AServer server);
    List<AChannelGroup> findByServer(AServer server);
}
