package dev.sheldan.abstracto.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<AChannel, Long> {
    List<AChannel> findAll();
}
