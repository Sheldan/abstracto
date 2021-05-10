package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.ImmuneUserConditionDetail;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.EffectConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.models.database.RoleImmunity;
import dev.sheldan.abstracto.core.service.RoleImmunityService;
import dev.sheldan.abstracto.core.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ImmuneUserCondition implements CommandCondition {

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleImmunityService roleImmunityService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        CommandConfiguration commandConfig = command.getConfiguration();
        if(commandConfig.getEffects().isEmpty()) {
            return ConditionResult.fromSuccess();
        }
        List<Object> parameters = context.getParameters().getParameters();
        for (EffectConfig effectConfig : commandConfig.getEffects()) {
            Integer position = effectConfig.getPosition();
            if (position < parameters.size()) {
                Object parameter = parameters.get(position);
                if (parameter instanceof Member) {
                    Member member = (Member) parameter;
                    Optional<RoleImmunity> immunityOptional = roleImmunityService.getRoleImmunity(member, effectConfig.getEffectKey());
                    if (immunityOptional.isPresent()) {
                        RoleImmunity immunity = immunityOptional.get();
                        ImmuneUserConditionDetail conditionDetail = new ImmuneUserConditionDetail(roleService.getRoleFromGuild(immunity.getRole()),
                                effectConfig.getEffectKey());
                        return ConditionResult.fromFailure(conditionDetail);
                    }
                }
            } else {
                log.info("Not enough parameters ({}) in command {} to retrieve position {} to check for immunity.",
                        parameters.size(), commandConfig.getName(), position);
            }
        }
        return ConditionResult.fromSuccess();
    }
}
