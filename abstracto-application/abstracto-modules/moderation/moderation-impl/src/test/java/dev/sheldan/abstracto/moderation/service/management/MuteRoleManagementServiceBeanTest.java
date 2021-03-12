package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.model.database.MuteRole;
import dev.sheldan.abstracto.moderation.repository.MuteRoleRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MuteRoleManagementServiceBeanTest {

    @InjectMocks
    private MuteRoleManagementServiceBean testUnit;

    @Mock
    private MuteRoleRepository muteRoleRepository;

    @Mock
    private AServer server;

    @Test
    public void testRetrieveMuteRoleForServer() {
        MuteRole role = Mockito.mock(MuteRole.class);
        when(muteRoleRepository.findByRoleServer(server)).thenReturn(role);
        MuteRole muteRole = testUnit.retrieveMuteRoleForServer(server);
        Assert.assertEquals(role, muteRole);
    }

    @Test
    public void testServeHasMuteRole() {
        when(muteRoleRepository.existsByRoleServer(server)).thenReturn(true);
        Assert.assertTrue(testUnit.muteRoleForServerExists(server));
    }

    @Test
    public void testCreateMuteRoleForServer() {
        ARole role = Mockito.mock(ARole.class);
        MuteRole muteRoleForServer = testUnit.createMuteRoleForServer(server, role);
        verifyRoleSaved(role, muteRoleForServer, 1);
    }

    @Test
    public void testRetrieveRolesForServer() {
        List<MuteRole> existingRoles = Arrays.asList(Mockito.mock(MuteRole.class), Mockito.mock(MuteRole.class));
        when(muteRoleRepository.findAllByRoleServer(server)).thenReturn(existingRoles);
        List<MuteRole> foundRoles = testUnit.retrieveMuteRolesForServer(server);
        Assert.assertEquals(existingRoles.size(), foundRoles.size());
        for (int i = 0; i < existingRoles.size(); i++) {
            MuteRole existingRole = existingRoles.get(i);
            MuteRole foundRole = foundRoles.get(i);
            Assert.assertEquals(existingRole, foundRole);
        }
    }

    @Test
    public void testSetMuteRoleWithoutPrevious() {
        ARole role = Mockito.mock(ARole.class);
        when(muteRoleRepository.existsByRoleServer(server)).thenReturn(false);
        MuteRole muteRole = testUnit.setMuteRoleForServer(server, role);
        verifyRoleSaved(role, muteRole, 1);
    }

    @Test
    public void testSetMuteRoleWithPrevious() {
        ARole role = Mockito.mock(ARole.class);
        when(muteRoleRepository.existsByRoleServer(server)).thenReturn(true);
        MuteRole existingRole = Mockito.mock(MuteRole.class);
        when(existingRole.getRole()).thenReturn(role);
        when(muteRoleRepository.findByRoleServer(server)).thenReturn(existingRole);
        MuteRole muteRole = testUnit.setMuteRoleForServer(server, role);
        verifyRoleSaved(role, muteRole, 0);
    }

    private void verifyRoleSaved(ARole role, MuteRole muteRoleForServer, Integer saveCount) {
        Assert.assertEquals(role, muteRoleForServer.getRole());
        verify(muteRoleRepository, times(saveCount)).save(muteRoleForServer);
    }


}
