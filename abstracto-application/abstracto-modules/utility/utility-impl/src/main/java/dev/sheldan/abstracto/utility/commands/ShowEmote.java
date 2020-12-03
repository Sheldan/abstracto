package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.ShowEmoteLog;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowEmote extends AbstractConditionableCommand {

    public static final String SHOW_EMOTE_RESPONSE_TEMPLATE = "showEmote_response";

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Emote emoteParameter = (Emote) parameters.get(0);
        ShowEmoteLog emoteLog = (ShowEmoteLog) ContextConverter.fromCommandContext(commandContext, ShowEmoteLog.class);
        emoteLog.setEmote(emoteParameter);
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(SHOW_EMOTE_RESPONSE_TEMPLATE, emoteLog, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("emote").type(Emote.class).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("showEmote")
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.UTILITY;
    }
}
