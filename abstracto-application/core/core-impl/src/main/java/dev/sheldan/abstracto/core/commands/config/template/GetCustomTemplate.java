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
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.CustomTemplateNotFoundException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.template.commands.GetCustomTemplateModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.model.database.CustomTemplate;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.CustomTemplateManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class GetCustomTemplate extends AbstractConditionableCommand {

    private static final String GET_CUSTOM_TEMPLATE_COMMAND = "getCustomTemplate";
    private static final String TEMPLATE_KEY_PARAMETER = "templateKey";
    private static final String GET_CUSTOM_TEMPLATE_RESPONSE_TEMPLATE_KEY = "getCustomTemplate_response";

    @Autowired
    private CustomTemplateManagementService customTemplateManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private FileService fileService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BotOwnerOnlyCondition botOwnerOnlyCondition;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String templateKey = (String) commandContext.getParameters().getParameters().get(0);
        GetCustomTemplateModel model = getModel(templateKey, commandContext.getGuild());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(GET_CUSTOM_TEMPLATE_RESPONSE_TEMPLATE_KEY, model, commandContext.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenAccept(interactionHook -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    private GetCustomTemplateModel getModel(String templateKey, Guild guild) {
        Optional<CustomTemplate> templateOptional = customTemplateManagementService.getCustomTemplate(templateKey, guild.getIdLong());
        return templateOptional.map(customTemplate -> {
            CustomTemplate template = templateOptional.get();
            return GetCustomTemplateModel
                    .builder()
                    .created(template.getCreated())
                    .lastModified(template.getLastModified())
                    .templateContent(template.getContent())
                    .templateKey(templateKey)
                    .build();
        }).orElseThrow(() -> new CustomTemplateNotFoundException(templateKey, guild));
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String templateKey = slashCommandParameterService.getCommandOption(TEMPLATE_KEY_PARAMETER, event, String.class);
        GetCustomTemplateModel model = getModel(templateKey, event.getGuild());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(GET_CUSTOM_TEMPLATE_RESPONSE_TEMPLATE_KEY, model, event.getGuild().getIdLong());
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenAccept(interactionHook -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter templateKeyParameter = Parameter
                .builder()
                .name(TEMPLATE_KEY_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(templateKeyParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .rootCommandName(CoreSlashCommandNames.INTERNAL)
                .commandName(GET_CUSTOM_TEMPLATE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(GET_CUSTOM_TEMPLATE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .supportsEmbedException(true)
                .parameters(parameters)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
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
