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
import dev.sheldan.abstracto.core.exception.CustomTemplateNotFoundException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.templating.model.database.CustomTemplate;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.management.CustomTemplateManagementService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ResetTemplate extends AbstractConditionableCommand {

    private static final String RESET_TEMPLATE_COMMAND = "resetTemplate";
    private static final String TEMPLATE_KEY_PARAMETER = "templateKey";
    private static final String RESET_TEMPLATE_RESPONSE_TEMPLATE_KEY = "resetTemplate_response";

    @Autowired
    private CustomTemplateManagementService customTemplateManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private BotOwnerOnlyCondition botOwnerOnlyCondition;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String templateKey = slashCommandParameterService.getCommandOption(TEMPLATE_KEY_PARAMETER, event, String.class);
        Optional<CustomTemplate> templateOptional = customTemplateManagementService.getCustomTemplate(templateKey, event.getGuild().getIdLong());
        if (templateOptional.isPresent()) {
            customTemplateManagementService.deleteCustomTemplate(templateOptional.get());
            templateService.clearCache();
            return interactionService.replyEmbed(RESET_TEMPLATE_RESPONSE_TEMPLATE_KEY, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        }
        throw new CustomTemplateNotFoundException(templateKey, event.getGuild());
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
                .commandName(RESET_TEMPLATE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(RESET_TEMPLATE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .supportsEmbedException(true)
                .parameters(parameters)
                .slashCommandConfig(slashCommandConfig)
                .help(helpInfo)
                .slashCommandOnly(true)
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
