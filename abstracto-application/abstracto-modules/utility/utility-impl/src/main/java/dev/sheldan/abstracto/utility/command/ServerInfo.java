package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.template.display.EmoteDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.model.ServerInfoModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ServerInfo extends AbstractConditionableCommand {

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        log.info("Displaying serverinfo for server {}", commandContext.getGuild().getId());
        ServerInfoModel model = buildModel(commandContext);
        return FutureUtils.toSingleFutureGeneric(
                channelService.sendEmbedTemplateInTextChannelList("serverinfo_response", model, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    private ServerInfoModel buildModel(CommandContext commandContext) {
        ServerInfoModel model = (ServerInfoModel) ContextConverter.fromCommandContext(commandContext, ServerInfoModel.class);
        model.setGuild(commandContext.getGuild());
        List<EmoteDisplay> staticEmotes = new ArrayList<>();
        List<EmoteDisplay> animatedEmotes = new ArrayList<>();
        commandContext.getGuild().getEmotes().forEach(emote -> {
            EmoteDisplay emoteDisplay = EmoteDisplay.fromEmote(emote);
            if(emote.isAnimated()) {
                animatedEmotes.add(emoteDisplay);
            } else {
                staticEmotes.add(emoteDisplay);
            }
        });
        model.setAnimatedEmotes(animatedEmotes);
        model.setStaticEmotes(staticEmotes);
        return model;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("serverInfo")
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return UtilityFeatureDefinition.UTILITY;
    }
}
