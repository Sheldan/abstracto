package dev.sheldan.abstracto.repostdetection.repository;

import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepostCheckChannelGroupRepository extends JpaRepository<RepostCheckChannelGroup, Long> {
}
