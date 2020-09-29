package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInGuildException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to define which commands are to be awarded at which level
 */
@Component
@Slf4j
public class SetExpRole extends AbstractConditionableCommand {

    @Autowired
    private ExperienceRoleService experienceRoleService;

    @Autowired
    private RoleService roleService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        checkParameters(commandContext);
        Integer level = (Integer) commandContext.getParameters().getParameters().get(0);
        ARole role = (ARole) commandContext.getParameters().getParameters().get(1);
        AServer server = commandContext.getUserInitiatedContext().getServer();
        if(!roleService.isRoleInServer(role)) {
            throw new RoleNotFoundInGuildException(role.getId(), server.getId());
        }
        log.info("Setting role  {} to be used for level {} on server {}", role.getId(), level, server.getId());
        return experienceRoleService.setRoleToLevel(role, level, commandContext.getUserInitiatedContext().getChannel())
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        List<ParameterValidator> levelValidators = Arrays.asList(MinIntegerValueValidator.min(0L));
        parameters.add(Parameter.builder().name("level").validators(levelValidators).templated(true).type(Integer.class).build());
        parameters.add(Parameter.builder().name("role").templated(true).type(ARole.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("setExpRole")
                .module(ExperienceModule.EXPERIENCE)
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
        return ExperienceFeature.EXPERIENCE;
    }
}
