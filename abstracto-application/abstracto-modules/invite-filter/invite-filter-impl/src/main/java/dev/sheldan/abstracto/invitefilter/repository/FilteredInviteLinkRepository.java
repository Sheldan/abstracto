package dev.sheldan.abstracto.invitefilter.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.invitefilter.model.database.FilteredInviteLink;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilteredInviteLinkRepository extends JpaRepository<FilteredInviteLink, Long> {
    Optional<FilteredInviteLink> findByTargetServerIdAndServer(Long targetServerId, AServer server);

    Optional<FilteredInviteLink> findByTargetServerIdAndServer_Id(Long targetServerId, Long serverId);

    void deleteByServer_Id(Long serverId);

    void deleteByTargetServerIdAndServer_Id(Long targetServerId, Long serverId);

    List<FilteredInviteLink> findAllByServer_IdOrderByUsesDesc(Long serverId, Pageable pageable);
}
