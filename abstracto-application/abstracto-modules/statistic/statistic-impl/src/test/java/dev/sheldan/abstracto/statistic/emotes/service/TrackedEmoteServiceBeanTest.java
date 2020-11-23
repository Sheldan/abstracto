package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emotes.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emotes.model.TrackedEmoteSynchronizationResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import dev.sheldan.abstracto.statistic.emotes.service.management.UsedEmoteManagementService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TrackedEmoteServiceBeanTest {

    private static final Long COUNT = 2L;
    private static final Long SERVER_ID = 3L;
    private static final Long EMOTE_ID = 5L;
    private static final Long EMOTE_ID_2 = 6L;
    @InjectMocks
    private TrackedEmoteServiceBean testUnit;

    @Mock
    private TrackedEmoteRuntimeService trackedEmoteRuntimeService;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Mock
    private UsedEmoteManagementService usedEmoteManagementService;

    @Mock
    private BotService botService;

    @Mock
    private Emote emote;

    @Mock
    private Emote secondEmote;

    @Mock
    private Guild guild;

    @Mock
    private TrackedEmote trackedEmote;

    @Mock
    private TrackedEmote trackedEmote2;

    @Mock
    private ServerSpecificId trackedEmoteServer;

    @Mock
    private TrackedEmote secondTrackedEmote;

    @Mock
    private PersistingEmote persistingEmote;

    @Mock
    private PersistingEmote persistingEmote2;

    @Mock
    private UsedEmote usedEmote;

    @Mock
    private ServerSpecificId secondTrackedEmoteServer;

    @Captor
    private ArgumentCaptor<Emote> emoteArgumentCaptor;

    @Captor
    private ArgumentCaptor<Boolean> booleanArgumentCaptor;

    @Test
    public void addSingleServerEmote() {
        externalEmotesEnabled(true);
        isEmoteExternal(false);
        testUnit.addEmoteToRuntimeStorage(emote, guild, COUNT);
        verify(trackedEmoteRuntimeService, times(1)).addEmoteForServer(emote, guild, COUNT,false);
    }

    @Test
    public void addSingleExternalEmote() {
        externalEmotesEnabled(true);
        isEmoteExternal(true);
        testUnit.addEmoteToRuntimeStorage(emote, guild, COUNT);
        verify(trackedEmoteRuntimeService, times(1)).addEmoteForServer(emote, guild, COUNT,true);
    }

    @Test
    public void addSingleExternalWhenExternalDisabled() {
        externalEmotesEnabled(false);
        isEmoteExternal(true);
        testUnit.addEmoteToRuntimeStorage(emote, guild, COUNT);
        verify(trackedEmoteRuntimeService, times(0)).addEmoteForServer(eq(emote), eq(guild), anyBoolean());
    }

    @Test
    public void addSingleServerEmoteExternalDisabled() {
        externalEmotesEnabled(false);
        isEmoteExternal(false);
        testUnit.addEmoteToRuntimeStorage(emote, guild, COUNT);
        verify(trackedEmoteRuntimeService, times(1)).addEmoteForServer(emote, guild, COUNT, false);
    }

    @Test
    public void addTwoExternalEmotes() {
        externalEmotesEnabled(true);
        bothEmotesExternal(true, true);
        testUnit.addEmoteToRuntimeStorage(Arrays.asList(emote, secondEmote), guild);
        verify(trackedEmoteRuntimeService, times(2)).addEmoteForServer(emoteArgumentCaptor.capture(), eq(guild), eq(true));
        List<Emote> usedEmotes = emoteArgumentCaptor.getAllValues();
        Assert.assertEquals(2, usedEmotes.size());
        Assert.assertEquals(emote, usedEmotes.get(0));
        Assert.assertEquals(secondEmote, usedEmotes.get(1));
    }

    @Test
    public void addOneExternalAndOneLocalEmote() {
        externalEmotesEnabled(true);
        bothEmotesExternal(true, false);
        testUnit.addEmoteToRuntimeStorage(Arrays.asList(emote, secondEmote), guild);
        verify(trackedEmoteRuntimeService, times(2)).addEmoteForServer(emoteArgumentCaptor.capture(), eq(guild), booleanArgumentCaptor.capture());
        List<Emote> usedEmotes = emoteArgumentCaptor.getAllValues();
        Assert.assertEquals(2, usedEmotes.size());
        Assert.assertEquals(emote, usedEmotes.get(0));
        Assert.assertEquals(secondEmote, usedEmotes.get(1));
        List<Boolean> externalValues = booleanArgumentCaptor.getAllValues();
        Assert.assertEquals(2, externalValues.size());
        Assert.assertTrue(externalValues.get(0));
        Assert.assertFalse(externalValues.get(1));
    }

    @Test
    public void addTwoExternalEmotesWhenExternalDisabled() {
        externalEmotesEnabled(false);
        bothEmotesExternal(true, true);
        testUnit.addEmoteToRuntimeStorage(Arrays.asList(emote, secondEmote), guild);
        verify(trackedEmoteRuntimeService, times(0)).addEmoteForServer(emoteArgumentCaptor.capture(), eq(guild), booleanArgumentCaptor.capture());
    }

    @Test
    public void addTwoLocalEmotes() {
        externalEmotesEnabled(false);
        bothEmotesExternal(false, false);
        testUnit.addEmoteToRuntimeStorage(Arrays.asList(emote, secondEmote), guild);
        verify(trackedEmoteRuntimeService, times(2)).addEmoteForServer(emoteArgumentCaptor.capture(), eq(guild), booleanArgumentCaptor.capture());
        List<Emote> usedEmotes = emoteArgumentCaptor.getAllValues();
        Assert.assertEquals(2, usedEmotes.size());
        Assert.assertEquals(emote, usedEmotes.get(0));
        Assert.assertEquals(secondEmote, usedEmotes.get(1));
        List<Boolean> externalValues = booleanArgumentCaptor.getAllValues();
        Assert.assertEquals(2, externalValues.size());
        Assert.assertFalse(externalValues.get(0));
        Assert.assertFalse(externalValues.get(1));
    }

    public void bothEmotesExternal(boolean external, boolean external2) {
        when(emoteService.emoteIsFromGuild(emote, guild)).thenReturn(!external);
        when(emoteService.emoteIsFromGuild(secondEmote, guild)).thenReturn(!external2);
    }

    public void isEmoteExternal(boolean external) {
        when(emoteService.emoteIsFromGuild(emote, guild)).thenReturn(!external);
    }

    public void externalEmotesEnabled(boolean external) {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(external);
    }

    @Test
    public void testCrateFakeEmote() {
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        TrackedEmote fakeTrackedEmote = testUnit.getFakeTrackedEmote(emote, guild);
        Assert.assertTrue(fakeTrackedEmote.isFake());
        Assert.assertEquals(EMOTE_ID, fakeTrackedEmote.getTrackedEmoteId().getId());
        Assert.assertEquals(SERVER_ID, fakeTrackedEmote.getTrackedEmoteId().getServerId());
    }

    @Test
    public void testCrateFakeEmoteViaId() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        TrackedEmote fakedTrackedEmote = testUnit.getFakeTrackedEmote(EMOTE_ID, guild);
        Assert.assertTrue(fakedTrackedEmote.isFake());
        Assert.assertEquals(EMOTE_ID, fakedTrackedEmote.getTrackedEmoteId().getId());
        Assert.assertEquals(SERVER_ID, fakedTrackedEmote.getTrackedEmoteId().getServerId());
    }

    @Test
    public void testSynchronizeTrackedEmotesNoNewEmotes() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteServer.getServerId()).thenReturn(SERVER_ID);
        when(trackedEmoteServer.getId()).thenReturn(EMOTE_ID);
        when(secondTrackedEmote.getTrackedEmoteId()).thenReturn(secondTrackedEmoteServer);
        when(secondTrackedEmoteServer.getServerId()).thenReturn(SERVER_ID);
        when(secondTrackedEmoteServer.getId()).thenReturn(EMOTE_ID_2);
        when(guild.getEmotes()).thenReturn(Arrays.asList(emote, secondEmote));
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(secondEmote.getIdLong()).thenReturn(EMOTE_ID_2);
        when(trackedEmoteManagementService.getAllActiveTrackedEmoteForServer(SERVER_ID)).thenReturn(new ArrayList<>(Arrays.asList(trackedEmote, secondTrackedEmote)));
        TrackedEmoteSynchronizationResult result = testUnit.synchronizeTrackedEmotes(guild);
        Assert.assertEquals(0L, result.getEmotesAdded().longValue());
        Assert.assertEquals(0L, result.getEmotesMarkedDeleted().longValue());
        verify(trackedEmoteManagementService, times(0)).createTrackedEmote(any(Emote.class), any(Guild.class));
        verify(trackedEmoteManagementService, times(0)).markAsDeleted(any(TrackedEmote.class));
    }

    @Test
    public void testSynchronizeTrackedEmotesOneNewEmote() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteServer.getServerId()).thenReturn(SERVER_ID);
        when(trackedEmoteServer.getId()).thenReturn(EMOTE_ID);
        when(guild.getEmotes()).thenReturn(Arrays.asList(emote, secondEmote));
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(trackedEmoteManagementService.getAllActiveTrackedEmoteForServer(SERVER_ID)).thenReturn(new ArrayList<>(Arrays.asList(trackedEmote)));
        TrackedEmoteSynchronizationResult result = testUnit.synchronizeTrackedEmotes(guild);
        Assert.assertEquals(1L, result.getEmotesAdded().longValue());
        Assert.assertEquals(0L, result.getEmotesMarkedDeleted().longValue());
        verify(trackedEmoteManagementService, times(1)).createTrackedEmote(secondEmote, guild);
        verify(trackedEmoteManagementService, times(0)).markAsDeleted(any(TrackedEmote.class));
    }

    @Test
    public void testSynchronizeTrackedEmotesWithEmotesDeleted() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteServer.getServerId()).thenReturn(SERVER_ID);
        when(trackedEmoteServer.getId()).thenReturn(EMOTE_ID);
        when(guild.getEmotes()).thenReturn(Arrays.asList(emote));
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(trackedEmoteManagementService.getAllActiveTrackedEmoteForServer(SERVER_ID)).thenReturn(new ArrayList<>(Arrays.asList(trackedEmote, secondTrackedEmote)));
        TrackedEmoteSynchronizationResult result = testUnit.synchronizeTrackedEmotes(guild);
        Assert.assertEquals(0L, result.getEmotesAdded().longValue());
        Assert.assertEquals(1L, result.getEmotesMarkedDeleted().longValue());
        verify(trackedEmoteManagementService, times(0)).createTrackedEmote(any(Emote.class), any(Guild.class));
        verify(trackedEmoteManagementService, times(1)).markAsDeleted(secondTrackedEmote);
    }

    @Test
    public void testSynchronizeTrackedEmotesNoEmotesLeft() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(guild.getEmotes()).thenReturn(new ArrayList<>());
        when(trackedEmoteManagementService.getAllActiveTrackedEmoteForServer(SERVER_ID)).thenReturn(new ArrayList<>(Arrays.asList(trackedEmote, secondTrackedEmote)));
        TrackedEmoteSynchronizationResult result = testUnit.synchronizeTrackedEmotes(guild);
        Assert.assertEquals(0L, result.getEmotesAdded().longValue());
        Assert.assertEquals(2L, result.getEmotesMarkedDeleted().longValue());
        verify(trackedEmoteManagementService, times(0)).createTrackedEmote(any(Emote.class), any(Guild.class));
        verify(trackedEmoteManagementService, times(2)).markAsDeleted(any(TrackedEmote.class));
    }

    @Test
    public void testSynchronizeTrackedEmotesAllEmotesAreNew() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(guild.getEmotes()).thenReturn(Arrays.asList(emote, secondEmote));
        when(trackedEmoteManagementService.getAllActiveTrackedEmoteForServer(SERVER_ID)).thenReturn(new ArrayList<>());
        TrackedEmoteSynchronizationResult result = testUnit.synchronizeTrackedEmotes(guild);
        Assert.assertEquals(2L, result.getEmotesAdded().longValue());
        Assert.assertEquals(0L, result.getEmotesMarkedDeleted().longValue());
        verify(trackedEmoteManagementService, times(2)).createTrackedEmote(any(Emote.class), any(Guild.class));
        verify(trackedEmoteManagementService, times(0)).markAsDeleted(any(TrackedEmote.class));
    }

    @Test
    public void testStoreEmptyEmoteStatistics() {
        HashMap<Long, List<PersistingEmote>> usagesToStore = new HashMap<>();
        testUnit.storeEmoteStatistics(usagesToStore);
        verify(featureModeService, times(0)).featureModeActive(eq(StatisticFeatures.EMOTE_TRACKING), anyLong(), eq(EmoteTrackingMode.AUTO_TRACK));
    }

    @Test
    public void testStoreStatisticOneServerExistingInternalEmoteUsageExistsYetTrackingEnabled() {
        HashMap<Long, List<PersistingEmote>> usagesToStore = new HashMap<>();
        usagesToStore.put(SERVER_ID, Arrays.asList(persistingEmote));
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(persistingEmote.getCount()).thenReturn(COUNT);
        when(trackedEmoteManagementService.loadByEmoteIdOptional(EMOTE_ID, SERVER_ID)).thenReturn(Optional.of(trackedEmote));
        when(usedEmoteManagementService.loadUsedEmoteForTrackedEmoteToday(trackedEmote)).thenReturn(Optional.of(usedEmote));
        when(trackedEmote.getTrackingEnabled()).thenReturn(true);
        when(usedEmote.getAmount()).thenReturn(COUNT);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.AUTO_TRACK_EXTERNAL)).thenReturn(true);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        testUnit.storeEmoteStatistics(usagesToStore);
        verify(usedEmote, times(1)).setAmount(2 * COUNT);
    }

    @Test
    public void testStoreStatisticOneServerExistingInternalEmoteUsageExistsTrackingDisabled() {
        HashMap<Long, List<PersistingEmote>> usagesToStore = new HashMap<>();
        usagesToStore.put(SERVER_ID, Arrays.asList(persistingEmote));
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(trackedEmoteManagementService.loadByEmoteIdOptional(EMOTE_ID, SERVER_ID)).thenReturn(Optional.of(trackedEmote));
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        when(trackedEmote.getTrackingEnabled()).thenReturn(false);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.AUTO_TRACK_EXTERNAL)).thenReturn(true);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        testUnit.storeEmoteStatistics(usagesToStore);
        verify(usedEmote, times(0)).setAmount(anyLong());
    }

    @Test
    public void testStoreStatisticOneServerExistingInternalEmoteNoUsageYetTrackingEnabled() {
        HashMap<Long, List<PersistingEmote>> usagesToStore = new HashMap<>();
        usagesToStore.put(SERVER_ID, Arrays.asList(persistingEmote));
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(persistingEmote.getCount()).thenReturn(COUNT);
        when(trackedEmoteManagementService.loadByEmoteIdOptional(EMOTE_ID, SERVER_ID)).thenReturn(Optional.of(trackedEmote));
        when(usedEmoteManagementService.loadUsedEmoteForTrackedEmoteToday(trackedEmote)).thenReturn(Optional.empty());
        when(trackedEmote.getTrackingEnabled()).thenReturn(true);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.AUTO_TRACK_EXTERNAL)).thenReturn(true);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        testUnit.storeEmoteStatistics(usagesToStore);
        verify(usedEmoteManagementService, times(1)).createEmoteUsageForToday(trackedEmote, COUNT);
    }

    @Test
    public void testStoreStatisticOneServerNotInternalEmoteTrackingDisabledAutoTracking() {
        HashMap<Long, List<PersistingEmote>> usagesToStore = new HashMap<>();
        usagesToStore.put(SERVER_ID, Arrays.asList(persistingEmote));
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(trackedEmoteManagementService.loadByEmoteIdOptional(EMOTE_ID, SERVER_ID)).thenReturn(Optional.empty());
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.AUTO_TRACK_EXTERNAL)).thenReturn(true);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        testUnit.storeEmoteStatistics(usagesToStore);
        verify(usedEmoteManagementService, times(0)).createEmoteUsageForToday(any(TrackedEmote.class), anyLong());
    }

    @Test
    public void testStoreStatisticOneServerExternalEmoteTrackingEnabledAutoTrackingDisabledExternal() {
        HashMap<Long, List<PersistingEmote>> usagesToStore = new HashMap<>();
        usagesToStore.put(SERVER_ID, Arrays.asList(persistingEmote));
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(trackedEmoteManagementService.loadByEmoteIdOptional(EMOTE_ID, SERVER_ID)).thenReturn(Optional.empty());
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.AUTO_TRACK_EXTERNAL)).thenReturn(false);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        testUnit.storeEmoteStatistics(usagesToStore);
        verify(usedEmoteManagementService, times(0)).createEmoteUsageForToday(any(TrackedEmote.class), anyLong());
    }

    @Test
    public void testStoreStatisticOneServerExternalEmoteTrackingEnabledAutoTrackingEnabledExternal() {
        HashMap<Long, List<PersistingEmote>> usagesToStore = new HashMap<>();
        usagesToStore.put(SERVER_ID, Arrays.asList(persistingEmote));
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(botService.getGuildByIdOptional(SERVER_ID)).thenReturn(Optional.of(guild));
        when(persistingEmote.getCount()).thenReturn(COUNT);
        when(trackedEmoteManagementService.loadByEmoteIdOptional(EMOTE_ID, SERVER_ID)).thenReturn(Optional.empty());
        when(trackedEmoteManagementService.createExternalTrackedEmote(persistingEmote)).thenReturn(trackedEmote);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.AUTO_TRACK_EXTERNAL)).thenReturn(true);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        testUnit.storeEmoteStatistics(usagesToStore);
        verify(usedEmoteManagementService, times(1)).createEmoteUsageForToday(trackedEmote, COUNT);
    }

    @Test
    public void testStoreStatisticTwoServerInternalEmoteCreateNewTrackedEmote() {
        HashMap<Long, List<PersistingEmote>> usagesToStore = new HashMap<>();

        usagesToStore.put(SERVER_ID, Arrays.asList(persistingEmote));
        when(trackedEmote.getTrackingEnabled()).thenReturn(true);
        when(trackedEmoteManagementService.loadByEmoteIdOptional(EMOTE_ID, SERVER_ID)).thenReturn(Optional.of(trackedEmote));
        when(persistingEmote.getEmoteId()).thenReturn(EMOTE_ID);
        when(persistingEmote.getCount()).thenReturn(COUNT);
        when(usedEmoteManagementService.loadUsedEmoteForTrackedEmoteToday(trackedEmote)).thenReturn(Optional.of(usedEmote));
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.AUTO_TRACK_EXTERNAL)).thenReturn(true);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);

        Long serverId2 = SERVER_ID + 1;
        usagesToStore.put(serverId2, Arrays.asList(persistingEmote2));
        when(trackedEmoteManagementService.loadByEmoteIdOptional(EMOTE_ID_2, serverId2)).thenReturn(Optional.of(trackedEmote2));
        when(trackedEmote2.getTrackingEnabled()).thenReturn(true);
        when(persistingEmote2.getEmoteId()).thenReturn(EMOTE_ID_2);
        when(persistingEmote2.getCount()).thenReturn(COUNT);
        when(usedEmote.getAmount()).thenReturn(COUNT);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, serverId2, EmoteTrackingMode.AUTO_TRACK_EXTERNAL)).thenReturn(true);
        when(featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, serverId2, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);

        testUnit.storeEmoteStatistics(usagesToStore);

        verify(usedEmoteManagementService, times(1)).createEmoteUsageForToday(eq(trackedEmote2), anyLong());
        verify(usedEmote, times(1)).setAmount(2 * COUNT);
    }

    @Test
    public void testCreateFakeTrackedEmoteExternal() {
        executeCreateFakeTrackedEmoteTest(true);
    }

    @Test
    public void testCreateFakeTrackedEmoteInternal() {
        executeCreateFakeTrackedEmoteTest(false);
    }

    private void executeCreateFakeTrackedEmoteTest(boolean external) {
        when(emoteService.emoteIsFromGuild(emote, guild)).thenReturn(!external);
        testUnit.createTrackedEmote(emote, guild);
        verify(trackedEmoteManagementService, times(1)).createTrackedEmote(emote, guild, external);
    }

    @Test
    public void testDeleteTrackedEmote() {
        testUnit.deleteTrackedEmote(trackedEmote);
        verify(usedEmoteManagementService, times(1)).purgeEmoteUsagesSince(trackedEmote, Instant.EPOCH);
        verify(trackedEmoteManagementService, times(1)).deleteTrackedEmote(trackedEmote);
    }

    @Test
    public void testResetEmoteStats() {
        when(trackedEmoteManagementService.getTrackedEmoteForServer(guild.getIdLong(), true)).thenReturn(Arrays.asList(trackedEmote));
        testUnit.resetEmoteStats(guild);
        verify(usedEmoteManagementService, times(1)).purgeEmoteUsagesSince(trackedEmote, Instant.EPOCH);
        verify(trackedEmoteManagementService, times(1)).deleteTrackedEmote(trackedEmote);
    }

    @Test
    public void testResetEmoteStatsNoEmotes() {
        when(trackedEmoteManagementService.getTrackedEmoteForServer(guild.getIdLong(), true)).thenReturn(new ArrayList<>());
        testUnit.resetEmoteStats(guild);
        verify(usedEmoteManagementService, times(0)).purgeEmoteUsagesSince(any(TrackedEmote.class), eq(Instant.EPOCH));
        verify(trackedEmoteManagementService, times(0)).deleteTrackedEmote(any(TrackedEmote.class));
    }

    @Test
    public void testDisableEmoteTracking() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmoteManagementService.getTrackedEmoteForServer(SERVER_ID, true)).thenReturn(Arrays.asList(trackedEmote));
        testUnit.disableEmoteTracking(guild);
        verify(trackedEmote, times(1)).setTrackingEnabled(false);
    }

    @Test
    public void testLoadTrackedEmoteOverviewInternal() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmote.getDeleted()).thenReturn(false);
        when(trackedEmote.getExternal()).thenReturn(false);
        when(trackedEmote.getAnimated()).thenReturn(false);
        when(trackedEmote2.getDeleted()).thenReturn(false);
        when(trackedEmote2.getExternal()).thenReturn(false);
        when(trackedEmote2.getAnimated()).thenReturn(true);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        when(trackedEmote2.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID_2));
        when(guild.getEmoteById(EMOTE_ID)).thenReturn(emote);
        Emote emote2 = Mockito.mock(Emote.class);
        when(guild.getEmoteById(EMOTE_ID_2)).thenReturn(emote2);
        when(trackedEmoteManagementService.getTrackedEmoteForServer(SERVER_ID, true)).thenReturn(Arrays.asList(trackedEmote, trackedEmote2));
        TrackedEmoteOverview trackedEmoteOverview = testUnit.loadTrackedEmoteOverview(guild, true);
        Assert.assertEquals(emote, trackedEmoteOverview.getStaticEmotes().get(0).getEmote());
        Assert.assertEquals(trackedEmote, trackedEmoteOverview.getStaticEmotes().get(0).getTrackedEmote());
        Assert.assertEquals(emote2, trackedEmoteOverview.getAnimatedEmotes().get(0).getEmote());
        Assert.assertEquals(trackedEmote2, trackedEmoteOverview.getAnimatedEmotes().get(0).getTrackedEmote());
    }

    @Test
    public void testLoadTrackedEmoteOverviewExternal() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmote.getDeleted()).thenReturn(false);
        when(trackedEmote.getExternal()).thenReturn(true);
        when(trackedEmote.getAnimated()).thenReturn(false);
        when(trackedEmote2.getDeleted()).thenReturn(false);
        when(trackedEmote2.getExternal()).thenReturn(true);
        when(trackedEmote2.getAnimated()).thenReturn(true);
        when(trackedEmoteManagementService.getTrackedEmoteForServer(SERVER_ID, true)).thenReturn(Arrays.asList(trackedEmote, trackedEmote2));
        TrackedEmoteOverview trackedEmoteOverview = testUnit.loadTrackedEmoteOverview(guild, true);
        Assert.assertEquals(trackedEmote, trackedEmoteOverview.getExternalStaticEmotes().get(0));
        Assert.assertEquals(trackedEmote2, trackedEmoteOverview.getExternalAnimatedEmotes().get(0));
    }

    @Test
    public void testLoadTrackedEmoteOverviewDeleted() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmote.getDeleted()).thenReturn(true);
        when(trackedEmote.getAnimated()).thenReturn(false);
        when(trackedEmote2.getDeleted()).thenReturn(true);
        when(trackedEmote2.getAnimated()).thenReturn(true);
        when(trackedEmoteManagementService.getTrackedEmoteForServer(SERVER_ID, true)).thenReturn(Arrays.asList(trackedEmote, trackedEmote2));
        TrackedEmoteOverview trackedEmoteOverview = testUnit.loadTrackedEmoteOverview(guild, true);
        Assert.assertEquals(trackedEmote, trackedEmoteOverview.getDeletedStaticEmotes().get(0));
        Assert.assertEquals(trackedEmote2, trackedEmoteOverview.getDeletedAnimatedEmotes().get(0));
    }

    @Test
    public void testLoadTrackedEmoteOverviewDeletedShowTrackedFalse() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(trackedEmote.getDeleted()).thenReturn(true);
        when(trackedEmote.getAnimated()).thenReturn(false);
        when(trackedEmote2.getDeleted()).thenReturn(true);
        when(trackedEmote2.getAnimated()).thenReturn(true);
        when(trackedEmoteManagementService.getTrackedEmoteForServer(SERVER_ID, false)).thenReturn(Arrays.asList(trackedEmote, trackedEmote2));
        TrackedEmoteOverview trackedEmoteOverview = testUnit.loadTrackedEmoteOverview(guild, false);
        Assert.assertEquals(trackedEmote, trackedEmoteOverview.getDeletedStaticEmotes().get(0));
        Assert.assertEquals(trackedEmote2, trackedEmoteOverview.getDeletedAnimatedEmotes().get(0));
    }

}
