package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.model.DownloadEmoteStatsModel;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emote.service.management.UsedEmoteManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.statistic.emote.command.ExportEmoteStats.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExportEmoteStatsTest {

    @InjectMocks
    private ExportEmoteStats testUnit;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ChannelService channelService;

    @Mock
    private UsedEmoteManagementService usedEmoteManagementService;

    @Mock
    private FileService fileService;

    @Captor
    private ArgumentCaptor<DownloadEmoteStatsModel> modelArgumentCaptor;

    @Mock
    private UsedEmote usedEmote;

    private static final Long SERVER_ID = 4L;
    private static final String FILE_NAME = "name";
    private static final String FILE_CONTENT = "content";

    @Test
    public void testExportNoStatsAvailable() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        List<UsedEmote> usedEmotes = new ArrayList<>();
        when(usedEmoteManagementService.loadEmoteUsagesForServerSince(server, Instant.EPOCH)).thenReturn(usedEmotes);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);
        verify(channelService, times(1)).sendEmbedTemplateInTextChannelList(eq(DOWNLOAD_EMOTE_STATS_NO_STATS_AVAILABLE_RESPONSE_TEMPLATE_KEY), any(), eq(commandContext.getChannel()));
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatureDefinition.EMOTE_TRACKING, testUnit.getFeature());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
