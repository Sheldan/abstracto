package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.UploadFileTooLargeException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.DownloadEmoteStatsModel;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emote.service.management.UsedEmoteManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * This command renders a file containing information about emote statistics and provides the file as a download.
 * If the file size is over the size limit of the {@link net.dv8tion.jda.api.entities.Guild}, this command will fail, but
 * throw an {@link UploadFileTooLargeException}.
 * This will create a temporary file on the server, which will be deleted after it has been send.
 */
@Component
@Slf4j
public class ExportEmoteStats extends AbstractConditionableCommand {

    public static final String DOWNLOAD_EMOTE_STATS_NO_STATS_AVAILABLE_RESPONSE_TEMPLATE_KEY = "downloadEmoteStats_no_stats_available_response";
    public static final String DOWNLOAD_EMOTE_STATS_RESPONSE_TEMPLATE_KEY = "downloadEmoteStats_response";
    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UsedEmoteManagementService usedEmoteManagementService;

    @Autowired
    private FileService fileService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Instant statsSince = Instant.EPOCH;
        // default is 1.1.1970
        if(!parameters.isEmpty()) {
            // if a duration is given, subtract this duration from the current point in time
            Duration duration = (Duration) parameters.get(0);
            statsSince = Instant.now().minus(duration).truncatedTo(ChronoUnit.DAYS);
        }
        AServer actualServer = serverManagementService.loadServer(commandContext.getGuild().getIdLong());
        List<UsedEmote> usedEmotes = usedEmoteManagementService.loadEmoteUsagesForServerSince(actualServer, statsSince);
        // if there are no stats available, render a message indicating so
        if(usedEmotes.isEmpty()) {
            return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(DOWNLOAD_EMOTE_STATS_NO_STATS_AVAILABLE_RESPONSE_TEMPLATE_KEY, new Object(), commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromIgnored());
        }
        // info might not be nice to handle in the template, and 1970 would look weird to users
        // effectively this means if its null its EPOCH, so it would be possible to render it still
        Instant toUseForModel = statsSince != Instant.EPOCH ? statsSince : null;
        DownloadEmoteStatsModel model = DownloadEmoteStatsModel
                .builder()
                .emotes(usedEmotes)
                .guild(commandContext.getGuild())
                .downloadDate(Instant.now())
                .serverId(commandContext.getGuild().getIdLong())
                .requester(commandContext.getAuthor())
                .statsSince(toUseForModel)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(DOWNLOAD_EMOTE_STATS_RESPONSE_TEMPLATE_KEY, model, actualServer.getId());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter periodParameter = Parameter
                .builder()
                .name("period")
                .templated(true)
                .optional(true)
                .type(Duration.class)
                .build();
        parameters.add(periodParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name("exportEmoteStats")
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .async(true)
                .messageCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return StatisticFeatureDefinition.EMOTE_TRACKING;
    }
}
