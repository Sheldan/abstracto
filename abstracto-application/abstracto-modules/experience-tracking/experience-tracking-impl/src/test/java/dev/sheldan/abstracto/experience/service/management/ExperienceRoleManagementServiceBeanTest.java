package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.repository.ExperienceRoleRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperienceRoleManagementServiceBeanTest {

    @InjectMocks
    private ExperienceRoleManagementServiceBean testUnit;

    @Mock
    private ExperienceRoleRepository experienceRoleRepository;

    @Captor
    private ArgumentCaptor<AExperienceRole> roleArgumentCaptor;

    @Mock
    private AExperienceRole experienceRole;

    @Mock
    private ARole role;

    @Mock
    private AServer server;

    @Mock
    private AExperienceLevel level;

    private static final Long SERVER_ID = 3L;
    private static final Long ROLE_ID = 4L;

    @Test
    public void testRemovingAllRoleAssignmentsForLevel() {
        when(level.getLevel()).thenReturn(10);
        AExperienceRole secondRole = Mockito.mock(AExperienceRole.class);
        List<AExperienceRole> experienceRoles = Arrays.asList(experienceRole, secondRole);
        when(experienceRoleRepository.findByLevelAndRoleServer(level, server)).thenReturn(experienceRoles);
        testUnit.removeAllRoleAssignmentsForLevelInServer(level, server);
        verify(experienceRoleRepository, times(1)).findByLevelAndRoleServer(level, server);
        verify(experienceRoleRepository, times(experienceRoles.size())).delete(roleArgumentCaptor.capture());
        List<AExperienceRole> allValues = roleArgumentCaptor.getAllValues();
        Assert.assertEquals(experienceRole, allValues.get(0));
        Assert.assertEquals(secondRole, allValues.get(1));
        Assert.assertEquals(2, allValues.size());
    }

    @Test
    public void removeRoleAssignmentsForLevelWithoutAny() {
        when(level.getLevel()).thenReturn(10);
        List<AExperienceRole> experienceRoles = new ArrayList<>();
        when(experienceRoleRepository.findByLevelAndRoleServer(level, server)).thenReturn(experienceRoles);
        testUnit.removeAllRoleAssignmentsForLevelInServer(level, server);
        verify(experienceRoleRepository, times(1)).findByLevelAndRoleServer(level, server);
        verify(experienceRoleRepository, times(experienceRoles.size())).delete(roleArgumentCaptor.capture());
        List<AExperienceRole> allValues = roleArgumentCaptor.getAllValues();
        Assert.assertEquals(0, allValues.size());
    }

    @Test
    public void testUnsetRole() {
        when(experienceRole.getServer()).thenReturn(server);
        testUnit.unsetRole(experienceRole);
        verify(experienceRoleRepository, times(1)).delete(experienceRole);
    }

    @Test
    public void testFindExperienceRoleForRoleInServer() {
        when((experienceRoleRepository.findByRole(role))).thenReturn(Optional.of(experienceRole));
        AExperienceRole roleInServer = testUnit.getRoleInServer(role);
        Assert.assertEquals(experienceRole, roleInServer);
    }

    @Test
    public void testFindExperienceRolesForServer() {
        AExperienceRole secondRole = Mockito.mock(AExperienceRole.class);
        List<AExperienceRole> experienceRoles = Arrays.asList(experienceRole, secondRole);
        when(experienceRoleRepository.findByRoleServer(server)).thenReturn(experienceRoles);
        List<AExperienceRole> experienceRolesForServer = testUnit.getExperienceRolesForServer(server);
        verify(experienceRoleRepository, times(1)).findByRoleServer(server);
        Assert.assertEquals(experienceRole, experienceRolesForServer.get(0));
        Assert.assertEquals(secondRole, experienceRolesForServer.get(1));
        Assert.assertEquals(2, experienceRolesForServer.size());
    }

    @Test
    public void setLevelToRoleWhichHasAnExistingMapping() {
        Integer levelNumber = 4;
        setupExperienceRole(levelNumber);
        when(experienceRoleRepository.findByRole(role)).thenReturn(Optional.of(experienceRole));
        AExperienceLevel newLevel = Mockito.mock(AExperienceLevel.class);
        when(newLevel.getLevel()).thenReturn(levelNumber);
        AExperienceRole updatedExperienceRole = testUnit.setLevelToRole(newLevel, role);
        verify(experienceRoleRepository, times(1)).findByRole(role);
        Assert.assertEquals(levelNumber, updatedExperienceRole.getLevel().getLevel());
    }

    @Test
    public void setLevelToRoleWithoutAMappingExistingPreviously() {
        Integer levelNumber = 4;
        setupExperienceRole(levelNumber);
        when(experienceRoleRepository.findByRole(experienceRole.getRole())).thenReturn(Optional.empty());
        when(experienceRoleRepository.save(any(AExperienceRole.class))).thenReturn(experienceRole);
        AExperienceLevel newLevel = Mockito.mock(AExperienceLevel.class);
        AExperienceRole updatedExperienceRole = testUnit.setLevelToRole(newLevel, role);
        verify(experienceRoleRepository, times(1)).findByRole(role);
        Assert.assertEquals(levelNumber, updatedExperienceRole.getLevel().getLevel());
    }

    private void setupExperienceRole(Integer levelNumber) {
        when(role.getId()).thenReturn(ROLE_ID);
        when(server.getId()).thenReturn(SERVER_ID);
        when(role.getServer()).thenReturn(server);
        when(experienceRole.getRole()).thenReturn(role);
        when(level.getLevel()).thenReturn(levelNumber);
        when(experienceRole.getLevel()).thenReturn(level);
    }

}
