package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.MuteRole;
import dev.sheldan.abstracto.moderation.repository.MuteRoleRepository;
import dev.sheldan.abstracto.test.MockUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    private AServer server;

    @Before
    public void setup() {
        this.server = MockUtils.getServer();
    }

    @Test
    public void testRetrieveMuteRoleForServer() {
        MuteRole role = getMuteRole();
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
        ARole role = ARole.builder().build();
        MuteRole muteRoleForServer = testUnit.createMuteRoleForServer(server, role);
        verifyRoleSaved(role, muteRoleForServer, 1);
    }

    @Test
    public void testRetrieveRolesForServer() {
        List<MuteRole> existingRoles = Arrays.asList(getMuteRole(), getMuteRole());
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
        ARole role = ARole.builder().build();
        when(muteRoleRepository.existsByRoleServer(server)).thenReturn(false);
        MuteRole muteRole = testUnit.setMuteRoleForServer(server, role);
        verifyRoleSaved(role, muteRole, 1);
    }

    @Test
    public void testSetMuteRoleWithPrevious() {
        ARole role = ARole.builder().build();
        when(muteRoleRepository.existsByRoleServer(server)).thenReturn(true);
        MuteRole existingRole = getMuteRole();
        when(muteRoleRepository.findByRoleServer(server)).thenReturn(existingRole);
        MuteRole muteRole = testUnit.setMuteRoleForServer(server, role);
        verifyRoleSaved(role, muteRole, 0);
    }

    private void verifyRoleSaved(ARole role, MuteRole muteRoleForServer, Integer saveCount) {
        Assert.assertEquals(role, muteRoleForServer.getRole());
        Assert.assertEquals(server, muteRoleForServer.getRoleServer());
        verify(muteRoleRepository, times(saveCount)).save(muteRoleForServer);
    }


    private MuteRole getMuteRole() {
        return MuteRole.builder().roleServer(server).build();
    }

}
