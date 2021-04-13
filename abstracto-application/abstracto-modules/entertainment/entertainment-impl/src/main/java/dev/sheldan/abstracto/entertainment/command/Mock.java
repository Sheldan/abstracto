package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.command.handler.parameter.CombinedParameter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.model.command.MockResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.core.command.config.Parameter.ADDITIONAL_TYPES_KEY;

@Component
public class Mock extends AbstractConditionableCommand {

    public static final String MOCK_RESPONSE_TEMPLATE_KEY = "mock_response";
    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private ChannelService channelService;

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
        MockResponseModel model = (MockResponseModel) ContextConverter.slimFromCommandContext(commandContext, MockResponseModel.class);
        model.setOriginalText(messageText);
        model.setMockingText(mockingText);
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(MOCK_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Map<String, Object> parameterAlternatives = new HashMap<>();
        parameterAlternatives.put(ADDITIONAL_TYPES_KEY, Arrays.asList(Message.class, String.class));
        parameters.add(Parameter.builder().name("message").type(CombinedParameter.class).remainder(true)
                .additionalInfo(parameterAlternatives).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("mock")
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .async(true)
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
