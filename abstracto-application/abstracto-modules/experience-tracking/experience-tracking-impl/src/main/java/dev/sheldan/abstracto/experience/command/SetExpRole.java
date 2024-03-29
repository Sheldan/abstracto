package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
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

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Integer level = (Integer) commandContext.getParameters().getParameters().get(0);
        Role role = (Role) commandContext.getParameters().getParameters().get(1);
        if(!role.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        log.info("Setting role  {} to be used for level {} on server {}", role.getId(), level, role.getGuild().getId());
        return experienceRoleService.setRoleToLevel(role, level, commandContext.getChannel())
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        List<ParameterValidator> levelValidators = Arrays.asList(MinIntegerValueValidator.min(0L));
        Parameter level = Parameter
                .builder()
                .name("level")
                .validators(levelValidators)
                .templated(true)
                .type(Integer.class)
                .build();
        parameters.add(level);
        Parameter role = Parameter
                .builder()
                .name("role")
                .templated(true)
                .type(Role.class)
                .build();
        parameters.add(role);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("setExpRole")
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .async(true)
                .messageCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .requiresConfirmation(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
