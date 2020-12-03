package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelGroupTypeRepository extends JpaRepository<ChannelGroupType, Integer> {
    Optional<ChannelGroupType> findByGroupTypeKey(String key);
}
