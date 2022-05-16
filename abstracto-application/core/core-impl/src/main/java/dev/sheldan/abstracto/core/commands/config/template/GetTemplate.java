package dev.sheldan.abstracto.core.commands.config.template;

import dev.sheldan.abstracto.core.command.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.TemplateNotFoundException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.template.commands.GetTemplateModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.TemplateManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class GetTemplate extends AbstractConditionableCommand {

    private static final String GET_TEMPLATE_COMMAND = "getTemplate";
    private static final String TEMPLATE_KEY_PARAMETER = "templateKey";
    private static final String GET_TEMPLATE_RESPONSE_TEMPLATE_KEY = "getTemplate_response";

    @Autowired
    private TemplateManagementService templateManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FileService fileService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String templateKey = (String) commandContext.getParameters().getParameters().get(0);
        GetTemplateModel model = getModel(templateKey);
        File tempFile = fileService.createTempFile(templateKey + ".ftl");
        try {
            fileService.writeContentToFile(tempFile, model.getTemplateContent());
            MessageToSend messageToSend = templateService.renderEmbedTemplate(GET_TEMPLATE_RESPONSE_TEMPLATE_KEY, model, commandContext.getGuild().getIdLong());
            messageToSend.getAttachedFiles().get(0).setFile(tempFile);
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromSuccess());
        } catch (IOException e) {
            throw new AbstractoRunTimeException(e);
        } finally {
            try {
                fileService.safeDelete(tempFile);
            } catch (IOException e) {
                log.error("Failed to delete temporary get template file {}.", tempFile.getAbsoluteFile(), e);
            }
        }
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String templateKey = slashCommandParameterService.getCommandOption(TEMPLATE_KEY_PARAMETER, event, String.class);
        GetTemplateModel model = getModel(templateKey);
        File tempFile = fileService.createTempFile(templateKey + ".ftl");
        try {
            fileService.writeContentToFile(tempFile, model.getTemplateContent());
            MessageToSend messageToSend = templateService.renderEmbedTemplate(GET_TEMPLATE_RESPONSE_TEMPLATE_KEY, model, event.getGuild().getIdLong());
            messageToSend.getAttachedFiles().get(0).setFile(tempFile);
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } catch (IOException e) {
            throw new AbstractoRunTimeException(e);
        } finally {
            try {
                fileService.safeDelete(tempFile);
            } catch (IOException e) {
                log.error("Failed to delete temporary get template file {}.", tempFile.getAbsoluteFile(), e);
            }
        }
    }

    private GetTemplateModel getModel(String templateKey) {
        return templateManagementService.getTemplateByKey(templateKey)
            .map(template -> GetTemplateModel
                .builder()
                .created(template.getCreated())
                .lastModified(template.getLastModified())
                .templateContent(template.getContent())
                .templateKey(templateKey)
                .build()).orElseThrow(() -> new TemplateNotFoundException(templateKey));
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        Parameter templateKeyParameter = Parameter
                .builder()
                .name(TEMPLATE_KEY_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(templateKeyParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.INTERNAL)
                .commandName(GET_TEMPLATE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(GET_TEMPLATE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .supportsEmbedException(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
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
