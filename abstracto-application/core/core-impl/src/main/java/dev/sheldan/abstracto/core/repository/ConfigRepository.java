package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

public interface ConfigRepository extends JpaRepository<AConfig, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    AConfig findAConfigByServerIdAndName(Long serverId, String name);
}
