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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
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
                    log.error("Startup listener {} failed.", asyncStartupListener);
                }
            }).thenAccept(unused -> log.info("Startup listener {} finished.", asyncStartupListener))
            .exceptionally(throwable -> {
                log.error("Startup listener {} failed.", asyncStartupListener);
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
        newRoles.forEach(aLong -> roleManagementService.createRole(aLong, existingAServer));
        Set<Long> deletedRoles = SetUtils.difference(existingRoleIds, guildRoleIds);
        deletedRoles.forEach(aLong -> roleManagementService.markDeleted(aLong));
    }

    private void synchronizeChannelsOf(Guild guild, AServer existingServer){
        List<GuildChannel> available = guild.getChannels();
        List<AChannel> knownChannels = existingServer.getChannels().stream().filter(aChannel -> !aChannel.getDeleted()).collect(Collectors.toList());
        Set<Long> knownChannelsIds = SnowflakeUtils.getOwnItemsIds(knownChannels);
        Set<Long> existingChannelsIds = SnowflakeUtils.getSnowflakeIds(available);
        Set<Long> newChannels = SetUtils.difference(existingChannelsIds, knownChannelsIds);
        newChannels.forEach(aLong -> {
            GuildChannel channel1 = available.stream().filter(channel -> channel.getIdLong() == aLong).findFirst().get();
            AChannelType type = AChannelType.getAChannelType(channel1.getType());
            channelManagementService.createChannel(channel1.getIdLong(), type, existingServer);
        });

        Set<Long> noLongAvailable = SetUtils.difference(knownChannelsIds, existingChannelsIds);
        noLongAvailable.forEach(aLong ->
            channelManagementService.markAsDeleted(aLong)
        );
    }
}
