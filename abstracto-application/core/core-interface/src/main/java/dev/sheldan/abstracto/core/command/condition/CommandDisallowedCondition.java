package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.InsufficientPermissionConditionDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CommandDisallowedCondition implements CommandCondition {


    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TemplateService templateService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        ACommand aCommand = commandService.findCommandByName(command.getConfiguration().getName());
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, context.getUserInitiatedContext().getServer());
        if(Boolean.FALSE.equals(commandForServer.getRestricted())) {
            return ConditionResult.builder().result(true).build();
        }
        for (ARole role : commandForServer.getAllowedRoles()) {
            Member author = context.getAuthor();
            if (roleService.memberHasRole(author, role)) {
                log.trace("Member {} is able to execute restricted command {}, because of role {}.", author.getIdLong(), aCommand.getName(), role.getId());
                return ConditionResult.builder().result(true).build();
            }
        }
        List<Role> allowedRoles = roleService.getRolesFromGuild(commandForServer.getAllowedRoles());
        InsufficientPermissionConditionDetail exception = new InsufficientPermissionConditionDetail(allowedRoles);
        return ConditionResult.builder().result(false).conditionDetail(exception).build();
    }
}
