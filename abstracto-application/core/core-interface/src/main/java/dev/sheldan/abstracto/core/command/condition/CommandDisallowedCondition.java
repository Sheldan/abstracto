package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.models.InsufficientPermissionMessage;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
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
            if (roleService.memberHasRole(context.getAuthor(), role)) {
                return ConditionResult.builder().result(true).build();
            }
        }
        InsufficientPermissionMessage insufficientPermissionMessage = InsufficientPermissionMessage
                .builder()
                .allowedRoles(roleService.getRolesFromGuild(commandForServer.getAllowedRoles()))
                .build();
        String message = templateService.renderTemplate("insufficient_role", insufficientPermissionMessage);
        return ConditionResult.builder().result(false).reason(message).build();
    }
}
