package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.SnowflakeUtils;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.core.models.ARole;
import dev.sheldan.abstracto.core.models.AServer;
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

@Slf4j
@Service
public class StartupManager implements Startup {

    @Autowired
    private BotService service;

    @Autowired
    private List<? extends  ListenerAdapter> listeners;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private RoleService roleService;


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
    }

    private void synchronizeServers(){
        JDA instance = service.getInstance();
        List<Guild> onlineGuilds = instance.getGuilds();
        Set<Long> availableServers = SnowflakeUtils.getSnowflakeIds(onlineGuilds);
        availableServers.forEach(aLong -> {
            AServer newAServer = serverService.createServer(aLong);
            Guild newGuild = instance.getGuildById(aLong);
            log.debug("Synchronizing server: {}", aLong);
            if(newGuild != null){
                synchronizeRolesOf(newGuild, newAServer);
                synchronizeChannelsOf(newGuild, newAServer);
            }
        });

    }

    private void synchronizeRolesOf(Guild guild, AServer existingAServer){
        List<Role> existingRoles = guild.getRoles();
        List<ARole> knownARoles = existingAServer.getRoles();
        Set<Long> knownRolesId = SnowflakeUtils.getOwnItemsIds(knownARoles);
        Set<Long> availableRoles = SnowflakeUtils.getSnowflakeIds(existingRoles);
        Set<Long> newRoles = SetUtils.disjunction(availableRoles, knownRolesId);
        newRoles.forEach(aLong -> {
            ARole newRole = roleService.createRole(aLong);
            log.debug("Adding new role: {}", aLong);
            existingAServer.getRoles().add(newRole);
        });
    }

    private void synchronizeChannelsOf(Guild guild, AServer existingServer){
        List<GuildChannel> available = guild.getChannels();
        List<AChannel> knownChannels = existingServer.getChannels();
        Set<Long> knownChannelsIds = SnowflakeUtils.getOwnItemsIds(knownChannels);
        Set<Long> existingChannelsIds = SnowflakeUtils.getSnowflakeIds(available);
        Set<Long> newChannels = SetUtils.disjunction(existingChannelsIds, knownChannelsIds);
        newChannels.forEach(aLong -> {
            GuildChannel channel1 = available.stream().filter(channel -> channel.getIdLong() == aLong).findFirst().get();
            log.debug("Adding new channel: {}", aLong);
            AChannelType type = AChannel.getAChannelType(channel1.getType());
            AChannel newChannel = channelService.createChannel(channel1.getIdLong(), type);
            serverService.addChannelToServer(existingServer, newChannel);
        });
    }
}
