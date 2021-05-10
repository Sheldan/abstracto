package dev.sheldan.abstracto.invitefilter.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.invitefilter.exception.InvalidInviteException;
import dev.sheldan.abstracto.invitefilter.model.database.FilteredInviteLink;
import dev.sheldan.abstracto.invitefilter.service.management.AllowedInviteLinkManagement;
import dev.sheldan.abstracto.invitefilter.service.management.FilteredInviteLinkManagement;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Invite;
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
import java.util.concurrent.CompletableFuture;

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

    @Mock
    private JDA jda;

    @Mock
    private InviteLinkFilterServiceBean self;

    @Mock
    private Invite invite;

    private static final Long TARGET_SERVER_ID = 3L;
    private static final String CODE = "c";
    private static final String FULL_INVITE = "discord.gg/" + CODE;
    private static final Long SERVER_ID = 1L;
    private static final String SERVER_NAME = "test";

    @Test
    public void testIsCodeAllowedViaId() {
        when(allowedInviteLinkManagement.allowedInviteLinkExists(SERVER_ID, TARGET_SERVER_ID)).thenReturn(true);
        boolean codeAllowed = testUnit.isCodeAllowed(TARGET_SERVER_ID, SERVER_ID);
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
        when(filteredInviteLinkManagement.findInviteLinkViaTargetID(SERVER_ID, TARGET_SERVER_ID)).thenReturn(Optional.of(mockedFilteredInviteLink));
        testUnit.storeFilteredInviteLinkUsage(TARGET_SERVER_ID, SERVER_NAME, serverUser);
        verify(mockedFilteredInviteLink, times(1)).setUses(2L);
    }

    @Test
    public void testStoreFilteredInviteLinkUsageNotPresent() {
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(filteredInviteLinkManagement.findInviteLinkViaTargetID(SERVER_ID, TARGET_SERVER_ID)).thenReturn(Optional.empty());
        testUnit.storeFilteredInviteLinkUsage(TARGET_SERVER_ID, SERVER_NAME, serverUser);
        verify(filteredInviteLinkManagement, times(1)).createFilteredInviteLink(server, TARGET_SERVER_ID, SERVER_NAME);
    }

    @Test
    public void testAllowInvite() {
        when(self.resolveInvite(jda, CODE)).thenReturn(CompletableFuture.completedFuture(invite));
        testUnit.allowInvite(FULL_INVITE, SERVER_ID, jda);
        verify(self, times(1)).allowInviteInServer(SERVER_ID, invite);
    }

    @Test(expected = InvalidInviteException.class)
    public void testAllowInviteIllegalInvite() {
        testUnit.allowInvite("#", SERVER_ID, jda);
    }

    @Test
    public void testAllowInviteInviteCode() {
        when(self.resolveInvite(jda, CODE)).thenReturn(CompletableFuture.completedFuture(invite));
        testUnit.allowInvite(FULL_INVITE, SERVER_ID, jda);
        verify(self, times(1)).allowInviteInServer(SERVER_ID, invite);
    }

    @Test
    public void testAllowInviteAlreadyPresent() {
        when(self.resolveInvite(jda, CODE)).thenReturn(CompletableFuture.completedFuture(invite));
        testUnit.allowInvite(FULL_INVITE, SERVER_ID, jda);
        verify(self, times(1)).allowInviteInServer(SERVER_ID, invite);
    }

    @Test
    public void testDisallowInvite() {
        when(self.resolveInvite(jda, CODE)).thenReturn(CompletableFuture.completedFuture(invite));
        testUnit.disAllowInvite(FULL_INVITE, SERVER_ID, jda);
        verify(self, times(1)).disallowInviteInServer(SERVER_ID, invite);
    }

    @Test
    public void testClearAllTrackedInviteCodes() {
        testUnit.clearAllTrackedInviteCodes(SERVER_ID);
        verify(filteredInviteLinkManagement, times(1)).clearFilteredInviteLinks(SERVER_ID);
    }

    @Test
    public void testClearAllUses() {
        testUnit.clearAllUses(TARGET_SERVER_ID, SERVER_ID);
        verify(filteredInviteLinkManagement, times(1)).clearFilteredInviteLink(TARGET_SERVER_ID, SERVER_ID);
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
