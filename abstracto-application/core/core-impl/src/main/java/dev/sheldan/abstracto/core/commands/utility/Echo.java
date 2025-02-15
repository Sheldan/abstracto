package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.template.commands.EchoModel;
import dev.sheldan.abstracto.core.models.template.commands.EchoRedirectResponseModel;
import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class Echo extends AbstractConditionableCommand {

    private static final String TEMPLATE_NAME = "echo_response";
    private static final String REDIRECT_TEMPLATE_NAME = "echo_redirect_response";
    public static final String ECHO_COMMAND = "echo";
    public static final String INPUT_PARAMETER = "input";
    public static final String TARGET_CHANNEL_PARAMETER = "targetChannel";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        StringBuilder sb = new StringBuilder();
        commandContext.getParameters().getParameters().forEach(o ->
                sb.append(o.toString())
        );
        EchoModel model = EchoModel
                .builder()
                .text(sb.toString())
                .build();
        String textToSend = templateService.renderTemplate(TEMPLATE_NAME, model, commandContext.getGuild().getIdLong());
        channelService.sendTextToChannel(textToSend, commandContext.getChannel());
        return CommandResult.fromIgnored();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String message = slashCommandParameterService.getCommandOption(INPUT_PARAMETER, event, String.class);
        MessageChannel messageChannel;
        boolean redirect = false;
        if (slashCommandParameterService.hasCommandOption(TARGET_CHANNEL_PARAMETER, event)) {
            messageChannel = slashCommandParameterService.getCommandOption(TARGET_CHANNEL_PARAMETER, event, GuildMessageChannel.class);
            redirect = true;
        } else {
            messageChannel = event.getChannel();
        }

        EchoModel model = EchoModel
                .builder()
                .text(message)
                .build();

        if (!redirect) {
            return interactionService.replyMessage(TEMPLATE_NAME, model, event)
                    .thenApply(unused -> CommandResult.fromSuccess());
        } else {
            EchoRedirectResponseModel redirectResponseModel = EchoRedirectResponseModel
                    .builder()
                    .channel(ChannelDisplay.fromChannel(messageChannel))
                    .build();
            return interactionService.replyEmbed(REDIRECT_TEMPLATE_NAME, redirectResponseModel, event)
                    .thenCompose(interactionHook -> channelService.sendTextTemplateInMessageChannel(TEMPLATE_NAME, model, messageChannel))
                    .thenApply(createdMessage -> CommandResult.fromSuccess());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter
                .builder()
                .name(INPUT_PARAMETER)
                .type(String.class)
                .templated(true)
                .remainder(true)
                .build());

        parameters.add(Parameter
                .builder()
                .name(TARGET_CHANNEL_PARAMETER)
                .type(GuildMessageChannel.class)
                .slashCommandOnly(true)
                .supportsUserCommands(false)
                .optional(true)
                .templated(true)
                .remainder(true)
                .build());

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .rootCommandName(ECHO_COMMAND)
                .build();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name(ECHO_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .slashCommandConfig(slashCommandConfig)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
