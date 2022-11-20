package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
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
