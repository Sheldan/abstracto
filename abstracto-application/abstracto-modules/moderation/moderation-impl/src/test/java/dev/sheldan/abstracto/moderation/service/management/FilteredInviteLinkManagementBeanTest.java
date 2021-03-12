package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.model.database.FilteredInviteLink;
import dev.sheldan.abstracto.moderation.repository.FilteredInviteLinkRepository;
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

    private static final String INVITE = "invite";
    private static final Long SERVER_ID = 1L;

    @Captor
    private ArgumentCaptor<FilteredInviteLink> linkCaptor;

    @Test
    public void testCreateFilteredInviteLink() {
        when(repository.save(linkCaptor.capture())).thenReturn(mockedFilteredLink);
        FilteredInviteLink filteredInviteLink = testUnit.createFilteredInviteLink(server, INVITE);
        verify(repository, times(1)).save(linkCaptor.capture());
        Assert.assertEquals(filteredInviteLink, mockedFilteredLink);
        FilteredInviteLink repositoryFilteredInviteLink = linkCaptor.getValue();
        Assert.assertEquals(server, repositoryFilteredInviteLink.getServer());
        Assert.assertEquals(INVITE, repositoryFilteredInviteLink.getCode());
        Assert.assertEquals(1, repositoryFilteredInviteLink.getUses().intValue());
    }

    @Test
    public void testFindInviteLinkViaCode() {
        when(repository.findByCodeAndServer(INVITE, server)).thenReturn(Optional.of(mockedFilteredLink));
        Optional<FilteredInviteLink> filteredInviteLinkOptional = testUnit.findInviteLinkViaCode(server, INVITE);
        Assert.assertTrue(filteredInviteLinkOptional.isPresent());
        filteredInviteLinkOptional.ifPresent(filteredInviteLink -> Assert.assertEquals(mockedFilteredLink, filteredInviteLink));
    }

    @Test
    public void testFindInviteLinkViaCodeById() {
        when(repository.findByCodeAndServer_Id(INVITE, SERVER_ID)).thenReturn(Optional.of(mockedFilteredLink));
        Optional<FilteredInviteLink> filteredInviteLinkOptional = testUnit.findInviteLinkViaCode(SERVER_ID, INVITE);
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
        testUnit.clearFilteredInviteLink(INVITE, SERVER_ID);
        verify(repository, times(1)).deleteByCodeAndServer_Id(INVITE, SERVER_ID);
    }

    @Test
    public void testClearFilteredInviteLink() {
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.clearFilteredInviteLink(INVITE, server);
        verify(repository, times(1)).deleteByCodeAndServer_Id(INVITE, SERVER_ID);
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
