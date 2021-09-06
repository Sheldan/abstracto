package dev.sheldan.abstracto.invitefilter.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.invitefilter.model.database.AllowedInviteLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllowedInviteLinkRepository extends JpaRepository<AllowedInviteLink, Long> {

    Optional<AllowedInviteLink> findByTargetServerIdAndServer(Long targetServerId, AServer server);

    Optional<AllowedInviteLink> findByTargetServerIdAndServer_Id(Long targetServerId, Long serverId);
    Optional<AllowedInviteLink> findByCodeIgnoreCaseAndServer_Id(String code, Long serverId);
}
