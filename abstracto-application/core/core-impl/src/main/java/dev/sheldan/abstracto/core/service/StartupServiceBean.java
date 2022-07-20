package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StartupServiceBean implements Startup {

    @Autowired
    private BotService service;

    @Autowired
    private List<? extends  ListenerAdapter> listeners;

    @Autowired(required = false)
    private List<AsyncStartupListener> startupListeners;

    @Autowired
    private StartupServiceBean self;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private ProfanityService profanityService;

    @Override
    public void startBot() throws LoginException {
        service.login();
        listeners.forEach(o -> service.getInstance().addEventListener(o));
    }

    @Override
    @Transactional
    public void synchronize() {
        log.info("Synchronizing servers.");
        synchronizeServers();
        log.info("Done synchronizing servers");
        executeStartUpListeners();
        profanityService.reloadRegex();
    }


    private void executeStartUpListeners() {
        if(startupListeners == null) {
            return;
        }
        log.info("Executing {} startup listeners.", startupListeners.size());
        startupListeners.forEach(asyncStartupListener ->
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("Executing startup listener {}.", asyncStartupListener);
                    self.executeStartupListener(asyncStartupListener);
                } catch (Exception e) {
                    log.error("Startup listener {} failed.", asyncStartupListener, e);
                }
            }).thenAccept(unused -> log.info("Startup listener {} finished.", asyncStartupListener))
            .exceptionally(throwable -> {
                log.error("Startup listener {} failed.", asyncStartupListener, throwable);
                return null;
            })
        );
    }

    @Transactional
    public void executeStartupListener(AsyncStartupListener startupListener) {
        startupListener.execute();
    }

    private void synchronizeServers(){
        JDA instance = service.getInstance();
        List<Guild> onlineGuilds = instance.getGuilds();
        Set<Long> availableServers = SnowflakeUtils.getSnowflakeIds(onlineGuilds);
        List<ACommand> existingCommands = commandManagementService.getAllCommands();
        availableServers.forEach(aLong -> {
            AServer newAServer = serverManagementService.loadOrCreate(aLong);
            Guild newGuild = instance.getGuildById(aLong);
            log.info("Synchronizing server: {}", aLong);
            if(newGuild != null){
                synchronizeRolesOf(newGuild, newAServer);
                synchronizeChannelsOf(newGuild, newAServer);
                synchronizeCommandsInServer(newAServer, existingCommands);
            }
        });

    }

    private void synchronizeCommandsInServer(AServer newAServer, List<ACommand> commands) {
        commands.forEach(aCommand -> {
            if(!commandInServerManagementService.doesCommandExistInServer(aCommand, newAServer)) {
                commandInServerManagementService.createCommandInServer(aCommand, newAServer);
            }
        });
    }

    private void synchronizeRolesOf(Guild guild, AServer existingAServer){
        List<Role> guildRoles = guild.getRoles();
        List<ARole> existingRoles = existingAServer.getRoles();
        Set<Long> existingRoleIds = SnowflakeUtils.getOwnItemsIds(existingRoles);
        Set<Long> guildRoleIds = SnowflakeUtils.getSnowflakeIds(guildRoles);
        Set<Long> newRoles = SetUtils.difference(guildRoleIds, existingRoleIds);
        newRoles.forEach(roleId -> roleManagementService.createRole(roleId, existingAServer));
        Set<Long> deletedRoles = SetUtils.difference(existingRoleIds, guildRoleIds);
        deletedRoles.forEach(roleId -> roleManagementService.markDeleted(roleId));
    }

    private void synchronizeChannelsOf(Guild guild, AServer existingServer){
        List<GuildChannel> available = guild.getChannels();
        List<AChannel> knownChannels = existingServer
                .getChannels()
                .stream()
                .filter(aChannel -> !aChannel.getDeleted())
                .filter(aChannel -> !aChannel.getType().isThread())
                .collect(Collectors.toList());
        Set<Long> knownChannelsIds = SnowflakeUtils.getOwnItemsIds(knownChannels);
        Set<Long> existingChannelsIds = SnowflakeUtils.getSnowflakeIds(available);
        Set<Long> newChannels = SetUtils.difference(existingChannelsIds, knownChannelsIds);
        newChannels.forEach(channelId -> {
            GuildChannel existingChannel = available
                    .stream()
                    .filter(channel -> channel.getIdLong() == channelId)
                    .findFirst()
                    .get();
            AChannelType type = AChannelType.getAChannelType(existingChannel.getType());
            channelManagementService.createChannel(existingChannel.getIdLong(), type, existingServer);
        });
        Set<Long> noLongAvailable = SetUtils.difference(knownChannelsIds, existingChannelsIds);
        noLongAvailable.forEach(channelId ->
                channelManagementService.markAsDeleted(channelId)
        );
        List<ThreadChannel> availableThreads = new ArrayList<>();
        List<AChannel> knownThreads = existingServer
                .getChannels()
                .stream()
                .filter(aChannel -> !aChannel.getDeleted())
                .filter(aChannel -> aChannel.getType().isThread())
                .collect(Collectors.toList());
        available.stream().forEach(guildChannel -> {
            if(guildChannel instanceof IThreadContainer) {
                IThreadContainer threadContainer = (IThreadContainer) guildChannel;
                availableThreads.addAll(threadContainer.getThreadChannels());
            }
        });
        Set<Long> knownThreadIds = SnowflakeUtils.getOwnItemsIds(knownThreads);
        Set<Long> existingThreadsIds = SnowflakeUtils.getSnowflakeIds(availableThreads);
        Set<Long> newThreads = SetUtils.difference(existingThreadsIds, knownThreadIds);

        newThreads.forEach(threadId -> {
            ThreadChannel existingThread = availableThreads
                    .stream()
                    .filter(channel -> channel.getIdLong() == threadId)
                    .findFirst()
                    .get();
            IThreadContainer parentChannel = existingThread.getParentChannel();
            AChannel parentChannelObj = channelManagementService.loadChannel(parentChannel);
            AChannelType type = AChannelType.getAChannelType(existingThread.getType());
            channelManagementService.createThread(existingThread.getIdLong(), type, existingServer, parentChannelObj);
        });
        Set<Long> noLongAvailableThreads = SetUtils.difference(knownThreadIds, existingThreadsIds);
        noLongAvailableThreads.forEach(channelId ->
                channelManagementService.markAsDeleted(channelId)
        );
    }
}
