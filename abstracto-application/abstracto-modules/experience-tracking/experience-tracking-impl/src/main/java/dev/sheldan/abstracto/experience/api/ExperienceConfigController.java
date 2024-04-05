package dev.sheldan.abstracto.experience.api;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.frontend.RoleDisplay;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.experience.model.api.ExperienceConfig;
import dev.sheldan.abstracto.experience.model.api.ExperienceRoleDisplay;
import dev.sheldan.abstracto.experience.model.template.LevelRole;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping(value = "/experience/v1/")
public class ExperienceConfigController {

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ExperienceRoleService experienceRoleService;

    @Autowired
    private GuildService guildService;

    @GetMapping(value = "/leaderboards/{serverId}/config", produces = "application/json")
    public ExperienceConfig getLeaderboard(@PathVariable("serverId") Long serverId) {
        AServer server = serverManagementService.loadServer(serverId);
        List<LevelRole> levelRoles = experienceRoleService.loadLevelRoleConfigForServer(server);
        levelRoles = levelRoles.stream().sorted(Comparator.comparingInt(LevelRole::getLevel).reversed()).toList();
        Guild guild = guildService.getGuildById(serverId);
        List<ExperienceRoleDisplay> roles = levelRoles
                .stream()
                .map(levelRole -> convertRole(levelRole, guild))
                .toList();
        return ExperienceConfig
                .builder()
                .roles(roles)
                .build();
    }

    private ExperienceRoleDisplay convertRole(LevelRole levelRole, Guild guild) {
        Role guildRole = guild.getRoleById(levelRole.getRoleId());
        RoleDisplay roleDisplay;
        if(guildRole != null) {
            roleDisplay = RoleDisplay.fromRole(guildRole);
        } else {
            roleDisplay = RoleDisplay
                    .builder()
                    .id(String.valueOf(levelRole.getRoleId()))
                    .build();
        }
        return ExperienceRoleDisplay
                .builder()
                .level(levelRole.getLevel())
                .role(roleDisplay)
                .build();
    }
}
