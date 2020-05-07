package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadState;

import java.util.List;

public interface ModMailThreadManagementService {
    ModMailThread getByChannelId(Long channelId);
    ModMailThread getById(Long modMailThreadId);
    ModMailThread getByChannel(AChannel channel);
    List<ModMailThread> getThreadByUserAndState(AUserInAServer userInAServer, ModMailThreadState state);
    ModMailThread getOpenModmailThreadForUser(AUserInAServer userInAServer);
    ModMailThread getOpenModmailThreadForUser(AUser user);
    List<ModMailThread> getModMailThreadForUser(AUserInAServer aUserInAServer);
    ModMailThread getLatestModMailThread(AUserInAServer aUserInAServer);
    void createModMailThread(AUserInAServer userInAServer, AChannel channel);
    void setModMailThreadState(ModMailThread modMailThread, ModMailThreadState newState);

}
