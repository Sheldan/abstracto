package dev.sheldan.abstracto.core.commands.config.template;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.CustomTemplateManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SetTemplate extends AbstractConditionableCommand {

    @Autowired
    private CustomTemplateManagementService customTemplateManagementService;

    @Autowired
    private FileService fileService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameter = commandContext.getParameters().getParameters();
        String templateKey = (String) parameter.get(0);
        File templateFile = (File) parameter.get(1);
        try {
            String templateContent = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
            customTemplateManagementService.createOrUpdateCustomTemplate(templateKey, templateContent, commandContext.getGuild().getIdLong());
            templateService.clearCache();
            return CommandResult.fromSuccess();
        } catch (IOException e) {
            log.error("IO Exception when loading input file.", e);
            throw new AbstractoTemplatedException("Failed to set template.", "failed_to_set_template_exception", e);
        } finally {
            try {
                fileService.safeDelete(templateFile);
            } catch (IOException e) {
                log.error("Failed to delete downloaded template file.", e);
            }
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter templateKeyParameter = Parameter.builder().name("templateKey").type(String.class).templated(true).build();
        Parameter fileAttachmentParameter = Parameter.builder().name("file").type(File.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(templateKeyParameter, fileAttachmentParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("setTemplate")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .supportsEmbedException(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }
}
