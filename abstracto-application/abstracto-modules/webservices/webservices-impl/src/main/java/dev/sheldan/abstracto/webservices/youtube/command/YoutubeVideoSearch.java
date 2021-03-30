package dev.sheldan.abstracto.webservices.youtube.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import dev.sheldan.abstracto.webservices.youtube.config.YoutubeWebServiceFeatureMode;
import dev.sheldan.abstracto.webservices.youtube.model.YoutubeVideo;
import dev.sheldan.abstracto.webservices.youtube.model.command.YoutubeVideoSearchCommandModel;
import dev.sheldan.abstracto.webservices.youtube.service.YoutubeSearchService;
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
    @Autowired
    private YoutubeSearchService youtubeSearchService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private FeatureModeService featureModeService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String query = (String) commandContext.getParameters().getParameters().get(0);
        YoutubeVideo foundVideo = youtubeSearchService.searchOneVideoForQuery(query);
        YoutubeVideoSearchCommandModel model = (YoutubeVideoSearchCommandModel) ContextConverter.slimFromCommandContext(commandContext, YoutubeVideoSearchCommandModel.class);
        model.setVideo(foundVideo);
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
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("searchQuery").type(String.class).remainder(true).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<String> aliases = Arrays.asList("yt");
        return CommandConfiguration.builder()
                .name("youtubeSearch")
                .module(UtilityModuleDefinition.UTILITY)
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
