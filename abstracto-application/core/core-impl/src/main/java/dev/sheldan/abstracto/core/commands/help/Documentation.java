package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Component
public class Documentation implements Command {

    @Autowired
    private ChannelService channelService;

    private static final String DOCUMENTATION_RESPONSE_TEMPLATE_KEY = "documentation_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannelList(DOCUMENTATION_RESPONSE_TEMPLATE_KEY, new Object(), commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("documentation")
                .async(true)
                .aliases(Arrays.asList("docu", "docs"))
                .module(SupportModuleDefinition.SUPPORT)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
