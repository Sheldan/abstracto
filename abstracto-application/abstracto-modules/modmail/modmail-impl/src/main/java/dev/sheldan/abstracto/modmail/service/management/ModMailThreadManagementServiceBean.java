package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadChannelNotFound;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadNotFoundException;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.repository.ModMailThreadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ModMailThreadManagementServiceBean implements ModMailThreadManagementService {

    @Autowired
    private ModMailThreadRepository modMailThreadRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public ModMailThread getByChannelId(Long channelId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        return getByChannel(channel);
    }

    @Override
    public Optional<ModMailThread> getByIdOptional(Long modMailThreadId) {
        return modMailThreadRepository.findById(modMailThreadId);
    }

    @Override
    public ModMailThread getById(Long modMailThreadId) {
        return getByIdOptional(modMailThreadId).orElseThrow(() -> new ModMailThreadNotFoundException(modMailThreadId));
    }

    @Override
    public ModMailThread getByChannel(AChannel channel) {
        return modMailThreadRepository.findByChannel(channel).orElseThrow(ModMailThreadChannelNotFound::new);
    }

    @Override
    public Optional<ModMailThread> getByChannelOptional(AChannel channel) {
        return modMailThreadRepository.findByChannel(channel);
    }

    @Override
    public Optional<ModMailThread> getByChannelIdOptional(Long channelId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        return getByChannelOptional(channel);
    }

    @Override
    public List<ModMailThread> getThreadByUserAndState(AUserInAServer userInAServer, ModMailThreadState state) {
        return modMailThreadRepository.findByUserAndState(userInAServer, state);
    }

    @Override
    public ModMailThread getOpenModMailThreadForUser(AUserInAServer userInAServer) {
        return modMailThreadRepository.findByUserAndStateNot(userInAServer, ModMailThreadState.CLOSED);
    }

    @Override
    public boolean hasOpenModMailThreadForUser(AUserInAServer userInAServer) {
        return modMailThreadRepository.existsByUserAndStateNot(userInAServer, ModMailThreadState.CLOSED);
    }

    @Override
    public List<ModMailThread> getOpenModMailThreadsForUser(AUser user) {
        return modMailThreadRepository.findByUser_UserReferenceAndStateNot(user, ModMailThreadState.CLOSED);
    }

    @Override
    public boolean hasOpenModMailThread(AUser user) {
        return modMailThreadRepository.existsByUser_UserReferenceAndStateNot(user, ModMailThreadState.CLOSED);
    }

    @Override
    public List<ModMailThread> getModMailThreadForUser(AUserInAServer aUserInAServer) {
        return modMailThreadRepository.findByUser(aUserInAServer);
    }

    @Override
    public ModMailThread getLatestModMailThread(AUserInAServer aUserInAServer) {
        return modMailThreadRepository.findTopByUserOrderByClosedDesc(aUserInAServer);
    }

    /**
     * The status of the created instance is INITIAL.
     * @param userInAServer The {@link AUserInAServer} for which the thread was created for
     * @param channel An instance of {@link AChannel} in which the conversation with the member is handled
     * @return the created {@link ModMailThread} instance
     */
    @Override
    public ModMailThread createModMailThread(AUserInAServer userInAServer, AChannel channel) {
        ModMailThread thread = ModMailThread
                .builder()
                .id(channel.getId())
                .channel(channel)
                .created(Instant.now())
                .user(userInAServer)
                .server(userInAServer.getServerReference())
                .state(ModMailThreadState.INITIAL)
                .updated(Instant.now())
                .build();

        log.info("Create modmail thread in channel {} for user {} in server {}.",
                channel.getId(), userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());

        return modMailThreadRepository.save(thread);
    }

    @Override
    public void setModMailThreadState(ModMailThread modMailThread, ModMailThreadState newState) {
        modMailThread.setState(newState);
        modMailThread.setUpdated(Instant.now());
        modMailThreadRepository.save(modMailThread);
    }
}
