package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.AllowedInviteLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllowedInviteLinkRepository extends JpaRepository<AllowedInviteLink, Long> {

    Optional<AllowedInviteLink> findByCodeAndServer(String code, AServer server);

    Optional<AllowedInviteLink> findByCodeAndServer_Id(String code, Long serverId);
}
