package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.experience.model.RoleCalculationResult;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
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
public class ExperienceRoleServiceBeanTest {

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
    private static final Long ROLE_ID = 5L;

    @Test
    public void testSettingRoleToLevelWithoutOldUsers() {
        Integer levelCount = 10;
        Role roleToChange = Mockito.mock(Role.class);
        ARole role = Mockito.mock(ARole.class);
        when(roleToChange.getIdLong()).thenReturn(ROLE_ID);
        when(roleManagementService.findRole(ROLE_ID)).thenReturn(role);
        AExperienceRole previousExperienceRole = Mockito.mock(AExperienceRole.class);
        when(experienceRoleManagementService.getRoleInServerOptional(role)).thenReturn(Optional.of(previousExperienceRole));
        CompletableFuture<Void> future = testingUnit.setRoleToLevel(roleToChange, levelCount, CHANNEL_ID);

        future.join();
        verify(experienceRoleManagementService, times(1)).unsetRole(previousExperienceRole);
        verify(self, times(1)).unsetRoleInDb(levelCount, ROLE_ID);
    }

    @Test
    public void testUnsetRoleInDb() {
        Integer levelCount = 10;
        AExperienceLevel level = Mockito.mock(AExperienceLevel.class);
        ARole roleToChange = Mockito.mock(ARole.class);
        when(roleToChange.getServer()).thenReturn(server);
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
        Role roleToChange = Mockito.mock(Role.class);
        ARole role = Mockito.mock(ARole.class);
        when(roleToChange.getIdLong()).thenReturn(ROLE_ID);
        when(roleManagementService.findRole(ROLE_ID)).thenReturn(role);
        when(role.getServer()).thenReturn(server);
        AUserExperience firstUser = Mockito.mock(AUserExperience.class);
        AUserExperience secondUser = Mockito.mock(AUserExperience.class);
        List<AUserExperience> users = Arrays.asList(firstUser, secondUser);
        AExperienceRole previousExperienceRole = Mockito.mock(AExperienceRole.class);
        when(previousExperienceRole.getUsers()).thenReturn(users);
        AExperienceRole newExperienceRole = Mockito.mock(AExperienceRole.class);
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
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles, 7);
        Assert.assertEquals(5, aExperienceRole.getLevel().getLevel().intValue());
    }

    @Test
    public void testCalculateRoleForLevelBelow() {
        List<AExperienceRole> roles = getExperienceRoles();
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles, 4);
        Assert.assertNull(aExperienceRole);
    }

    @Test
    public void testCalculateRoleForLevelOver() {
        List<AExperienceRole> roles = getExperienceRoles();
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles, 11);
        Assert.assertEquals(10, aExperienceRole.getLevel().getLevel().intValue());
    }

    @Test
    public void testCalculateRoleForLevelExact() {
        List<AExperienceRole> roles = getExperienceRoles();
        Integer level = 10;
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles, level);
        Assert.assertEquals(level, aExperienceRole.getLevel().getLevel());
    }

    @Test
    public void testCalculateRoleForNoRoleConfigFound() {
        List<AExperienceRole> roles = new ArrayList<>();
        AExperienceRole aExperienceRole = testingUnit.calculateRole(roles,  10);
        Assert.assertNull(aExperienceRole);
    }

    @Test
    public void testCalculatingLevelOfNextRole() {
        List<AExperienceRole> experienceRoles = getExperienceRoles();
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        AExperienceLevel levelToCheckFor = Mockito.mock(AExperienceLevel.class);
        when(levelToCheckFor.getLevel()).thenReturn(7);
        AExperienceLevel levelOfNextRole = testingUnit.getLevelOfNextRole(levelToCheckFor, server);
        Assert.assertEquals(10, levelOfNextRole.getLevel().intValue());
    }

    @Test
    public void testCalculatingLevelOfNextRoleIfThereIsNone() {
        List<AExperienceRole> experienceRoles = getExperienceRoles();
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        AExperienceLevel levelToCheckFor = Mockito.mock(AExperienceLevel.class);
        when(levelToCheckFor.getLevel()).thenReturn(15);
        AExperienceLevel levelOfNextRole = testingUnit.getLevelOfNextRole(levelToCheckFor, server);
        Assert.assertEquals(200, levelOfNextRole.getLevel().intValue());
    }

    private List<AExperienceRole> getExperienceRoles() {
        AExperienceRole level5ExperienceRole = getExperienceRoleForLevel(5);
        AExperienceRole level10ExperienceRole = getExperienceRoleForLevel(10);
        when(level5ExperienceRole.getServer()).thenReturn(server);
        return Arrays.asList(level5ExperienceRole, level10ExperienceRole);
    }

    private AExperienceRole getExperienceRoleForLevel(int level) {
        AExperienceLevel experienceLevel = Mockito.mock(AExperienceLevel.class);
        when(experienceLevel.getLevel()).thenReturn(level);
        AExperienceRole role = Mockito.mock(AExperienceRole.class);
        when(role.getLevel()).thenReturn(experienceLevel);
        return role;
    }

}
