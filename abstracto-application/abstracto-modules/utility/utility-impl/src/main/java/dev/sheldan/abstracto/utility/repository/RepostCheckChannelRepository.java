package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.database.RepostCheckChannelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepostCheckChannelRepository extends JpaRepository<RepostCheckChannelGroup, Long> {
}
