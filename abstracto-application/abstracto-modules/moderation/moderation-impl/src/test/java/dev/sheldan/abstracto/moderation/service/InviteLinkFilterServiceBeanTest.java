package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.moderation.exception.InvalidInviteException;
import dev.sheldan.abstracto.moderation.model.database.FilteredInviteLink;
import dev.sheldan.abstracto.moderation.service.management.AllowedInviteLinkManagement;
import dev.sheldan.abstracto.moderation.service.management.FilteredInviteLinkManagement;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InviteLinkFilterServiceBeanTest {

    @InjectMocks
    private InviteLinkFilterServiceBean testUnit;

    @Mock
    private AllowedInviteLinkManagement allowedInviteLinkManagement;

    @Mock
    private FilteredInviteLinkManagement filteredInviteLinkManagement;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private AServer server;

    @Mock
    private ServerUser serverUser;

    private static final String INVITE_CODE = "asdf";
    private static final String FULL_INVITE = "discord.gg/" + INVITE_CODE;
    private static final Long SERVER_ID = 1L;

    @Test
    public void testIsCodeAllowedViaId() {
        when(allowedInviteLinkManagement.allowedInviteLinkExists(SERVER_ID, FULL_INVITE)).thenReturn(true);
        boolean codeAllowed = testUnit.isCodeAllowed(FULL_INVITE, SERVER_ID);
        Assert.assertTrue(codeAllowed);
    }

    @Test
    public void testIsCodeAllowed() {
        when(allowedInviteLinkManagement.allowedInviteLinkExists(serverUser, FULL_INVITE)).thenReturn(true);
        boolean codeAllowed = testUnit.isCodeAllowed(FULL_INVITE, serverUser);
        Assert.assertTrue(codeAllowed);
    }

    @Test
    public void testIsCodeFiltered() {
        when(allowedInviteLinkManagement.allowedInviteLinkExists(serverUser, FULL_INVITE)).thenReturn(true);
        boolean codeAllowed = testUnit.isCodeFiltered(FULL_INVITE, serverUser);
        Assert.assertFalse(codeAllowed);
    }

    @Test
    public void testStoreFilteredInviteLinkUsage() {
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        FilteredInviteLink mockedFilteredInviteLink = Mockito.mock(FilteredInviteLink.class);
        when(mockedFilteredInviteLink.getUses()).thenReturn(1L);
        when(filteredInviteLinkManagement.findInviteLinkViaCode(SERVER_ID, FULL_INVITE)).thenReturn(Optional.of(mockedFilteredInviteLink));
        testUnit.storeFilteredInviteLinkUsage(FULL_INVITE, serverUser);
        verify(mockedFilteredInviteLink, times(1)).setUses(2L);
    }

    @Test
    public void testStoreFilteredInviteLinkUsageNotPresent() {
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(filteredInviteLinkManagement.findInviteLinkViaCode(SERVER_ID, FULL_INVITE)).thenReturn(Optional.empty());
        testUnit.storeFilteredInviteLinkUsage(FULL_INVITE, serverUser);
        verify(filteredInviteLinkManagement, times(1)).createFilteredInviteLink(server, FULL_INVITE);
    }

    @Test
    public void testAllowInvite() {
        when(allowedInviteLinkManagement.allowedInviteLinkExists(SERVER_ID, INVITE_CODE)).thenReturn(false);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        testUnit.allowInvite(FULL_INVITE, SERVER_ID);
        verify(allowedInviteLinkManagement, times(1)).createAllowedInviteLink(server, INVITE_CODE);
    }

    @Test(expected = InvalidInviteException.class)
    public void testAllowInviteIllegalInvite() {
        testUnit.allowInvite("#", SERVER_ID);
    }

    @Test
    public void testAllowInviteInviteCode() {
        when(allowedInviteLinkManagement.allowedInviteLinkExists(SERVER_ID, INVITE_CODE)).thenReturn(false);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        testUnit.allowInvite(INVITE_CODE, SERVER_ID);
        verify(allowedInviteLinkManagement, times(1)).createAllowedInviteLink(server, INVITE_CODE);
    }

    @Test
    public void testAllowInviteAlreadyPresent() {
        when(allowedInviteLinkManagement.allowedInviteLinkExists(SERVER_ID, INVITE_CODE)).thenReturn(true);
        testUnit.allowInvite(FULL_INVITE, SERVER_ID);
        verify(allowedInviteLinkManagement, times(0)).createAllowedInviteLink(any(AServer.class), anyString());
    }

    @Test
    public void testDisallowInvite() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        testUnit.disAllowInvite(FULL_INVITE, SERVER_ID);
        verify(allowedInviteLinkManagement, times(1)).removeAllowedInviteLink(server, INVITE_CODE);
    }

    @Test
    public void testClearAllTrackedInviteCodes() {
        testUnit.clearAllTrackedInviteCodes(SERVER_ID);
        verify(filteredInviteLinkManagement, times(1)).clearFilteredInviteLinks(SERVER_ID);
    }

    @Test
    public void testClearAllUses() {
        testUnit.clearAllUses(FULL_INVITE, SERVER_ID);
        verify(filteredInviteLinkManagement, times(1)).clearFilteredInviteLink(INVITE_CODE, SERVER_ID);
    }

    @Test
    public void testGetTopFilteredInviteLinksWithCount() {
        int count = 4;
        FilteredInviteLink mockedFilteredInviteLink = Mockito.mock(FilteredInviteLink.class);
        when(filteredInviteLinkManagement.getTopFilteredInviteLink(SERVER_ID, count)).thenReturn(Arrays.asList(mockedFilteredInviteLink));
        List<FilteredInviteLink> filteredInviteLinks = testUnit.getTopFilteredInviteLinks(SERVER_ID, count);
        Assert.assertEquals(1, filteredInviteLinks.size());
        Assert.assertEquals(mockedFilteredInviteLink, filteredInviteLinks.get(0));
    }

    @Test
    public void testGetTopFilteredInviteLinksDefaultCount() {
        FilteredInviteLink mockedFilteredInviteLink = Mockito.mock(FilteredInviteLink.class);
        when(filteredInviteLinkManagement.getTopFilteredInviteLink(SERVER_ID, 5)).thenReturn(Arrays.asList(mockedFilteredInviteLink));
        List<FilteredInviteLink> filteredInviteLinks = testUnit.getTopFilteredInviteLinks(SERVER_ID);
        Assert.assertEquals(1, filteredInviteLinks.size());
        Assert.assertEquals(mockedFilteredInviteLink, filteredInviteLinks.get(0));
    }

}
