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
import dev.sheldan.abstracto.core.exception.TemplateNotFoundException;
import dev.sheldan.abstracto.core.models.template.commands.GetTemplateModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.database.Template;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.TemplateManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class GetTemplate extends AbstractConditionableCommand {

    @Autowired
    private TemplateManagementService templateManagementService;

    @Autowired
    private FileService fileService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    private static final String GET_TEMPLATE_FILE_NAME_TEMPLATE_KEY = "getTemplate_file_name";
    private static final String GET_TEMPLATE_RESPONSE_TEMPLATE_KEY = "getTemplate_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String templateKey = (String) commandContext.getParameters().getParameters().get(0);
        Optional<Template> templateOptional = templateManagementService.getTemplateByKey(templateKey);
        if(templateOptional.isPresent()) {
            Template template = templateOptional.get();
            GetTemplateModel model = (GetTemplateModel) ContextConverter.slimFromCommandContext(commandContext, GetTemplateModel.class);
            model.setCreated(template.getCreated());
            model.setLastModified(template.getLastModified());
            model.setTemplateContent(template.getContent());
            model.setTemplateKey(templateKey);
            return FutureUtils.toSingleFutureGeneric(channelService.sendFileToChannel(template.getContent(),
                    GET_TEMPLATE_FILE_NAME_TEMPLATE_KEY, GET_TEMPLATE_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromSuccess());
        }
        throw new TemplateNotFoundException(templateKey);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        Parameter templateKeyParameter = Parameter.builder().name("templateKey").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(templateKeyParameter);
        return CommandConfiguration.builder()
                .name("getTemplate")
                .module(ConfigModuleInterface.CONFIG)
                .supportsEmbedException(true)
                .async(true)
                .parameters(parameters)
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
