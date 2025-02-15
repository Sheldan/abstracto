package dev.sheldan.abstracto.core.commands.config.template;

import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.condition.BotOwnerOnlyCondition;
import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.CustomTemplateManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class SetTemplate extends AbstractConditionableCommand {

    private static final String TEMPLATE_KEY_PARAMETER = "templateKey";
    private static final String FILE_PARAMETER = "file";
    private static final String SET_TEMPLATE_COMMAND = "setTemplate";
    private static final String SET_TEMPLATE_RESPONSE_TEMPLATE = "setTemplate_response";

    @Autowired
    private CustomTemplateManagementService customTemplateManagementService;

    @Autowired
    private FileService fileService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private BotOwnerOnlyCondition botOwnerOnlyCondition;

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
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String templateKey = slashCommandParameterService.getCommandOption(TEMPLATE_KEY_PARAMETER, event, String.class);
        Message.Attachment templateAttachment = slashCommandParameterService.getCommandOption(FILE_PARAMETER, event, File.class, Message.Attachment.class);
        File templateFile = fileService.createTempFile(Math.random() + "");
        return templateAttachment.getProxy().downloadToFile(templateFile).thenCompose(file -> {
            try {
                return updateTemplate(event, templateFile, templateKey);
            } catch (IOException e) {
                log.error("IO Exception when loading input file.", e);
                throw new AbstractoTemplatedException("Failed to set template.", "failed_to_set_template_exception", e);
            } finally {
                try {
                    if(templateFile != null) {
                        fileService.safeDelete(templateFile);
                    }
                } catch (IOException e) {
                    log.error("Failed to delete downloaded template file.", e);
                }
            }
        });
    }

    private CompletableFuture<CommandResult> updateTemplate(SlashCommandInteractionEvent event, File templateFile, String templateKey) throws IOException {
        String templateContent = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
        customTemplateManagementService.createOrUpdateCustomTemplate(templateKey, templateContent, event.getGuild().getIdLong());
        templateService.clearCache();
        return interactionService.replyEmbed(SET_TEMPLATE_RESPONSE_TEMPLATE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter templateKeyParameter = Parameter
                .builder()
                .name(TEMPLATE_KEY_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter fileAttachmentParameter = Parameter
                .builder()
                .name(FILE_PARAMETER)
                .type(File.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(templateKeyParameter, fileAttachmentParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .rootCommandName(CoreSlashCommandNames.INTERNAL)
                .commandName(SET_TEMPLATE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SET_TEMPLATE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .supportsEmbedException(true)
                .slashCommandConfig(slashCommandConfig)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(botOwnerOnlyCondition);
        return conditions;
    }
}
