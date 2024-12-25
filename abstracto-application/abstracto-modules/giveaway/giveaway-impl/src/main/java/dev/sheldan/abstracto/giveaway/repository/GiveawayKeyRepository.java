package dev.sheldan.abstracto.giveaway.repository;

import dev.sheldan.abstracto.giveaway.model.database.GiveawayKey;
import dev.sheldan.abstracto.giveaway.model.database.embed.GiveawayKeyId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiveawayKeyRepository extends JpaRepository<GiveawayKey, GiveawayKeyId> {
    List<GiveawayKey> findGiveawayKeysByUsedAndServer_IdOrderById(Boolean used, Long serverId);
    List<GiveawayKey> findGiveawayKeysByServer_IdOrderById(Long serverId);
}
