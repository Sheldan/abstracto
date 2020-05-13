package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.repository.ModMailThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ModMailThreadManagementServiceBean implements ModMailThreadManagementService {

    @Autowired
    private ModMailThreadRepository modMailThreadRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public ModMailThread getByChannelId(Long channelId) {
        Optional<AChannel> channelOpt = channelManagementService.loadChannel(channelId);
        AChannel channel = channelOpt.orElseThrow(() -> new ChannelNotFoundException(channelId, 0L));
        return getByChannel(channel);
    }

    @Override
    public Optional<ModMailThread> getById(Long modMailThreadId) {
        return modMailThreadRepository.findById(modMailThreadId);
    }

    @Override
    public ModMailThread getByChannel(AChannel channel) {
        return modMailThreadRepository.findByChannel(channel);
    }

    @Override
    public List<ModMailThread> getThreadByUserAndState(AUserInAServer userInAServer, ModMailThreadState state) {
        return modMailThreadRepository.findByUserAndState(userInAServer, state);
    }

    @Override
    public ModMailThread getOpenModmailThreadForUser(AUserInAServer userInAServer) {
        return modMailThreadRepository.findByUserAndStateNot(userInAServer, ModMailThreadState.CLOSED);
    }

    @Override
    public ModMailThread getOpenModmailThreadForUser(AUser user) {
        return modMailThreadRepository.findByUser_UserReferenceAndStateNot(user, ModMailThreadState.CLOSED);
    }

    @Override
    public List<ModMailThread> getModMailThreadForUser(AUserInAServer aUserInAServer) {
        return modMailThreadRepository.findByUser(aUserInAServer);
    }

    @Override
    public ModMailThread getLatestModMailThread(AUserInAServer aUserInAServer) {
        return modMailThreadRepository.findTopByUserOrderByClosedDesc(aUserInAServer);
    }

    @Override
    public ModMailThread createModMailThread(AUserInAServer userInAServer, AChannel channel) {
        ModMailThread thread = ModMailThread
                .builder()
                .channel(channel)
                .created(Instant.now())
                .user(userInAServer)
                .server(userInAServer.getServerReference())
                .state(ModMailThreadState.INITIAL)
                .updated(Instant.now())
                .build();

        modMailThreadRepository.save(thread);
        return thread;
    }

    @Override
    public void setModMailThreadState(ModMailThread modMailThread, ModMailThreadState newState) {
        modMailThread.setState(newState);
        modMailThread.setUpdated(Instant.now());
        modMailThreadRepository.save(modMailThread);
    }
}
