package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.models.RoleCalculationResult;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperienceRoleServiceBeanTest extends ExperienceRelatedTest {

    @InjectMocks
    private ExperienceRoleServiceBean testingUnit;

    @Mock
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Mock
    private ExperienceLevelManagementService experienceLevelService;

    @Mock
    private AUserExperienceService userExperienceService;

    @Mock
    private RoleManagementService roleManagementService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private ExperienceRoleServiceBean self;

    @Mock
    private AServer server;

    private static final Long CHANNEL_ID = 4L;

    @Test
    public void testSettingRoleToLevelWithoutOldUsers() {
        Integer levelCount = 10;
        AExperienceLevel level = Mockito.mock(AExperienceLevel.class);
        Role roleToChange = Mockito.mock(Role.class);
        ARole role = Mockito.mock(ARole.class);
        Long roleId = 5L;
        when(roleToChange.getIdLong()).thenReturn(roleId);
        when(roleManagementService.findRole(roleId)).thenReturn(role);
        AExperienceRole previousExperienceRole = AExperienceRole.builder().role(role).roleServer(server).level(level).build();
        when(experienceRoleManagementService.getRoleInServerOptional(role)).thenReturn(Optional.of(previousExperienceRole));
        CompletableFuture<Void> future = testingUnit.setRoleToLevel(roleToChange, levelCount, CHANNEL_ID);

        future.join();
        verify(experienceRoleManagementService, times(1)).unsetRole(previousExperienceRole);
        verify(self, times(1)).unsetRoleInDb(levelCount, roleId);
    }

    @Test
    public void testUnsetRoleInDb() {
        Integer levelCount = 10;
        AExperienceLevel level = AExperienceLevel.builder().experienceNeeded(10L).level(levelCount).build();
        ARole roleToChange = getRole(1L, server);
        when(experienceLevelService.getLevel(levelCount)).thenReturn(Optional.of(level));
        when(roleManagementService.findRole(roleToChange.getId())).thenReturn(roleToChange);
        testingUnit.unsetRoleInDb(levelCount, roleToChange.getId());

        verify(experienceRoleManagementService, times(1)).removeAllRoleAssignmentsForLevelInServer(level, server);
        verify(experienceRoleManagementService, times(1)).setLevelToRole(level, roleToChange);
        verify(experienceRoleManagementService, times(0)).getExperienceRolesForServer(server);
    }


    @Test
    public void testSettingRoleToLevelExistingUsers() {
        Integer levelCount = 10;
        AExperienceLevel level = AExperienceLevel.builder().experienceNeeded(10L).level(levelCount).build();
        Role roleToChange = Mockito.mock(Role.class);
        ARole role = Mockito.mock(ARole.class);
        Long roleId = 5L;
        when(roleToChange.getIdLong()).thenReturn(roleId);
        when(roleManagementService.findRole(roleId)).thenReturn(role);
        when(role.getServer()).thenReturn(server);
        ARole newRoleToAward = getRole(2L, server);
        AUserExperience firstUser = AUserExperience.builder().build();
        AUserExperience secondUser = AUserExperience.builder().build();
        List<AUserExperience> users = Arrays.asList(firstUser, secondUser);
        AExperienceRole previousExperienceRole = AExperienceRole.builder().role(role).id(roleToChange.getIdLong()).roleServer(server).level(level).users(users).build();
        AExperienceRole newExperienceRole = AExperienceRole.builder().role(newRoleToAward).id(newRoleToAward.getId()).roleServer(server).level(level).build();
        when(experienceRoleManagementService.getRoleInServerOptional(role)).thenReturn(Optional.of(previousExperienceRole));
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(new ArrayList<>(Arrays.asList(newExperienceRole, previousExperienceRole)));
        List<CompletableFuture<RoleCalculationResult>> futures = new ArrayList<>();
        futures.add(CompletableFuture.completedFuture(null));
        AChannel feedbackChannel = Mockito.mock(AChannel.class);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(feedbackChannel);
        CompletableFutureList<RoleCalculationResult> futuresList = new CompletableFutureList<>(futures);
        when(userExperienceService.executeActionOnUserExperiencesWithFeedBack(eq(users), eq(feedbackChannel), any())).thenReturn(futuresList);
        CompletableFuture<Void> future = testingUnit.setRoleToLevel(roleToChange, levelCount, CHANNEL_ID);
        future.join();
        verify(experienceRoleManagementService, times(0)).unsetRole(previousExperienceRole);
    }

    @Test
    public void testCalculateRoleForLevelInBetween() {
        List<AExperienceRole> roles = getExperienceRoles();
        AUserExperience userExperience = AUserExperience.builder().currentLevel(AExperienceLevel.builder().level(6).build()).build();
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles, userExperience.getLevelOrDefault());
        Assert.assertEquals(aExperienceRole.getLevel().getLevel().intValue(), 5);
    }

    @Test
    public void testCalculateRoleForLevelBelow() {
        List<AExperienceRole> roles = getExperienceRoles();
        AUserExperience userExperience = AUserExperience.builder().currentLevel(AExperienceLevel.builder().level(4).build()).build();
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles, userExperience.getLevelOrDefault());
        Assert.assertNull(aExperienceRole);
    }

    @Test
    public void testCalculateRoleForLevelOver() {
        List<AExperienceRole> roles = getExperienceRoles();
        AUserExperience userExperience = AUserExperience.builder().currentLevel(AExperienceLevel.builder().level(11).build()).build();
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles, userExperience.getLevelOrDefault());
        Assert.assertEquals(aExperienceRole.getLevel().getLevel().intValue(), 10);
    }

    @Test
    public void testCalculateRoleForLevelExact() {
        List<AExperienceRole> roles = getExperienceRoles();
        AUserExperience userExperience = AUserExperience.builder().currentLevel(AExperienceLevel.builder().level(10).build()).build();
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles,  userExperience.getLevelOrDefault());
        Assert.assertEquals(aExperienceRole.getLevel().getLevel().intValue(), 10);
    }

    @Test
    public void testCalculateRoleForNoRoleConfigFound() {
        List<AExperienceRole> roles = new ArrayList<>();
        AUserExperience userExperience = AUserExperience.builder().currentLevel(AExperienceLevel.builder().level(6).build()).build();
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles,  userExperience.getLevelOrDefault());
        Assert.assertNull(aExperienceRole);
    }

    @Test
    public void testCalculatingLevelOfNextRole() {
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(getExperienceRoles());
        AExperienceLevel levelToCheckFor =  AExperienceLevel.builder().level(7).build();
        AExperienceLevel levelOfNextRole = testingUnit.getLevelOfNextRole(levelToCheckFor, server);
        Assert.assertEquals(10, levelOfNextRole.getLevel().intValue());
    }

    @Test
    public void testCalculatingLevelOfNextRoleIfThereIsNone() {
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(getExperienceRoles());
        AExperienceLevel levelToCheckFor =  AExperienceLevel.builder().level(15).build();
        AExperienceLevel levelOfNextRole = testingUnit.getLevelOfNextRole(levelToCheckFor, server);
        Assert.assertEquals(200, levelOfNextRole.getLevel().intValue());
    }

    private List<AExperienceRole> getExperienceRoles() {
        AExperienceRole level5ExperienceRole = getExperienceRoleForLevel(5);
        AExperienceRole level10ExperienceRole = getExperienceRoleForLevel(10);
        return Arrays.asList(level5ExperienceRole, level10ExperienceRole);
    }

    private AExperienceRole getExperienceRoleForLevel(int levelToBuild) {
        AExperienceLevel firstLevel = AExperienceLevel.builder().level(levelToBuild).build();
        return AExperienceRole.builder().roleServer(server).level(firstLevel).build();
    }

    private ARole getRole(Long id, AServer server) {
        return ARole.builder().id(id).server(server).deleted(false).build();
    }

    private AChannel getFeedbackChannel(AServer server) {
        return AChannel.builder().id(1L).server(server).type(AChannelType.TEXT).build();
    }

}
