package dev.sheldan.abstracto.experience.api;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.frontend.RoleDisplay;
import dev.sheldan.abstracto.core.models.frontend.UserDisplay;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.model.api.UserExperienceDisplay;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    private ExperienceLevelService experienceLevelService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    @GetMapping(value = "/leaderboards/{serverId}", produces = "application/json")
    public Page<UserExperienceDisplay> getLeaderboard(@PathVariable("serverId") Long serverId,
                                                      @PageableDefault(value = 50, page = 0)
                                                    @SortDefault(sort = "experience", direction = Sort.Direction.DESC)
                                                    Pageable pageable) {
        AServer server = serverManagementService.loadServer(serverId);
        Guild guild = guildService.getGuildById(serverId);
        Page<AUserExperience> allElements = userExperienceManagementService.loadAllUsersPaginated(server, pageable);
        return allElements
                .map(userExperience -> convertFromUser(guild, userExperience, pageable, allElements));
    }

    @GetMapping(value = "/leaderboards/{serverId}/{userId}", produces = "application/json")
    public List<UserExperienceDisplay> getLeaderboardForUser(@PathVariable("serverId") Long serverId, @PathVariable("userId") Long userId,
                                                             @RequestParam("windowSize") Integer windowSize) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(ServerUser.fromId(serverId, userId));
        Map<Long, AExperienceRole> experienceRolesForServer = experienceRoleManagementService.getExperienceRolesForServer(aUserInAServer.getServerReference())
            .stream()
            .collect(Collectors.toMap(AExperienceRole::getId, Function.identity()));

        Map<Integer, AExperienceLevel> levels =
            experienceLevelManagementService.getLevelConfig().stream().collect(Collectors.toMap(AExperienceLevel::getLevel, Function.identity()));
        Guild guild = guildService.getGuildById(serverId);
        List<LeaderBoardEntryResult> allElements = userExperienceManagementService.getWindowedLeaderboardEntriesForUser(aUserInAServer, windowSize);
        return allElements.stream()
            .map(leaderboardEntry -> convertFromLeaderboardEntry(guild, leaderboardEntry, experienceRolesForServer, levels))
            .toList();
    }

    private UserExperienceDisplay convertFromUser(Guild guild, AUserExperience aUserExperience, Pageable pageable, Page<AUserExperience> page) {
        Long userId = aUserExperience.getUser().getUserReference().getId();
        Member member = guild.getMember(UserSnowflake.fromId(userId));
        AExperienceRole experienceRole = aUserExperience.getCurrentExperienceRole();
        UserDisplay userDisplay = null;
        RoleDisplay roleDisplay = null;
        Long experienceNeededToNextLevel = experienceLevelService.calculateExperienceToNextLevel(aUserExperience.getCurrentLevel().getLevel(), aUserExperience.getExperience());
        Long nextLevelExperience = experienceLevelService.calculateNextLevel(aUserExperience.getCurrentLevel().getLevel()).getExperienceNeeded();
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
        Long currentExpNeeded = aUserExperience.getCurrentLevel().getExperienceNeeded();
        Long experienceWithinLevel = aUserExperience.getExperience() - currentExpNeeded;
        Long experienceNeededForCurrentLevel = nextLevelExperience - currentExpNeeded;
        return UserExperienceDisplay
                .builder()
                .id(String.valueOf(userId))
                .messages(aUserExperience.getMessageCount())
                .level(aUserExperience.getLevelOrDefault())
                .rank((int) pageable.getOffset() + page.getContent().indexOf(aUserExperience) + 1)
                .experience(aUserExperience.getExperience())
                .experienceToNextLevel(experienceNeededToNextLevel)
                .currentLevelExperienceNeeded(experienceNeededForCurrentLevel)
                .experienceOnCurrentLevel(experienceWithinLevel)
                .percentage(((float) experienceWithinLevel / experienceNeededForCurrentLevel) * 100)
                .nextLevelExperienceNeeded(nextLevelExperience)
                .role(roleDisplay)
                .member(userDisplay)
                .build();
    }

    private UserExperienceDisplay convertFromLeaderboardEntry(Guild guild, LeaderBoardEntryResult leaderBoardEntryResult,
                                                              Map<Long, AExperienceRole> experienceRolesForServer, Map<Integer, AExperienceLevel> levels) {
        Long userId = leaderBoardEntryResult.getUserId();
        Member member = guild.getMember(UserSnowflake.fromId(userId));
        UserDisplay userDisplay = null;
        RoleDisplay roleDisplay = null;
        Long experienceNeededToNextLevel = experienceLevelService.calculateExperienceToNextLevel(leaderBoardEntryResult.getLevel(),
            leaderBoardEntryResult.getExperience());
        AExperienceLevel currentExperienceLevel = levels.get(leaderBoardEntryResult.getLevel());
        Long nextLevelExperience = experienceLevelService.calculateNextLevel(leaderBoardEntryResult.getLevel()).getExperienceNeeded();
        if(experienceRolesForServer.containsKey(leaderBoardEntryResult.getRoleId())) {
            AExperienceRole experienceRole =  experienceRolesForServer.get(leaderBoardEntryResult.getRoleId());
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
        Long currentExpNeeded = currentExperienceLevel.getExperienceNeeded();
        Long experienceWithinLevel = leaderBoardEntryResult.getExperience() - currentExpNeeded;
        Long experienceNeededForCurrentLevel = nextLevelExperience - currentExpNeeded;
        return UserExperienceDisplay
            .builder()
            .id(String.valueOf(userId))
            .messages(leaderBoardEntryResult.getMessageCount())
            .level(leaderBoardEntryResult.getLevel())
            .rank(leaderBoardEntryResult.getRank())
            .experience(leaderBoardEntryResult.getExperience())
            .experienceToNextLevel(experienceNeededToNextLevel)
            .currentLevelExperienceNeeded(experienceNeededForCurrentLevel)
            .experienceOnCurrentLevel(experienceWithinLevel)
            .percentage(((float) experienceWithinLevel / experienceNeededForCurrentLevel) * 100)
            .nextLevelExperienceNeeded(nextLevelExperience)
            .role(roleDisplay)
            .member(userDisplay)
            .build();
    }

}
