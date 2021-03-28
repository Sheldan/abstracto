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
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
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

    @Autowired
    private YoutubeSearchService youtubeSearchService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String query = (String) commandContext.getParameters().getParameters().get(0);
        YoutubeVideo foundVideo = youtubeSearchService.searchOneVideoForQuery(query);
        YoutubeVideoSearchCommandModel model = (YoutubeVideoSearchCommandModel) ContextConverter.slimFromCommandContext(commandContext, YoutubeVideoSearchCommandModel.class);
        model.setVideo(foundVideo);
        MessageToSend message = templateService.renderEmbedTemplate("youtube_search_command_response", model);
        MessageToSend linkEmbed = templateService.renderEmbedTemplate("youtube_search_command_response_link", model);
        CompletableFuture<Void> infoEmbedFuture = FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(message, commandContext.getChannel()));
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
