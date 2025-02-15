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
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.TemplateNotFoundException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.template.commands.GetTemplateModel;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.TemplateManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FileService fileService;

    @Autowired
    private BotOwnerOnlyCondition botOwnerOnlyCondition;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String templateKey = slashCommandParameterService.getCommandOption(TEMPLATE_KEY_PARAMETER, event, String.class);
        GetTemplateModel model = getModel(templateKey);
            MessageToSend messageToSend = templateService.renderEmbedTemplate(GET_TEMPLATE_RESPONSE_TEMPLATE_KEY, model, event.getGuild().getIdLong());
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenAccept(interactionHook -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
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
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName(GET_TEMPLATE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(GET_TEMPLATE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .supportsEmbedException(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .parameters(parameters)
                .slashCommandOnly(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(botOwnerOnlyCondition);
        return conditions;
    }
}
