package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.converter.RoleConverter;
import dev.sheldan.abstracto.core.models.converter.ServerConverter;
import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.*;
import dev.sheldan.abstracto.core.models.dto.RoleDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.models.utils.ChannelUtils;
import dev.sheldan.abstracto.core.service.management.ChannelManagementServiceBean;
import dev.sheldan.abstracto.core.service.management.RoleManagementServiceBean;
import dev.sheldan.abstracto.core.service.management.ServerManagementServiceBean;
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

import javax.security.auth.login.LoginException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StartupServiceBean implements Startup {

    @Autowired
    private Bot service;

    @Autowired
    private List<? extends  ListenerAdapter> listeners;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    @Autowired
    private ChannelManagementServiceBean channelManagementService;

    @Autowired
    private RoleManagementServiceBean roleManagementService;

    @Autowired
    private ServerConverter serverConverter;

    @Autowired
    private List<ServerConfigListener> configListeners;

    @Autowired
    private RoleConverter roleConverter;


    @Override
    public void startBot() throws LoginException {
        service.login();
        listeners.forEach(o -> service.getInstance().addEventListener(o));
    }

    @Override
    @Transactional
    public void synchronize() {
        log.info("Synchronizing servers.");
       // synchronizeServers();
        log.info("Done synchronizing servers");
    }
    /*

    private void synchronizeServers(){
        JDA instance = service.getInstance();
        List<Guild> onlineGuilds = instance.getGuilds();
        Set<Long> availableServers = SnowflakeUtils.getSnowflakeIds(onlineGuilds);
        availableServers.forEach(aLong -> {
            ServerDto newAServer = serverManagementService.loadOrCreate(aLong);
            Guild newGuild = instance.getGuildById(aLong);
            log.debug("Synchronizing server: {}", aLong);
            if(newGuild != null){
                synchronizeRolesOf(newGuild, newAServer);
                synchronizeChannelsOf(newGuild, newAServer);
                configListeners.forEach(serverConfigListener -> {
                    serverConfigListener.updateServerConfig(newAServer);
                });
            }
        });

    }

    private void synchronizeRolesOf(Guild guild, ServerDto existingAServer){
        List<Role> existingRoles = guild.getRoles();
        List<RoleDto> knownARoles = existingAServer.getRoles();
        Set<Long> knownRolesId = SnowflakeUtils.getOwnItemsIds(knownARoles);
        Set<Long> availableRoles = SnowflakeUtils.getSnowflakeIds(existingRoles);
        Set<Long> newRoles = SetUtils.disjunction(availableRoles, knownRolesId);
        newRoles.forEach(aLong -> {
            ARole newRole = roleManagementService.createRole(aLong);
            log.debug("Adding new role: {}", aLong);
            existingAServer.getRoles().add(roleConverter.fromARole(newRole));
        });
    }

    private void synchronizeChannelsOf(Guild guild, ServerDto existingServer){
        List<GuildChannel> available = guild.getChannels();
        List<AChannel> knownChannels = existingServer.getChannels().stream().filter(aChannel -> !aChannel.getDeleted()).collect(Collectors.toList());
        Set<Long> knownChannelsIds = SnowflakeUtils.getOwnItemsIds(knownChannels);
        Set<Long> existingChannelsIds = SnowflakeUtils.getSnowflakeIds(available);
        Set<Long> newChannels = SetUtils.difference(existingChannelsIds, knownChannelsIds);
        newChannels.forEach(aLong -> {
            GuildChannel channel1 = available.stream().filter(channel -> channel.getIdLong() == aLong).findFirst().get();
            log.debug("Adding new channel: {}", aLong);
            AChannelType type = ChannelUtils.getAChannelType(channel1.getType());
            AChannel newChannel = channelManagementService.createChannel(channel1.getIdLong(), type);
            serverManagementService.addChannelToServer(existingServer, newChannel);
        });

        Set<Long> noLongAvailable = SetUtils.difference(knownChannelsIds, existingChannelsIds);
        noLongAvailable.forEach(aLong -> {
            channelManagementService.markAsDeleted(aLong);
        });
    }


     */
}
