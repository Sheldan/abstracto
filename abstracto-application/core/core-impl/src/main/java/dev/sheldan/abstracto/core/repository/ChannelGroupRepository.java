package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelGroupRepository extends JpaRepository<AChannelGroup, Long> {
    AChannelGroup findByGroupName(String name);
}
