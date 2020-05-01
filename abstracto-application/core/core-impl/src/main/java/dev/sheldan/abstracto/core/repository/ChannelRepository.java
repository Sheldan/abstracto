package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<AChannel, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<AChannel> findAll();
}
