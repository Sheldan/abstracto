package dev.sheldan.abstracto.invitefilter.service.management;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.invitefilter.exception.AllowedInviteLinkNotFound;
import dev.sheldan.abstracto.invitefilter.model.database.AllowedInviteLink;
import dev.sheldan.abstracto.invitefilter.repository.AllowedInviteLinkRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllowedInviteLinkManagementBeanTest {

    @InjectMocks
    private AllowedInviteLinkManagementBean testUnit;

    @Mock
    private AllowedInviteLinkRepository repository;

    @Mock
    private AServer server;

    @Mock
    private AllowedInviteLink mockedInviteLink;

    private static final Long SERVER_ID = 1L;
    private static final Long INVITE = 3L;
    private static final String CODE = "c";

    @Captor
    private ArgumentCaptor<AllowedInviteLink> linkCaptor;

    @Test
    public void testCreateAllowedInviteLink() {
        when(repository.save(linkCaptor.capture())).thenReturn(mockedInviteLink);
        AllowedInviteLink allowedInviteLink = testUnit.createAllowedInviteLink(server, INVITE, CODE);
        verify(repository, times(1)).save(linkCaptor.capture());
        Assert.assertEquals(allowedInviteLink, mockedInviteLink);
        AllowedInviteLink repositoryAllowedInviteLink = linkCaptor.getValue();
        Assert.assertEquals(server, repositoryAllowedInviteLink.getServer());
        Assert.assertEquals(INVITE, repositoryAllowedInviteLink.getTargetServerId());
    }

    @Test
    public void testRemoveAllowedInviteLink() {
        when(repository.findByTargetServerIdAndServer(INVITE, server)).thenReturn(Optional.of(mockedInviteLink));
        testUnit.removeAllowedInviteLink(server, INVITE);
        verify(repository, times(1)).delete(mockedInviteLink);
    }

    @Test(expected = AllowedInviteLinkNotFound.class)
    public void testRemoveNotPresentAllowedInviteLink() {
        when(repository.findByTargetServerIdAndServer(INVITE, server)).thenReturn(Optional.empty());
        testUnit.removeAllowedInviteLink(server, INVITE);
    }

    @Test
    public void testFindAllowedInviteLinkByCode() {
        when(repository.findByTargetServerIdAndServer(INVITE, server)).thenReturn(Optional.of(mockedInviteLink));
        AllowedInviteLink allowedInviteLinkByCode = testUnit.findAllowedInviteLinkByCode(server, INVITE);
        Assert.assertEquals(mockedInviteLink, allowedInviteLinkByCode);
    }

    @Test(expected = AllowedInviteLinkNotFound.class)
    public void testFindNotPresentAllowedInviteLinkByCode() {
        when(repository.findByTargetServerIdAndServer(INVITE, server)).thenReturn(Optional.empty());
        testUnit.findAllowedInviteLinkByCode(server, INVITE);
    }

    @Test
    public void testAllowedInviteLinkExists() {
        when(repository.findByTargetServerIdAndServer(INVITE, server)).thenReturn(Optional.of(mockedInviteLink));
        boolean exists = testUnit.allowedInviteLinkExists(server, INVITE);
        Assert.assertTrue(exists);
    }

    @Test
    public void testAllowedInviteLinkExistsViaId() {
        when(repository.findByTargetServerIdAndServer_Id(INVITE, SERVER_ID)).thenReturn(Optional.of(mockedInviteLink));
        boolean exists = testUnit.allowedInviteLinkExists(SERVER_ID, INVITE);
        Assert.assertTrue(exists);
    }

    @Test
    public void testAllowedInviteLinkExistsViaServerUser() {
        ServerUser serverUser = Mockito.mock(ServerUser.class);
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        when(repository.findByTargetServerIdAndServer_Id(INVITE, SERVER_ID)).thenReturn(Optional.of(mockedInviteLink));
        boolean exists = testUnit.allowedInviteLinkExists(serverUser, INVITE);
        Assert.assertTrue(exists);
    }
}
