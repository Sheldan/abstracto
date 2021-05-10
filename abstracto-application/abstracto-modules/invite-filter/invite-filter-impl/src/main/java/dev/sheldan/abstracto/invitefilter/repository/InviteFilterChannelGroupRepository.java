package dev.sheldan.abstracto.invitefilter.repository;

import dev.sheldan.abstracto.invitefilter.model.database.InviteFilterChannelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteFilterChannelGroupRepository extends JpaRepository<InviteFilterChannelGroup, Long> {
}
