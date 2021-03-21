package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadSubscriber;
import dev.sheldan.abstracto.modmail.repository.ModMailSubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ModMailSubscriberManagementServiceBean implements ModMailSubscriberManagementService {

    @Autowired
    private ModMailSubscriberRepository modMailSubscriberRepository;

    @Override
    public List<ModMailThreadSubscriber> getSubscribersForThread(ModMailThread modMailThread) {
        return modMailSubscriberRepository.findByThreadReference(modMailThread);
    }

    @Override
    public boolean isSubscribedToThread(AUserInAServer aUserInAServer, ModMailThread modMailThread) {
        return modMailSubscriberRepository.existsBySubscriberAndThreadReference(aUserInAServer, modMailThread);
    }

    @Override
    public ModMailThreadSubscriber createSubscriber(AUserInAServer aUserInAServer, ModMailThread modMailThread) {

        ModMailThreadSubscriber subscriber = ModMailThreadSubscriber
                .builder()
                .server(modMailThread.getServer())
                .subscriber(aUserInAServer)
                .threadReference(modMailThread)
                .build();

        log.info("Creating subscription for user {} in server {} for modmail thread {}.",
                aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId(), modMailThread.getId());
        return modMailSubscriberRepository.save(subscriber);
    }

    @Override
    public void removeSubscriber(AUserInAServer aUserInAServer, ModMailThread modMailThread) {
        log.info("Un-subscribing user {} in server {} from modmail thread {}.",
                aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId(), modMailThread.getId());
        modMailSubscriberRepository.deleteBySubscriberAndThreadReference(aUserInAServer, modMailThread);
    }
}
