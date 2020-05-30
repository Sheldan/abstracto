package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import dev.sheldan.abstracto.experience.repository.DisabledExpRoleRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DisabledExpRoleManagementServiceBeanTest extends ExperienceRelatedTest {

    @InjectMocks
    private DisabledExpRoleManagementServiceBean testUnit;

    @Mock
    private DisabledExpRoleRepository disabledExpRoleRepository;

    @Test
    public void testRoleToSetDisabled() {
        ARole role = getARole();
        ADisabledExpRole createdDisabledRole = getDisabledRole(role);
        when(disabledExpRoleRepository.save(any(ADisabledExpRole.class))).thenReturn(createdDisabledRole);
        ADisabledExpRole aDisabledExpRole = testUnit.setRoleToBeDisabledForExp(role);
        Assert.assertEquals(role.getId(), aDisabledExpRole.getRole().getId());
        Assert.assertEquals(createdDisabledRole.getId(), aDisabledExpRole.getId());
    }

    @Test
    public void testIfRoleIsDisabled() {
        ARole aRole = getARole();
        when(disabledExpRoleRepository.existsByRole(aRole)).thenReturn(true);
        boolean experienceDisabledForRole = testUnit.isExperienceDisabledForRole(aRole);
        Assert.assertTrue(experienceDisabledForRole);
        verify(disabledExpRoleRepository, times(1)).existsByRole(aRole);
    }

    @Test
    public void testRemoveRoleFromDisabled() {
        ARole aRole = getARole();
        testUnit.removeRoleToBeDisabledForExp(aRole);
        verify(disabledExpRoleRepository, times(1)).deleteByRole(aRole);
    }

    @Test
    public void testRetrieveAllDisabledRolesForServer() {
        AServer server = AServer.builder().id(1L).build();
        testUnit.getDisabledRolesForServer(server);
        verify(disabledExpRoleRepository, times(1)).getByRole_Server(server);
    }

    private ADisabledExpRole getDisabledRole(ARole role) {
        return ADisabledExpRole.builder().role(role).id(2L).build();
    }

    private ARole getARole() {
        return ARole.builder().id(1L).build();
    }
}
