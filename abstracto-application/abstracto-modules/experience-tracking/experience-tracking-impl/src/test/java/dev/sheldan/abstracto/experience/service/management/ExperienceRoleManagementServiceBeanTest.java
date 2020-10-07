package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.repository.ExperienceRoleRepository;
import dev.sheldan.abstracto.test.MockUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperienceRoleManagementServiceBeanTest extends ExperienceRelatedTest {

    @InjectMocks
    private ExperienceRoleManagementServiceBean testUnit;

    @Mock
    private ExperienceRoleRepository experienceRoleRepository;

    @Captor
    private ArgumentCaptor<AExperienceRole> roleArgumentCaptor;

    @Test
    public void testRemovingAllRoleAssignmentsForLevel() {
        AServer server = MockUtils.getServer();
        AExperienceLevel level = getLevel(10, 100L);
        List<AExperienceRole> experienceRoles = getExperienceRoles();
        when(experienceRoleRepository.findByLevelAndRoleServer(level, server)).thenReturn(experienceRoles);
        testUnit.removeAllRoleAssignmentsForLevelInServer(level, server);
        verify(experienceRoleRepository, times(1)).findByLevelAndRoleServer(level, server);
        verify(experienceRoleRepository, times(experienceRoles.size())).delete(roleArgumentCaptor.capture());
        List<AExperienceRole> allValues = roleArgumentCaptor.getAllValues();
        for (int i = 0; i < allValues.size(); i++) {
            AExperienceRole role = allValues.get(i);
            AExperienceRole innerRole = experienceRoles.get(i);
            Assert.assertEquals(innerRole.getLevel().getLevel(), role.getLevel().getLevel());
            Assert.assertEquals(innerRole.getRole().getId(), role.getRole().getId());
        }
        Assert.assertEquals(2, allValues.size());
    }

    @Test
    public void removeRoleAssignmentsForLevelWithoutAny() {
        AServer server = MockUtils.getServer();
        AExperienceLevel level = getLevel(10, 100L);
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
        AExperienceRole role = getExperienceRoleForLevel(37);
        testUnit.unsetRole(role);
        verify(experienceRoleRepository, times(1)).delete(role);
    }

    @Test
    public void testFindExperienceRoleForRoleInServer() {
        AExperienceRole expRole = getExperienceRoleForLevel(37);
        when((experienceRoleRepository.findByRole(expRole.getRole()))).thenReturn(Optional.of(expRole));
        AExperienceRole roleInServer = testUnit.getRoleInServer(expRole.getRole());
        Assert.assertEquals(expRole.getRole().getId(), roleInServer.getRole().getId());
        verify(experienceRoleRepository, times(1)).findByRole(expRole.getRole());
    }

    @Test
    public void testFindExperienceRolesForServer() {
        AServer server = MockUtils.getServer();
        List<AExperienceRole> experienceRoles = getExperienceRoles();
        when(experienceRoleRepository.findByRoleServer(server)).thenReturn(experienceRoles);
        List<AExperienceRole> experienceRolesForServer = testUnit.getExperienceRolesForServer(server);
        verify(experienceRoleRepository, times(1)).findByRoleServer(server);

        for (int i = 0; i < experienceRolesForServer.size(); i++) {
            AExperienceRole role = experienceRolesForServer.get(i);
            AExperienceRole innerRole = experienceRoles.get(i);
            Assert.assertEquals(innerRole.getLevel().getLevel(), role.getLevel().getLevel());
            Assert.assertEquals(innerRole.getRole().getId(), role.getRole().getId());
        }
        Assert.assertEquals(2, experienceRolesForServer.size());
    }

    @Test
    public void setLevelToRoleWhichHasAnExistingMapping() {
        int level = 5;
        AExperienceRole experienceRole = getExperienceRoleForLevel(level);
        when(experienceRoleRepository.findByRole(experienceRole.getRole())).thenReturn(Optional.of(experienceRole));
        AExperienceLevel newLevel = AExperienceLevel.builder().level(8).build();
        AExperienceRole updatedExperienceRole = testUnit.setLevelToRole(newLevel, experienceRole.getRole());
        verify(experienceRoleRepository, times(1)).findByRole(experienceRole.getRole());
        Assert.assertEquals(newLevel.getLevel(), updatedExperienceRole.getLevel().getLevel());
    }

    @Test
    public void setLevelToRoleWithoutAMappingExistingPreviously() {
        int level = 5;
        AExperienceRole experienceRole = getExperienceRoleForLevel(level);
        when(experienceRoleRepository.findByRole(experienceRole.getRole())).thenReturn(Optional.empty());
        when(experienceRoleRepository.save(any(AExperienceRole.class))).thenReturn(experienceRole);
        AExperienceLevel newLevel = AExperienceLevel.builder().level(8).build();
        AExperienceRole updatedExperienceRole = testUnit.setLevelToRole(newLevel, experienceRole.getRole());
        verify(experienceRoleRepository, times(1)).findByRole(experienceRole.getRole());
        Assert.assertEquals(experienceRole.getLevel().getLevel(), updatedExperienceRole.getLevel().getLevel());
    }

    private List<AExperienceRole> getExperienceRoles() {
        AExperienceRole level5ExperienceRole = getExperienceRoleForLevel(7);
        AExperienceRole level10ExperienceRole = getExperienceRoleForLevel(25);
        return Arrays.asList(level5ExperienceRole, level10ExperienceRole);
    }

    private AExperienceRole getExperienceRoleForLevel(int levelToBuild) {
        AExperienceLevel firstLevel = AExperienceLevel.builder().level(levelToBuild).build();
        AServer server = AServer.builder().id(4L).build();
        ARole aRole = ARole.builder().id((long) levelToBuild).server(server).build();
        return AExperienceRole.builder().role(aRole).roleServer(server).level(firstLevel).build();
    }

    private AExperienceLevel getLevel(Integer level, Long neededExperience) {
        return AExperienceLevel
                .builder()
                .level(level)
                .experienceNeeded(neededExperience)
                .build();
    }

}
