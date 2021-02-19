package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.UploadFileTooLargeException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.model.DownloadEmoteStatsModel;
import dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.management.UsedEmoteManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.statistic.emotes.command.ExportEmoteStats.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    public void testExportAllEmoteStats() throws IOException {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getMaxFileSize()).thenReturn(4L);
        mockServerAndFileRendering(commandContext);
        File file = Mockito.mock(File.class);
        when(fileService.createTempFile(FILE_NAME)).thenReturn(file);
        when(file.length()).thenReturn(3L);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(DOWNLOAD_EMOTE_STATS_RESPONSE_TEMPLATE_KEY), any())).thenReturn(messageToSend);
        when(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);
        verify(fileService, times(1)).writeContentToFile(file, FILE_CONTENT);
        verify(messageToSend, times(1)).setFileToSend(file);
        verify(fileService, times(1)).safeDelete(file);
        verifyModel();
    }

    @Test
    public void testExportAllEmoteStatsSince() throws IOException {
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(Duration.ofHours(3)));
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandContext.getGuild().getMaxFileSize()).thenReturn(4L);
        List<UsedEmote> usedEmotes = Arrays.asList(usedEmote);
        when(usedEmoteManagementService.loadEmoteUsagesForServerSince(eq(server), any(Instant.class))).thenReturn(usedEmotes);
        when(templateService.renderTemplate(eq(DOWNLOAD_EMOTE_STATS_FILE_NAME_TEMPLATE_KEY), modelArgumentCaptor.capture())).thenReturn(FILE_NAME);
        when(templateService.renderTemplate(eq(DOWNLOAD_EMOTE_STATS_FILE_CONTENT_TEMPLATE_KEY), any())).thenReturn(FILE_CONTENT);
        File file = Mockito.mock(File.class);
        when(fileService.createTempFile(FILE_NAME)).thenReturn(file);
        when(file.length()).thenReturn(3L);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(DOWNLOAD_EMOTE_STATS_RESPONSE_TEMPLATE_KEY), any())).thenReturn(messageToSend);
        when(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);
        verify(fileService, times(1)).writeContentToFile(file, FILE_CONTENT);
        verify(messageToSend, times(1)).setFileToSend(file);
        verify(fileService, times(1)).safeDelete(file);
        verifyModel();
    }

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

    @Test(expected = AbstractoRunTimeException.class)
    public void testFileIOException() throws IOException {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        mockServerAndFileRendering(commandContext);
        File file = Mockito.mock(File.class);
        when(fileService.createTempFile(FILE_NAME)).thenReturn(file);
        doThrow(new IOException()).when(fileService).writeContentToFile(file, FILE_CONTENT);
        testUnit.executeAsync(commandContext);
    }

    @Test(expected = UploadFileTooLargeException.class)
    public void testExportAllEmoteStatsTooBig() throws IOException {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getMaxFileSize()).thenReturn(2L);
        mockServerAndFileRendering(commandContext);
        File file = Mockito.mock(File.class);
        when(fileService.createTempFile(FILE_NAME)).thenReturn(file);
        when(file.length()).thenReturn(3L);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);
        verify(fileService, times(1)).writeContentToFile(file, FILE_CONTENT);
        verify(messageToSend, times(1)).setFileToSend(file);
        verify(fileService, times(1)).safeDelete(file);
        verifyModel();
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }

    private void verifyModel() {
        DownloadEmoteStatsModel model = modelArgumentCaptor.getValue();
        Assert.assertEquals(1, model.getEmotes().size());
        Assert.assertEquals(usedEmote, model.getEmotes().get(0));
    }

    private void mockServerAndFileRendering(CommandContext commandContext) {
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        List<UsedEmote> usedEmotes = Arrays.asList(usedEmote);
        when(usedEmoteManagementService.loadEmoteUsagesForServerSince(server, Instant.EPOCH)).thenReturn(usedEmotes);
        when(templateService.renderTemplate(eq(DOWNLOAD_EMOTE_STATS_FILE_NAME_TEMPLATE_KEY), modelArgumentCaptor.capture())).thenReturn(FILE_NAME);
        when(templateService.renderTemplate(eq(DOWNLOAD_EMOTE_STATS_FILE_CONTENT_TEMPLATE_KEY), any())).thenReturn(FILE_CONTENT);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
