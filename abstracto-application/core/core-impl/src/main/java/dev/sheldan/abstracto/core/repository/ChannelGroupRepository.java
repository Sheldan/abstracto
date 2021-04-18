package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelGroupRepository extends JpaRepository<AChannelGroup, Long> {

    Optional<AChannelGroup> findByGroupNameIgnoreCaseAndServer(String name, AServer server);

    Optional<AChannelGroup> findByGroupNameAndServerAndChannelGroupType_GroupTypeKey(String name, AServer server, String groupTyeKey);

    List<AChannelGroup> findByServerAndChannelGroupType_GroupTypeKey(AServer server, String groupTyeKey);

    List<AChannelGroup> findByServer(AServer server);

    boolean existsByGroupNameIgnoreCaseAndServer(String name, AServer server);

    List<AChannelGroup> findAllByChannels(AChannel channel);
}
