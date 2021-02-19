package dev.sheldan.abstracto.core.commands.config.template;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.exception.CustomTemplateNotFoundException;
import dev.sheldan.abstracto.core.models.template.commands.GetCustomTemplateModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.database.CustomTemplate;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.CustomTemplateManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class GetCustomTemplate extends AbstractConditionableCommand {

    @Autowired
    private CustomTemplateManagementService customTemplateManagementService;

    @Autowired
    private FileService fileService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    private static final String GET_CUSTOM_TEMPLATE_FILE_NAME_TEMPLATE_KEY = "getCustomTemplate_file_name";
    private static final String GET_CUSTOM_TEMPLATE_RESPONSE_TEMPLATE_KEY = "getCustomTemplate_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String templateKey = (String) commandContext.getParameters().getParameters().get(0);
        Optional<CustomTemplate> templateOptional = customTemplateManagementService.getCustomTemplate(templateKey, commandContext.getGuild().getIdLong());
        if(templateOptional.isPresent()) {
            CustomTemplate template = templateOptional.get();
            GetCustomTemplateModel model = (GetCustomTemplateModel) ContextConverter.slimFromCommandContext(commandContext, GetCustomTemplateModel.class);
            model.setCreated(template.getCreated());
            model.setLastModified(template.getLastModified());
            model.setTemplateContent(template.getContent());
            model.setTemplateKey(templateKey);
            return FutureUtils.toSingleFutureGeneric(channelService.sendFileToChannel(template.getContent(),
                    GET_CUSTOM_TEMPLATE_FILE_NAME_TEMPLATE_KEY, GET_CUSTOM_TEMPLATE_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromSuccess());
        }
        throw new CustomTemplateNotFoundException(templateKey, commandContext.getGuild());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter templateKeyParameter = Parameter.builder().name("templateKey").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(templateKeyParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("getCustomTemplate")
                .module(ConfigModuleInterface.CONFIG)
                .supportsEmbedException(true)
                .parameters(parameters)
                .async(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
