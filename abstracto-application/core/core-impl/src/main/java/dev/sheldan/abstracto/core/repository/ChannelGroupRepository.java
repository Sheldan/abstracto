package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelGroupRepository extends JpaRepository<AChannelGroup, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<AChannelGroup> findByGroupNameAndServer(String name, AServer server);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<AChannelGroup> findByGroupNameAndServerAndChannelGroupType_GroupTypeKey(String name, AServer server, String groupTyeKey);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<AChannelGroup> findByServerAndChannelGroupType_GroupTypeKey(AServer server, String groupTyeKey);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<AChannelGroup> findByServer(AServer server);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByGroupNameAndServer(String name, AServer server);

    List<AChannelGroup> findAllByChannels(AChannel channel);
}
