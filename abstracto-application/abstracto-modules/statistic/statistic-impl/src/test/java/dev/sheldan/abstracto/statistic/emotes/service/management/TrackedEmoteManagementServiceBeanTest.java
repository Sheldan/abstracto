package dev.sheldan.abstracto.statistic.emotes.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.repository.TrackedEmoteRepository;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TrackedEmoteManagementServiceBeanTest {

    private static final Long SERVER_ID = 3L;
    private static final Long EMOTE_ID = 4L;
    private static final String EMOTE_NAME = "name";
    private static final Boolean ANIMATED = true;
    private static final String EXTERNAL_URL = "url";

    @InjectMocks
    private TrackedEmoteManagementServiceBean testUnit;

    @Mock
    private TrackedEmoteRepository repository;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private Emote emote;

    @Mock
    private Guild guild;

    @Mock
    private AServer server;

    @Mock
    private TrackedEmote trackedEmote;

    @Captor
    private ArgumentCaptor<TrackedEmote> trackedEmoteArgumentCaptor;

    @Test
    public void testCreateTrackedEmote() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(emote.getName()).thenReturn(EMOTE_NAME);
        when(emote.isAnimated()).thenReturn(ANIMATED);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.createTrackedEmote(emote, guild);
        verifyEmoteCreation(true, false, null);
    }

    @Test
    public void testCreateTrackedEmoteAllParams() {
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.createTrackedEmote(EMOTE_ID, EMOTE_NAME, ANIMATED, server);
        verifyEmoteCreation(true, false, null);
    }

    @Test
    public void testCreateExternalEmote() {
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.createExternalEmote(EMOTE_ID, EMOTE_NAME, EXTERNAL_URL, ANIMATED, server, true);
        verifyEmoteCreation(true, true, EXTERNAL_URL);
    }

    @Test
    public void testCreateTrackedEmoteExternal() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(emote.getName()).thenReturn(EMOTE_NAME);
        when(emote.getImageUrl()).thenReturn(EXTERNAL_URL);
        when(emote.isAnimated()).thenReturn(ANIMATED);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.createTrackedEmote(emote, guild, true);
        verifyEmoteCreation(true, true, EXTERNAL_URL);
    }

    @Test
    public void testCreateTrackedEmoteInternal() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(emote.getName()).thenReturn(EMOTE_NAME);
        when(emote.isAnimated()).thenReturn(ANIMATED);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.createTrackedEmote(emote, guild, false);
        verifyEmoteCreation(true, false, null);
    }

    @Test
    public void testCreateNotTrackedEmote() {
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.createNotTrackedEmote(EMOTE_ID, EMOTE_NAME, ANIMATED, server);
        verifyEmoteCreation(false, false, null);
    }

    @Test
    public void testCreateExternalEmotePersistingEmote() {
        when(server.getId()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        PersistingEmote persistingEmote = Mockito.mock(PersistingEmote.class);
        when(persistingEmote.getServerId()).thenReturn(SERVER_ID);
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(persistingEmote.getEmoteName()).thenReturn(EMOTE_NAME);
        when(persistingEmote.getAnimated()).thenReturn(ANIMATED);
        when(persistingEmote.getExternalUrl()).thenReturn(EXTERNAL_URL);
        testUnit.createExternalTrackedEmote(persistingEmote);
        verifyEmoteCreation(true, true, EXTERNAL_URL);
    }

    @Test
    public void testCreateExternalEmoteDirect() {
        when(emote.getImageUrl()).thenReturn(EXTERNAL_URL);
        when(emote.isAnimated()).thenReturn(ANIMATED);
        when(emote.getName()).thenReturn(EMOTE_NAME);
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(server.getId()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        testUnit.createExternalTrackedEmote(emote, guild);
        verifyEmoteCreation(true, true, EXTERNAL_URL);
    }

    public void verifyEmoteCreation(boolean tracked, boolean external, String externalUrl) {
        verify(repository, times(1)).save(trackedEmoteArgumentCaptor.capture());
        TrackedEmote createdTrackedEmote = trackedEmoteArgumentCaptor.getValue();
        Assert.assertEquals(EMOTE_ID, createdTrackedEmote.getTrackedEmoteId().getId());
        Assert.assertEquals(SERVER_ID, createdTrackedEmote.getTrackedEmoteId().getServerId());
        Assert.assertEquals(EMOTE_NAME, createdTrackedEmote.getEmoteName());
        Assert.assertEquals(ANIMATED, createdTrackedEmote.getAnimated());
        Assert.assertEquals(tracked, createdTrackedEmote.getTrackingEnabled());
        Assert.assertEquals(server, createdTrackedEmote.getServer());
        Assert.assertFalse(createdTrackedEmote.getDeleted());
        Assert.assertEquals(external, createdTrackedEmote.getExternal());
        Assert.assertEquals(externalUrl, createdTrackedEmote.getExternalUrl());
    }

    @Test
    public void testMarkAsDeleted() {
        ServerSpecificId trackedEmoteServer = new ServerSpecificId(SERVER_ID, EMOTE_ID);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        testUnit.markAsDeleted(trackedEmote);
        verify(trackedEmote, times(1)).setDeleted(true);
    }

    @Test
    public void testMarkAsDeletedId() {
        ServerSpecificId trackedEmoteServer = new ServerSpecificId(SERVER_ID, EMOTE_ID);
        when(repository.findById(trackedEmoteServer)).thenReturn(Optional.of(trackedEmote));
        when(trackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        testUnit.markAsDeleted(SERVER_ID, EMOTE_ID);
        verify(trackedEmote, times(1)).setDeleted(true);
    }

    @Test
    public void testLoadByEmoteEmote() {
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(emote.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        ServerSpecificId trackedEmoteServer = new ServerSpecificId(SERVER_ID, EMOTE_ID);
        when(repository.findById(trackedEmoteServer)).thenReturn(Optional.of(trackedEmote));
        TrackedEmote retrievedTrackedEmote = testUnit.loadByEmote(emote);
        Assert.assertEquals(trackedEmote, retrievedTrackedEmote);
    }

    @Test
    public void testLoadByEmoteId() {
        ServerSpecificId trackedEmoteServer = new ServerSpecificId(SERVER_ID, EMOTE_ID);
        when(repository.findById(trackedEmoteServer)).thenReturn(Optional.of(trackedEmote));
        TrackedEmote retrievedEmote = testUnit.loadByEmoteId(EMOTE_ID, SERVER_ID);
        Assert.assertEquals(trackedEmote, retrievedEmote);
    }

    @Test(expected = AbstractoRunTimeException.class)
    public void testLoadByEmoteIdNotFound() {
        ServerSpecificId trackedEmoteServer = new ServerSpecificId(SERVER_ID, EMOTE_ID);
        when(repository.findById(trackedEmoteServer)).thenReturn(Optional.empty());
        testUnit.loadByEmoteId(EMOTE_ID, SERVER_ID);
    }

    @Test
    public void testTrackedEmoteExists() {
        ServerSpecificId trackedEmoteServer = new ServerSpecificId(SERVER_ID, EMOTE_ID);
        when(repository.findById(trackedEmoteServer)).thenReturn(Optional.of(trackedEmote));
        boolean exists = testUnit.trackedEmoteExists(EMOTE_ID, SERVER_ID);
        Assert.assertTrue(exists);
    }

    @Test
    public void testTrackedEmoteExistsNot() {
        ServerSpecificId trackedEmoteServer = new ServerSpecificId(SERVER_ID, EMOTE_ID);
        when(repository.findById(trackedEmoteServer)).thenReturn(Optional.empty());
        boolean exists = testUnit.trackedEmoteExists(EMOTE_ID, SERVER_ID);
        Assert.assertFalse(exists);
    }

    @Test
    public void testLoadByTrackedEmoteServer() {
        ServerSpecificId trackedEmoteServer = new ServerSpecificId(SERVER_ID, EMOTE_ID);
        when(repository.findById(trackedEmoteServer)).thenReturn(Optional.of(trackedEmote));
        TrackedEmote retrievedTrackedEmote = testUnit.loadByTrackedEmoteServer(trackedEmoteServer);
        Assert.assertEquals(trackedEmote, retrievedTrackedEmote);
    }

    @Test
    public void testGetAllActiveTrackedEmoteForServer() {
        List<TrackedEmote> controlTrackedEmotes = Arrays.asList(trackedEmote);
        when(repository.findByTrackedEmoteId_ServerIdAndDeletedFalseAndExternalFalse(SERVER_ID)).thenReturn(controlTrackedEmotes);
        List<TrackedEmote> retrievedTrackedEmotes = testUnit.getAllActiveTrackedEmoteForServer(SERVER_ID);
        Assert.assertEquals(controlTrackedEmotes, retrievedTrackedEmotes);
    }

    @Test
    public void testGetAllActiveTrackedEmoteForServerId() {
        when(server.getId()).thenReturn(SERVER_ID);
        List<TrackedEmote> controlTrackedEmotes = Arrays.asList(trackedEmote);
        when(repository.findByTrackedEmoteId_ServerIdAndDeletedFalseAndExternalFalse(SERVER_ID)).thenReturn(controlTrackedEmotes);
        List<TrackedEmote> retrievedTrackedEmotes = testUnit.getAllActiveTrackedEmoteForServer(server);
        Assert.assertEquals(controlTrackedEmotes, retrievedTrackedEmotes);
    }

    @Test
    public void testGetTrackedEmoteForServerGetAll() {
        List<TrackedEmote> controlTrackedEmotes = Arrays.asList(trackedEmote);
        when(repository.findByTrackedEmoteId_ServerId(SERVER_ID)).thenReturn(controlTrackedEmotes);
        List<TrackedEmote> retrievedTrackedEmotes = testUnit.getTrackedEmoteForServer(SERVER_ID, true);
        Assert.assertEquals(controlTrackedEmotes, retrievedTrackedEmotes);
    }

    @Test
    public void testGetTrackedEmoteForServerGet() {
        List<TrackedEmote> controlTrackedEmotes = Arrays.asList(trackedEmote);
        when(repository.findByTrackedEmoteId_ServerIdAndTrackingEnabledTrue(SERVER_ID)).thenReturn(controlTrackedEmotes);
        List<TrackedEmote> retrievedTrackedEmotes = testUnit.getTrackedEmoteForServer(SERVER_ID, false);
        Assert.assertEquals(controlTrackedEmotes, retrievedTrackedEmotes);
    }

    @Test
    public void testSetName() {
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        testUnit.changeName(trackedEmote, EMOTE_NAME);
        verify(trackedEmote, times(1)).setEmoteName(EMOTE_NAME);
    }

    @Test
    public void testDisableTrackedEmote() {
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        testUnit.disableTrackedEmote(trackedEmote);
        verify(trackedEmote, times(1)).setTrackingEnabled(false);
    }

    @Test
    public void testEnableTrackedEmote() {
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        testUnit.enableTrackedEmote(trackedEmote);
        verify(trackedEmote, times(1)).setTrackingEnabled(true);
    }

    @Test
    public void testDeleteTrackedEmote() {
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        testUnit.deleteTrackedEmote(trackedEmote);
        verify(repository, times(1)).delete(trackedEmote);
    }

}
