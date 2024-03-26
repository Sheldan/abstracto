package dev.sheldan.abstracto.experience.api;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.frontend.RoleDisplay;
import dev.sheldan.abstracto.core.models.frontend.UserDisplay;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.experience.model.api.UserExperienceDisplay;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/experience/v1")
public class LeaderboardController {

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private GuildService guildService;

    @GetMapping(value = "/leaderboards/{serverId}", produces = "application/json")
    public Page<UserExperienceDisplay> getLeaderboard(@PathVariable("serverId") Long serverId,
                                                      @PageableDefault(value = 25, page = 0)
                                                    @SortDefault(sort = "experience", direction = Sort.Direction.DESC)
                                                    Pageable pageable) {
        AServer server = serverManagementService.loadServer(serverId);
        Guild guild = guildService.getGuildById(serverId);
        Page<AUserExperience> allElements = userExperienceManagementService.loadAllUsersPaginated(server, pageable);
        return allElements
                .map(userExperience -> convertFromUser(guild, userExperience, pageable, allElements));
    }

    private UserExperienceDisplay convertFromUser(Guild guild, AUserExperience aUserExperience, Pageable pageable, Page<AUserExperience> page) {
        Long userId = aUserExperience.getUser().getUserReference().getId();
        Member member = guild.getMember(UserSnowflake.fromId(userId));
        AExperienceRole experienceRole = aUserExperience.getCurrentExperienceRole();
        UserDisplay userDisplay = null;
        RoleDisplay roleDisplay = null;
        if(experienceRole != null) {
            Role role = guild.getRoleById(experienceRole.getRole().getId());
            if(role != null) {
                roleDisplay = RoleDisplay.fromRole(role);
            } else {
                roleDisplay = RoleDisplay.fromARole(experienceRole.getRole());
            }
        }
        if(member != null) {
            userDisplay = UserDisplay.fromMember(member);
        }
        return UserExperienceDisplay
                .builder()
                .id(userId)
                .messages(aUserExperience.getMessageCount())
                .level(aUserExperience.getLevelOrDefault())
                .rank((int) pageable.getOffset() + page.getContent().indexOf(aUserExperience) + 1)
                .experience(aUserExperience.getExperience())
                .role(roleDisplay)
                .member(userDisplay)
                .build();
    }

}
