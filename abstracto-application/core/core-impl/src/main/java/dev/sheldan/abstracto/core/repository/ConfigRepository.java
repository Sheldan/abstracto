package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AConfig;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ConfigRepository extends JpaRepository<AConfig, Long> {

    AConfig findAConfigByServerIdAndNameIgnoreCase(Long serverId, String name);
    void deleteAConfigByServerId(Long serverId);

    boolean existsAConfigByServerIdAndNameIgnoreCase(Long serverId, String name);

    boolean existsAConfigByServerAndNameIgnoreCase(AServer server, String name);
}
