package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.model.ShowEmoteLog;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowEmote extends AbstractConditionableCommand {

    public static final String SHOW_EMOTE_RESPONSE_TEMPLATE = "showEmote_response";
    public static final String SHOW_EMOTE_COMMAND = "showEmote";
    public static final String EMOTE_PARAMETER = "emote";

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        CustomEmoji emoteParameter = (CustomEmoji) parameters.get(0);
        ShowEmoteLog emoteLog = ShowEmoteLog
                .builder()
                .emote(emoteParameter)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(SHOW_EMOTE_RESPONSE_TEMPLATE, emoteLog, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter emoteParameter = Parameter
                .builder()
                .name(EMOTE_PARAMETER)
                .type(CustomEmoji.class)
                .templated(true)
                .build();
        parameters.add(emoteParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        return CommandConfiguration.builder()
                .name(SHOW_EMOTE_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .messageCommandOnly(true)
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
