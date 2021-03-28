package dev.sheldan.abstracto.webservices.urban.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import dev.sheldan.abstracto.webservices.urban.model.UrbanDefinition;
import dev.sheldan.abstracto.webservices.urban.model.UrbanResponseModel;
import dev.sheldan.abstracto.webservices.urban.service.UrbanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class UrbanDefine extends AbstractConditionableCommand {

    @Autowired
    private UrbanService urbanService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    private static final String URBAN_DEFINE_RESPONSE_MODEL_TEMPLATE_KEY = "urban_define_response_model";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String parameter = (String) commandContext.getParameters().getParameters().get(0);
        try {
            UrbanDefinition definition = urbanService.getUrbanDefinition(parameter);
            UrbanResponseModel model = (UrbanResponseModel) ContextConverter.slimFromCommandContext(commandContext, UrbanResponseModel.class);
            model.setDefinition(definition);
            MessageToSend message = templateService.renderEmbedTemplate(URBAN_DEFINE_RESPONSE_MODEL_TEMPLATE_KEY, model);
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(message, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromSuccess());
        } catch (IOException e) {
            throw new AbstractoRunTimeException(e);
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("searchQuery").type(String.class).remainder(true).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<String> aliases = Arrays.asList("ud");
        return CommandConfiguration.builder()
                .name("urbanDefine")
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .aliases(aliases)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.URBAN_DICTIONARY;
    }
}
