package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.handler.parameter.CombinedParameter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.MockResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.core.command.config.Parameter.ADDITIONAL_TYPES_KEY;

@Component
public class Mock extends AbstractConditionableCommand {

    public static final String MOCK_RESPONSE_TEMPLATE_KEY = "mock_response";
    public static final String MOCK_COMMAND = "mock";
    public static final String MESSAGE_PARAMETER = "message";
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
        Object givenParameter = commandContext.getParameters().getParameters().get(0);
        String messageText;
        Member mockedMember = null;
        if(givenParameter instanceof Message) {
            Message originalMessage = (Message) givenParameter;
            messageText = originalMessage.getContentRaw();
            mockedMember = originalMessage.getMember();
        } else {
            messageText = givenParameter.toString();
        }
        String mockingText = entertainmentService.createMockText(messageText, commandContext.getAuthor(), mockedMember);
        MockResponseModel model = MockResponseModel
                .builder()
                .originalText(messageText)
                .mockingText(mockingText)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(MOCK_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String text = slashCommandParameterService.getCommandOption(MESSAGE_PARAMETER, event, String.class);
        String mockingText = entertainmentService.createMockText(text, event.getMember(), null);
        MockResponseModel model = MockResponseModel
                .builder()
                .originalText(text)
                .mockingText(mockingText)
                .build();
        return interactionService.replyEmbed(MOCK_RESPONSE_TEMPLATE_KEY, model, event.getInteraction())
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Map<String, Object> parameterAlternatives = new HashMap<>();
        parameterAlternatives.put(ADDITIONAL_TYPES_KEY, Arrays.asList(CombinedParameterEntry.messageParameter(Message.class), CombinedParameterEntry.parameter(String.class)));

        Parameter messageParameter = Parameter
                .builder()
                .name(MESSAGE_PARAMETER)
                .type(CombinedParameter.class)
                .remainder(true)
                .additionalInfo(parameterAlternatives)
                .templated(true)
                .build();

        parameters.add(messageParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(EntertainmentSlashCommandNames.ENTERTAINMENT)
                .commandName(MOCK_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(MOCK_COMMAND)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
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
