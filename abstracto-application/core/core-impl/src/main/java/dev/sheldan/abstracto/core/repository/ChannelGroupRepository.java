package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelGroupRepository extends JpaRepository<AChannelGroup, Long> {

    Optional<AChannelGroup> findByGroupNameIgnoreCaseAndServer(String name, AServer server);

    Optional<AChannelGroup> findByGroupNameIgnoreCaseAndServerAndChannelGroupType_GroupTypeKey(String name, AServer server, String groupTyeKey);

    List<AChannelGroup> findByServerAndChannelGroupType_GroupTypeKey(AServer server, String groupTyeKey);

    List<AChannelGroup> findByServer(AServer server);

    boolean existsByGroupNameIgnoreCaseAndServer(String name, AServer server);

    List<AChannelGroup> findAllByChannels(AChannel channel);

    @Query("select case when count(1) > 0 then true else false end " +
            "from AChannelGroup grp " +
            "join grp.channels ch " +
            "where grp.enabled = true " +
            " and grp.channelGroupType.groupTypeKey = ?1" +
            " and ch.id = ?2")
    boolean existsChannelInGroupOfType(String groupTypeKey, Long channelId);
}
