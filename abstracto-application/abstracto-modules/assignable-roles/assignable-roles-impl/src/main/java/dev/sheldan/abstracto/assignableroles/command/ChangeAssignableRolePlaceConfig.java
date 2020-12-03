package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceParameterKey;
import dev.sheldan.abstracto.assignableroles.config.features.AssignableRoleFeature;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ChangeAssignableRolePlaceConfig extends AbstractConditionableCommand {

    @Autowired
    private AssignableRolePlaceService service;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String name = (String) parameters.get(0);
        AssignableRolePlaceParameterKey configKey = (AssignableRolePlaceParameterKey) parameters.get(1);
        Object parameterValue = parameters.get(2);
        return service.changeConfiguration(commandContext.getUserInitiatedContext().getServer(), name, configKey, parameterValue)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter assignableRolePlaceName = Parameter.builder().name("name").type(String.class).templated(true).build();
        Parameter parameterKey = Parameter.builder().name("key").type(AssignableRolePlaceParameterKey.class).templated(true).build();
        Parameter parameterValue = Parameter.builder().name("value").type(Object.class).templated(true).build();

        List<Parameter> parameters = Arrays.asList(assignableRolePlaceName, parameterKey, parameterValue);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("changeAssignableRolePlaceConfig")
                .module(AssignableRoleModule.ASSIGNABLE_ROLES)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return AssignableRoleFeature.ASSIGNABLE_ROLES;
    }
}