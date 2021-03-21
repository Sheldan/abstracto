package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AllowedMention;
import dev.sheldan.abstracto.core.repository.AllowedMentionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AllowedMentionManagementServiceBean implements AllowedMentionManagementService {

    @Autowired
    private AllowedMentionRepository allowedMentionRepository;

    @Autowired
    private ServerManagementService serverManagementServiceBean;

    @Override
    public Optional<AllowedMention> getCustomAllowedMentionFor(Long serverId) {
        return allowedMentionRepository.findById(serverId);
    }

    @Override
    public Optional<AllowedMention> getCustomAllowedMentionFor(AServer server) {
        return allowedMentionRepository.findByServer(server);
    }

    @Override
    public boolean hasCustomAllowedMention(Long serverId) {
        return getCustomAllowedMentionFor(serverId).isPresent();
    }

    @Override
    public AllowedMention createCustomAllowedMention(Long serverId, AllowedMention base) {
        log.info("Creating custom allowed mention for server {} based on {}.", serverId, base);
        AServer server = serverManagementServiceBean.loadOrCreate(serverId);
        AllowedMention allowedMention = AllowedMention
                .builder()
                .everyone(base.getEveryone())
                .role(base.getRole())
                .user(base.getUser())
                .server(server).build();
        return allowedMentionRepository.save(allowedMention);
    }

    @Override
    public void deleteCustomAllowedMention(Long serverId) {
        allowedMentionRepository.deleteById(serverId);
    }
}
