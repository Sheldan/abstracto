package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.config.UtilitySlashCommandNames;
import dev.sheldan.abstracto.utility.model.ShowEmoteLog;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
        Emote emoteParameter = (Emote) parameters.get(0);
        ShowEmoteLog emoteLog = ShowEmoteLog
                .builder()
                .emote(emoteParameter)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(SHOW_EMOTE_RESPONSE_TEMPLATE, emoteLog, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String emoteText = slashCommandParameterService.getCommandOption(EMOTE_PARAMETER, event, Emote.class, String.class);
        Emoji emoji = Emoji.fromMarkdown(emoteText);
        Emote emote = event.getJDA().getEmoteById(emoji.getId());
        ShowEmoteLog emoteLog = ShowEmoteLog
                .builder()
                .emote(emote)
                .build();
        return interactionService.replyEmbed(SHOW_EMOTE_RESPONSE_TEMPLATE, emoteLog, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter emoteParameter = Parameter
                .builder()
                .name(EMOTE_PARAMETER)
                .type(Emote.class)
                .templated(true)
                .build();
        parameters.add(emoteParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(UtilitySlashCommandNames.UTILITY)
                .commandName(SHOW_EMOTE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SHOW_EMOTE_COMMAND)
                .slashCommandConfig(slashCommandConfig)
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
