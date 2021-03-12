package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.model.database.FilteredInviteLink;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilteredInviteLinkRepository extends JpaRepository<FilteredInviteLink, Long> {
    Optional<FilteredInviteLink> findByCodeAndServer(String code, AServer server);

    Optional<FilteredInviteLink> findByCodeAndServer_Id(String code, Long serverId);

    void deleteByServer_Id(Long serverId);

    void deleteByCodeAndServer_Id(String code, Long serverId);

    List<FilteredInviteLink> findAllByServer_IdOrderByUsesDesc(Long serverId, Pageable pageable);
}
