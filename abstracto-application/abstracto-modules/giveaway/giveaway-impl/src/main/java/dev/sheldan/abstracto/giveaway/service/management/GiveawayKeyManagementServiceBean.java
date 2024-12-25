package dev.sheldan.abstracto.giveaway.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.CounterService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.giveaway.exception.GiveawayKeyNotFoundException;
import dev.sheldan.abstracto.giveaway.model.database.GiveawayKey;
import dev.sheldan.abstracto.giveaway.model.database.embed.GiveawayKeyId;
import dev.sheldan.abstracto.giveaway.repository.GiveawayKeyRepository;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GiveawayKeyManagementServiceBean implements GiveawayKeyManagementService {

  @Autowired
  private GiveawayKeyRepository giveawayKeyRepository;

  @Autowired
  private CounterService counterService;

  @Autowired
  private UserInServerManagementService userInServerManagementService;

  public static final String GIVEAWAY_KEYS_COUNTER = "giveaway_keys";

  @Override
  public GiveawayKey createGiveawayKey(Member creator, Member benefactor, String key, String description, String name) {
    Long counterValue = counterService.getNextCounterValue(creator.getGuild().getIdLong(), GIVEAWAY_KEYS_COUNTER);
    GiveawayKeyId id = new GiveawayKeyId(counterValue, creator.getGuild().getIdLong());

    AUserInAServer creatorUser = userInServerManagementService.loadOrCreateUser(creator);
    AUserInAServer benefactorUser;
    if(benefactor != null) {
      benefactorUser = userInServerManagementService.loadOrCreateUser(benefactor);
    } else {
      benefactorUser = null;
    }

    GiveawayKey giveawayKey = GiveawayKey
        .builder()
        .id(id)
        .creator(creatorUser)
        .used(false)
        .server(creatorUser.getServerReference())
        .key(key)
        .description(description)
        .benefactor(benefactorUser)
        .name(name)
        .build();
    return giveawayKeyRepository.save(giveawayKey);
  }

  @Override
  public void deleteById(Long id, Long serverId) {
    GiveawayKey key = giveawayKeyRepository.findById(new GiveawayKeyId(id, serverId)).orElseThrow(GiveawayKeyNotFoundException::new);
    key.getGiveaway().setGiveawayKey(null);
    giveawayKeyRepository.delete(key);
  }

  @Override
  public Optional<GiveawayKey> getById(Long id, Long serverId) {
    return giveawayKeyRepository.findById(new GiveawayKeyId(id, serverId));
  }

  @Override
  public GiveawayKey saveGiveawayKey(GiveawayKey giveawayKey) {
    return giveawayKeyRepository.save(giveawayKey);
  }

  @Override
  public List<GiveawayKey> getGiveawayKeys(Long serverId, Boolean showAll) {
    if(showAll) {
      return giveawayKeyRepository.findGiveawayKeysByServer_IdOrderById(serverId);
    } else {
      return giveawayKeyRepository.findGiveawayKeysByUsedAndServer_IdOrderById(false, serverId);
    }
  }
}
