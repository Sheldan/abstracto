package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<AConfig, Long> {
    AConfig findAConfigByServerIdAndName(Long serverId, String name);
}
