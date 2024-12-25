package dev.sheldan.abstracto.giveaway.service.management;

import dev.sheldan.abstracto.giveaway.model.database.GiveawayKey;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Member;

public interface GiveawayKeyManagementService {
  GiveawayKey createGiveawayKey(Member creator, Member benefactor, String key, String description, String name);
  void deleteById(Long id, Long serverId);
  Optional<GiveawayKey> getById(Long id, Long serverId);
  GiveawayKey saveGiveawayKey(GiveawayKey giveawayKey);
  List<GiveawayKey> getGiveawayKeys(Long serverId, Boolean ignoreUsedFlag);
}
