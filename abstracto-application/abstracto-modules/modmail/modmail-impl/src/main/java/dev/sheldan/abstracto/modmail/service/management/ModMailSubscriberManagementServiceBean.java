package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadSubscriber;
import dev.sheldan.abstracto.modmail.repository.ModMailSubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
                .subscriber(aUserInAServer)
                .threadReference(modMailThread)
                .build();

        modMailSubscriberRepository.save(subscriber);
        return subscriber;
    }

    @Override
    public void removeSubscriber(AUserInAServer aUserInAServer, ModMailThread modMailThread) {
        modMailSubscriberRepository.deleteBySubscriberAndThreadReference(aUserInAServer, modMailThread);
    }
}
