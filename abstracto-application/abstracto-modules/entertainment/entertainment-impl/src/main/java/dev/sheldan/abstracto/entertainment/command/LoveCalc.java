package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.LoveCalcResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class LoveCalc extends AbstractConditionableCommand {

    public static final String LOVE_CALC_RESPONSE_TEMPLATE_KEY = "loveCalc_response";
    public static final String FIRST_SUBJECT_PARAMETER = "firstSubject";
    public static final String SECOND_SUBJECT_PARAMETER = "secondSubject";
    public static final String LOVE_CALC_COMMAND = "loveCalc";

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String firstPart = (String) parameters.get(0);
        String secondPart = (String) parameters.get(1);
        MessageToSend messageToSend = getMessageToSend(commandContext.getGuild().getIdLong(), firstPart, secondPart);
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String firstPart = slashCommandParameterService.getCommandOption(FIRST_SUBJECT_PARAMETER, event, String.class);
        String secondPart = slashCommandParameterService.getCommandOption(SECOND_SUBJECT_PARAMETER, event, String.class);
        MessageToSend messageToSend = getMessageToSend(ContextUtils.serverIdOrNull(event), firstPart, secondPart);
        return interactionService.replyMessageToSend(messageToSend, event.getInteraction())
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getMessageToSend(Long serverId, String firstPart, String secondPart) {
        Integer rolled = entertainmentService.getLoveCalcValue(firstPart, secondPart);
        LoveCalcResponseModel model = LoveCalcResponseModel
                .builder()
                .rolled(rolled)
                .firstPart(firstPart)
                .secondPart(secondPart)
                .build();
        return templateService.renderEmbedTemplate(LOVE_CALC_RESPONSE_TEMPLATE_KEY, model, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter firstSubjectParameter = Parameter
                .builder()
                .name(FIRST_SUBJECT_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        parameters.add(firstSubjectParameter);
        Parameter secondSubjectParameter = Parameter
                .builder()
                .name(SECOND_SUBJECT_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        parameters.add(secondSubjectParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(EntertainmentSlashCommandNames.UTILITY)
                .commandName(LOVE_CALC_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(LOVE_CALC_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ENTERTAINMENT;
    }
}
