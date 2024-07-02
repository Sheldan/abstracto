package dev.sheldan.abstracto.webservices.youtube.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.webservices.common.service.SuggestQueriesService;
import dev.sheldan.abstracto.webservices.config.WebServicesSlashCommandNames;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import dev.sheldan.abstracto.webservices.youtube.config.YoutubeWebServiceFeatureMode;
import dev.sheldan.abstracto.webservices.youtube.model.YoutubeVideo;
import dev.sheldan.abstracto.webservices.youtube.model.command.YoutubeVideoSearchCommandModel;
import dev.sheldan.abstracto.webservices.youtube.service.YoutubeSearchService;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class YoutubeVideoSearch extends AbstractConditionableCommand {

    public static final String YOUTUBE_SEARCH_COMMAND_RESPONSE_TEMPLATE_KEY = "youtube_search_command_response";
    public static final String YOUTUBE_SEARCH_COMMAND_RESPONSE_LINK_TEMPLATE_KEY = "youtube_search_command_response_link";
    private static final String SEARCH_QUERY_PARAMETER = "searchQuery";
    private static final String YOUTUBE_SEARCH_COMMAND = "youtubeSearch";

    @Autowired
    private YoutubeSearchService youtubeSearchService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private SuggestQueriesService suggestQueriesService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String query = (String) commandContext.getParameters().getParameters().get(0);
        YoutubeVideo foundVideo = youtubeSearchService.searchOneVideoForQuery(query);
        YoutubeVideoSearchCommandModel model = YoutubeVideoSearchCommandModel
                .builder()
                .video(foundVideo)
                .build();
        CompletableFuture<Void> infoEmbedFuture;
        if(featureModeService.featureModeActive(WebserviceFeatureDefinition.YOUTUBE, commandContext.getGuild().getIdLong(), YoutubeWebServiceFeatureMode.VIDEO_DETAILS)) {
            MessageToSend message = templateService.renderEmbedTemplate(YOUTUBE_SEARCH_COMMAND_RESPONSE_TEMPLATE_KEY, model, commandContext.getGuild().getIdLong());
            infoEmbedFuture = FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(message, commandContext.getChannel()));
        } else {
            infoEmbedFuture = CompletableFuture.completedFuture(null);
        }
        MessageToSend linkEmbed = templateService.renderEmbedTemplate(YOUTUBE_SEARCH_COMMAND_RESPONSE_LINK_TEMPLATE_KEY, model, commandContext.getGuild().getIdLong());
        CompletableFuture<Void> linkEmbedFuture = FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(linkEmbed, commandContext.getChannel()));
        return CompletableFuture.allOf(infoEmbedFuture, linkEmbedFuture)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String query = slashCommandParameterService.getCommandOption(SEARCH_QUERY_PARAMETER, event, String.class);
        YoutubeVideo foundVideo = youtubeSearchService.searchOneVideoForQuery(query);
        YoutubeVideoSearchCommandModel model = YoutubeVideoSearchCommandModel
                .builder()
                .video(foundVideo)
                .build();
        boolean sendInfo;
        MessageToSend linkEmbed = templateService.renderEmbedTemplate(YOUTUBE_SEARCH_COMMAND_RESPONSE_LINK_TEMPLATE_KEY, model, ContextUtils.serverIdOrNull(event));
        MessageToSend infoEmbed;
        if(ContextUtils.isNotUserCommand(event)) {
            sendInfo = featureModeService.featureModeActive(WebserviceFeatureDefinition.YOUTUBE, event.getGuild().getIdLong(), YoutubeWebServiceFeatureMode.VIDEO_DETAILS);
            infoEmbed = templateService.renderEmbedTemplate(YOUTUBE_SEARCH_COMMAND_RESPONSE_TEMPLATE_KEY, model, ContextUtils.serverIdOrNull(event));
        } else {
            infoEmbed = null;
            sendInfo = false;
        }
        return interactionService.replyMessageToSend(linkEmbed, event)
                .thenCompose(interactionHook -> {
                    if(sendInfo) {
                        return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(infoEmbed, interactionHook));
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .thenApply(o -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), SEARCH_QUERY_PARAMETER)) {
            return suggestQueriesService.getYoutubeSuggestionsForQuery(event.getFocusedOption().getValue());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter queryParameter = Parameter
                .builder()
                .name(SEARCH_QUERY_PARAMETER)
                .type(String.class)
                .remainder(true)
                .supportsAutoComplete(true)
                .templated(true)
                .build();
        parameters.add(queryParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        List<String> aliases = Arrays.asList("yt");

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(WebServicesSlashCommandNames.YOUTUBE)
                .commandName("search")
                .build();

        return CommandConfiguration.builder()
                .name(YOUTUBE_SEARCH_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .async(true)
                .aliases(aliases)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.YOUTUBE;
    }
}
