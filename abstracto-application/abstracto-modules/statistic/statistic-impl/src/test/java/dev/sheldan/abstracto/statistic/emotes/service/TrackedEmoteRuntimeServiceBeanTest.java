package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TrackedEmoteRuntimeServiceBeanTest {

    @InjectMocks
    @Spy
    private TrackedEmoteRuntimeServiceBean testUnit;

    @Mock
    private TrackedEmoteRunTimeStorage trackedEmoteRunTimeStorage;

    @Mock
    private Guild guild;

    @Mock
    private Emote emote;

    @Mock
    private PersistingEmote persistingEmote;

    @Captor
    private ArgumentCaptor<HashMap<Long, List<PersistingEmote>>> putCaptor;

    private static final Long COUNT = 4L;
    private static final String URL = "url";
    private static final Long EMOTE_ID = 8L;
    private static final Long SECOND = 9L;
    private static final Long SERVER_ID = 45L;

    @Test
    public void testCreateFromEmoteFromGuild() {
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        PersistingEmote createdEmote = testUnit.createFromEmote(guild, emote, COUNT, false);
        Assert.assertFalse(createdEmote.getExternal());
        Assert.assertNull(createdEmote.getExternalUrl());
        Assert.assertEquals(EMOTE_ID, createdEmote.getEmoteId());
        Assert.assertEquals(COUNT, createdEmote.getCount());
    }

    @Test
    public void testCreateFromEmoteExternal() {
        when(emote.getImageUrl()).thenReturn(URL);
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        PersistingEmote createdEmote = testUnit.createFromEmote(guild, emote, COUNT, true);
        Assert.assertTrue(createdEmote.getExternal());
        Assert.assertEquals(URL, createdEmote.getExternalUrl());
        Assert.assertEquals(EMOTE_ID, createdEmote.getEmoteId());
        Assert.assertEquals(COUNT, createdEmote.getCount());
    }

    @Test
    public void testCreateFromEmoteOneCountFromGuild() {
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        PersistingEmote createdEmote = testUnit.createFromEmote(guild, emote, false);
        Assert.assertFalse(createdEmote.getExternal());
        Assert.assertNull(createdEmote.getExternalUrl());
        Assert.assertEquals(EMOTE_ID, createdEmote.getEmoteId());
        Assert.assertEquals(1, createdEmote.getCount().longValue());
    }

    @Test
    public void testCreateFromEmoteOneCountExternal() {
        when(emote.getImageUrl()).thenReturn(URL);
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        PersistingEmote createdEmote = testUnit.createFromEmote(guild, emote, true);
        Assert.assertTrue(createdEmote.getExternal());
        Assert.assertEquals(URL, createdEmote.getExternalUrl());
        Assert.assertEquals(EMOTE_ID, createdEmote.getEmoteId());
        Assert.assertEquals(1, createdEmote.getCount().longValue());
    }

    @Test
    public void testEmoteForServerWithoutExistingSecond() {
        doReturn(SECOND).when(testUnit).getKey();
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmoteRunTimeStorage.contains(SECOND)).thenReturn(false);
        testUnit.addEmoteForServer(emote, guild, false);
        verify(trackedEmoteRunTimeStorage, times(1)).put(eq(SECOND), putCaptor.capture());
        HashMap<Long, List<PersistingEmote>> value = putCaptor.getValue();
        Assert.assertEquals(1, value.keySet().size());
        Assert.assertEquals(SERVER_ID, value.keySet().iterator().next());
        List<PersistingEmote> createdEmotes = value.values().iterator().next();
        Assert.assertEquals(1, createdEmotes.size());
        Assert.assertEquals(EMOTE_ID, createdEmotes.get(0).getEmoteId());
    }

    @Test
    public void testEmoteForServerWithExistingSecondButNotServer() {
        doReturn(SECOND).when(testUnit).getKey();
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmoteRunTimeStorage.contains(SECOND)).thenReturn(true);
        HashMap<Long, List<PersistingEmote>> serverMap = new HashMap<>();
        when(trackedEmoteRunTimeStorage.get(SECOND)).thenReturn(serverMap);
        testUnit.addEmoteForServer(emote, guild, false);
        Assert.assertEquals(1, serverMap.keySet().size());
        Assert.assertEquals(SERVER_ID, serverMap.keySet().iterator().next());
        List<PersistingEmote> createdEmotes = serverMap.values().iterator().next();
        Assert.assertEquals(1, createdEmotes.size());
        Assert.assertEquals(EMOTE_ID, createdEmotes.get(0).getEmoteId());
    }

    @Test
    public void testEmoteForServerWithExistingSecondAndServerButNotEmote() {
        doReturn(SECOND).when(testUnit).getKey();
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmoteRunTimeStorage.contains(SECOND)).thenReturn(true);
        HashMap<Long, List<PersistingEmote>> serverMap = new HashMap<>();
        serverMap.put(SERVER_ID, new ArrayList<>(Arrays.asList(persistingEmote)));
        when(trackedEmoteRunTimeStorage.get(SECOND)).thenReturn(serverMap);
        testUnit.addEmoteForServer(emote, guild, false);
        Assert.assertEquals(1, serverMap.keySet().size());
        Assert.assertEquals(SERVER_ID, serverMap.keySet().iterator().next());
        List<PersistingEmote> persistingEmotes = serverMap.values().iterator().next();
        Assert.assertEquals(2, persistingEmotes.size());
        Assert.assertEquals(persistingEmote, persistingEmotes.get(0));
        Assert.assertEquals(EMOTE_ID, persistingEmotes.get(1).getEmoteId());
    }

    @Test
    public void testEmoteForServerWithExistingSecondAndServerAndEmote() {
        doReturn(SECOND).when(testUnit).getKey();
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmoteRunTimeStorage.contains(SECOND)).thenReturn(true);
        HashMap<Long, List<PersistingEmote>> serverMap = new HashMap<>();
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(persistingEmote.getCount()).thenReturn(COUNT);
        serverMap.put(SERVER_ID, new ArrayList<>(Arrays.asList(persistingEmote)));
        when(trackedEmoteRunTimeStorage.get(SECOND)).thenReturn(serverMap);
        testUnit.addEmoteForServer(emote, guild, false);
        Assert.assertEquals(1, serverMap.keySet().size());
        Assert.assertEquals(SERVER_ID, serverMap.keySet().iterator().next());
        List<PersistingEmote> persistingEmotes = serverMap.values().iterator().next();
        Assert.assertEquals(1, persistingEmotes.size());
        PersistingEmote persistedEmote = persistingEmotes.get(0);
        Assert.assertEquals(EMOTE_ID, persistedEmote.getEmoteId());
        verify(persistedEmote, times(1)).setCount(COUNT + 1);
    }

    @Test
    public void testEmoteForServerWithExistingSecondAndServerAndEmoteCustomCount() {
        doReturn(SECOND).when(testUnit).getKey();
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmoteRunTimeStorage.contains(SECOND)).thenReturn(true);
        HashMap<Long, List<PersistingEmote>> serverMap = new HashMap<>();
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(persistingEmote.getCount()).thenReturn(COUNT);
        serverMap.put(SERVER_ID, new ArrayList<>(Arrays.asList(persistingEmote)));
        when(trackedEmoteRunTimeStorage.get(SECOND)).thenReturn(serverMap);
        testUnit.addEmoteForServer(emote, guild, COUNT, false);
        Assert.assertEquals(1, serverMap.keySet().size());
        Assert.assertEquals(SERVER_ID, serverMap.keySet().iterator().next());
        List<PersistingEmote> persistingEmotes = serverMap.values().iterator().next();
        Assert.assertEquals(1, persistingEmotes.size());
        PersistingEmote persistedEmote = persistingEmotes.get(0);
        Assert.assertEquals(EMOTE_ID, persistedEmote.getEmoteId());
        verify(persistedEmote, times(1)).setCount(COUNT + COUNT);
    }

}
