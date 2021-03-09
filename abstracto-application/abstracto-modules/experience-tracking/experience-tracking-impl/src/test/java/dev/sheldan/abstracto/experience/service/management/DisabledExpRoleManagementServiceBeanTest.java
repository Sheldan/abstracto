package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import dev.sheldan.abstracto.experience.repository.DisabledExpRoleRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DisabledExpRoleManagementServiceBeanTest {

    @InjectMocks
    private DisabledExpRoleManagementServiceBean testUnit;

    @Mock
    private DisabledExpRoleRepository disabledExpRoleRepository;

    @Test
    public void testRoleToSetDisabled() {
        ARole role = Mockito.mock(ARole.class);
        AServer server = Mockito.mock(AServer.class);
        when(role.getServer()).thenReturn(server);
        ADisabledExpRole createdDisabledRole = Mockito.mock(ADisabledExpRole.class);
        when(createdDisabledRole.getRole()).thenReturn(role);
        when(disabledExpRoleRepository.save(any(ADisabledExpRole.class))).thenReturn(createdDisabledRole);
        ADisabledExpRole aDisabledExpRole = testUnit.setRoleToBeDisabledForExp(role);
        Assert.assertEquals(role, aDisabledExpRole.getRole());
        Assert.assertEquals(createdDisabledRole, aDisabledExpRole);
    }

    @Test
    public void testIfRoleIsDisabled() {
        ARole aRole = Mockito.mock(ARole.class);
        Long roleId = 1L;
        when(aRole.getId()).thenReturn(roleId);
        when(disabledExpRoleRepository.existsById(roleId)).thenReturn(true);
        boolean experienceDisabledForRole = testUnit.isExperienceDisabledForRole(aRole);
        Assert.assertTrue(experienceDisabledForRole);
        verify(disabledExpRoleRepository, times(1)).existsById(roleId);
    }

    @Test
    public void testRemoveRoleFromDisabled() {
        ARole aRole = Mockito.mock(ARole.class);
        testUnit.removeRoleToBeDisabledForExp(aRole);
        verify(disabledExpRoleRepository, times(1)).deleteByRole(aRole);
    }

    @Test
    public void testRetrieveAllDisabledRolesForServer() {
        AServer server = Mockito.mock(AServer.class);
        testUnit.getDisabledRolesForServer(server);
        verify(disabledExpRoleRepository, times(1)).getByRole_Server(server);
    }

}
