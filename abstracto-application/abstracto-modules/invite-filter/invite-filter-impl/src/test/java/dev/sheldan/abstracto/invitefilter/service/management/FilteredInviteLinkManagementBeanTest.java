package dev.sheldan.abstracto.invitefilter.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.invitefilter.model.database.FilteredInviteLink;
import dev.sheldan.abstracto.invitefilter.repository.FilteredInviteLinkRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FilteredInviteLinkManagementBeanTest {

    @InjectMocks
    private FilteredInviteLinkManagementBean testUnit;

    @Mock
    private FilteredInviteLinkRepository repository;

    @Mock
    private AServer server;

    @Mock
    private FilteredInviteLink mockedFilteredLink;

    private static final Long TARGET_SERVER_ID = 2L;
    private static final Long SERVER_ID = 1L;
    private static final String SERVER_NAME = "serverName";

    @Captor
    private ArgumentCaptor<FilteredInviteLink> linkCaptor;

    @Test
    public void testCreateFilteredInviteLink() {
        when(repository.save(linkCaptor.capture())).thenReturn(mockedFilteredLink);
        FilteredInviteLink filteredInviteLink = testUnit.createFilteredInviteLink(server, TARGET_SERVER_ID, SERVER_NAME);
        verify(repository, times(1)).save(linkCaptor.capture());
        Assert.assertEquals(filteredInviteLink, mockedFilteredLink);
        FilteredInviteLink repositoryFilteredInviteLink = linkCaptor.getValue();
        Assert.assertEquals(server, repositoryFilteredInviteLink.getServer());
        Assert.assertEquals(TARGET_SERVER_ID, repositoryFilteredInviteLink.getTargetServerId());
        Assert.assertEquals(1, repositoryFilteredInviteLink.getUses().intValue());
    }

    @Test
    public void testFindInviteLinkViaCode() {
        when(repository.findByTargetServerIdAndServer(TARGET_SERVER_ID, server)).thenReturn(Optional.of(mockedFilteredLink));
        Optional<FilteredInviteLink> filteredInviteLinkOptional = testUnit.findInviteLinkViaTargetID(server, TARGET_SERVER_ID);
        Assert.assertTrue(filteredInviteLinkOptional.isPresent());
        filteredInviteLinkOptional.ifPresent(filteredInviteLink -> Assert.assertEquals(mockedFilteredLink, filteredInviteLink));
    }

    @Test
    public void testFindInviteLinkViaCodeById() {
        when(repository.findByTargetServerIdAndServer_Id(TARGET_SERVER_ID, SERVER_ID)).thenReturn(Optional.of(mockedFilteredLink));
        Optional<FilteredInviteLink> filteredInviteLinkOptional = testUnit.findInviteLinkViaTargetID(SERVER_ID, TARGET_SERVER_ID);
        Assert.assertTrue(filteredInviteLinkOptional.isPresent());
        filteredInviteLinkOptional.ifPresent(filteredInviteLink -> Assert.assertEquals(mockedFilteredLink, filteredInviteLink));
    }

    @Test
    public void testClearFilteredInviteLinksViaId() {
        testUnit.clearFilteredInviteLinks(SERVER_ID);
        verify(repository, times(1)).deleteByServer_Id(SERVER_ID);
    }

    @Test
    public void testClearFilteredInviteLinks() {
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.clearFilteredInviteLinks(server);
        verify(repository, times(1)).deleteByServer_Id(SERVER_ID);
    }

    @Test
    public void testClearFilteredInviteLinkViaId() {
        testUnit.clearFilteredInviteLink(TARGET_SERVER_ID, SERVER_ID);
        verify(repository, times(1)).deleteByTargetServerIdAndServer_Id(TARGET_SERVER_ID, SERVER_ID);
    }

    @Test
    public void testClearFilteredInviteLink() {
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.clearFilteredInviteLink(TARGET_SERVER_ID, server);
        verify(repository, times(1)).deleteByTargetServerIdAndServer_Id(TARGET_SERVER_ID, SERVER_ID);
    }

    @Test
    public void testGetTopFilteredInviteLink() {
        Integer count = 4;
        testUnit.getTopFilteredInviteLink(SERVER_ID, count);
        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository, times(1)).findAllByServer_IdOrderByUsesDesc(eq(SERVER_ID), pageableArgumentCaptor.capture());
        Pageable paginator = pageableArgumentCaptor.getValue();
        Assert.assertEquals(count.intValue(), paginator.getPageSize());
        Assert.assertEquals(0, paginator.getPageNumber());
    }
}
