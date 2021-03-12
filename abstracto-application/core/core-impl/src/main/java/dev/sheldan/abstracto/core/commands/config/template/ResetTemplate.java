package dev.sheldan.abstracto.core.commands.config.template;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.CustomTemplateNotFoundException;
import dev.sheldan.abstracto.core.templating.model.database.CustomTemplate;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.CustomTemplateManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ResetTemplate extends AbstractConditionableCommand {

    @Autowired
    private CustomTemplateManagementService customTemplateManagementService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String templateKey = (String) commandContext.getParameters().getParameters().get(0);
        Optional<CustomTemplate> templateOptional = customTemplateManagementService.getCustomTemplate(templateKey, commandContext.getGuild().getIdLong());
        if (templateOptional.isPresent()) {
            customTemplateManagementService.deleteCustomTemplate(templateOptional.get());
            templateService.clearCache();
            return CommandResult.fromSuccess();
        }
        throw new CustomTemplateNotFoundException(templateKey, commandContext.getGuild());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        Parameter templateKeyParameter = Parameter.builder().name("templateKey").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(templateKeyParameter);
        return CommandConfiguration.builder()
                .name("resetTemplate")
                .module(ConfigModuleDefinition.CONFIG)
                .supportsEmbedException(true)
                .parameters(parameters)
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
