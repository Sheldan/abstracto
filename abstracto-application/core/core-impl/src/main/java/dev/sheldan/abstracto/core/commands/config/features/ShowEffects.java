package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.EffectType;
import dev.sheldan.abstracto.core.models.template.commands.ShowEffectsModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.EffectTypeManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ShowEffects extends AbstractConditionableCommand {

    private static final String SHOW_EFFECTS_COMMAND = "showEffects";
    private static final String SHOW_EFFECTS_RESPONSE_TEMPLATE = "showEffects_response";

    @Autowired
    private EffectTypeManagementService effectTypeManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        ShowEffectsModel model = getModel();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(SHOW_EFFECTS_RESPONSE_TEMPLATE,
                model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        ShowEffectsModel model = getModel();
        return interactionService.replyEmbed(SHOW_EFFECTS_RESPONSE_TEMPLATE, model, event)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    private ShowEffectsModel getModel() {
        List<EffectType> allEffects = effectTypeManagementService.getAllEffects();
        List<String> effectKeys = allEffects.stream().map(EffectType::getEffectTypeKey).collect(Collectors.toList());
        return ShowEffectsModel
                .builder()
                .effects(effectKeys)
                .build();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.CONFIG)
                .commandName(SHOW_EFFECTS_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SHOW_EFFECTS_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
