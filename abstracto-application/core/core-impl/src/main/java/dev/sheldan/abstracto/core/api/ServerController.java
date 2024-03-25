package dev.sheldan.abstracto.core.api;

import dev.sheldan.abstracto.core.models.api.GuildDisplay;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/servers/v1/")
public class ServerController {

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private GuildService guildService;

    @GetMapping(value = "/{serverId}/info", produces = "application/json")
    public GuildDisplay getLeaderboard(@PathVariable("serverId") Long serverId) {
        serverManagementService.loadServer(serverId); // only used for verification if it exists in the db
        Guild guild = guildService.getGuildById(serverId);
        return GuildDisplay
                .builder()
                .name(guild.getName())
                .id(guild.getIdLong())
                .bannerUrl(guild.getBannerUrl())
                .iconUrl(guild.getIconUrl())
                .build();
    }
}
