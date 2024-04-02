package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Component
public class Documentation extends AbstractConditionableCommand {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    private static final String DOCUMENTATION_RESPONSE_TEMPLATE_KEY = "documentation_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(DOCUMENTATION_RESPONSE_TEMPLATE_KEY, new Object(), commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        return interactionService.replyEmbed(DOCUMENTATION_RESPONSE_TEMPLATE_KEY, new Object(), event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
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
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(CoreSlashCommandNames.INFO)
                .commandName("documentation")
                .build();

        return CommandConfiguration.builder()
                .name("documentation")
                .async(true)
                .aliases(Arrays.asList("docu", "docs"))
                .module(SupportModuleDefinition.SUPPORT)
                .help(helpInfo)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
