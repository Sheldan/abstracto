package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.ChooseResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Choose extends AbstractConditionableCommand {

    public static final String CHOOSE_RESPONSE_TEMPLATE_KEY = "choose_response";
    private static final String CHOOSE_COMMAND = "choose";
    private static final String TEXT_PARAMETER = "text";
    private static final int CHOICES_SIZE = 5;

    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<String> choices = (List) commandContext.getParameters().getParameters().get(0);
        String choice = entertainmentService.takeChoice(choices, commandContext.getAuthor());
        ChooseResponseModel responseModel = ChooseResponseModel
                .builder()
                .chosenValue(choice)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(CHOOSE_RESPONSE_TEMPLATE_KEY, responseModel, commandContext.getChannel()))
            .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        List<String> choices = new ArrayList<>();
        for (int i = 0; i < CHOICES_SIZE; i++) {
            if(slashCommandParameterService.hasCommandOption(TEXT_PARAMETER + "_" + i, event)) {
                String choice = slashCommandParameterService.getCommandOption(TEXT_PARAMETER + "_" + i, event, String.class);
                choices.add(choice);
            }
        }
        String choice = entertainmentService.takeChoice(choices, event.getMember());
        ChooseResponseModel responseModel = ChooseResponseModel
                .builder()
                .chosenValue(choice)
                .build();
        return interactionService.replyEmbed(CHOOSE_RESPONSE_TEMPLATE_KEY, responseModel, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter textParameter = Parameter
                .builder()
                .name(TEXT_PARAMETER)
                .type(String.class)
                .templated(true)
                .remainder(true)
                .listSize(CHOICES_SIZE)
                .isListParam(true)
                .build();
        parameters.add(textParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(EntertainmentSlashCommandNames.UTILITY)
                .commandName(CHOOSE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(CHOOSE_COMMAND)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ENTERTAINMENT;
    }
}
