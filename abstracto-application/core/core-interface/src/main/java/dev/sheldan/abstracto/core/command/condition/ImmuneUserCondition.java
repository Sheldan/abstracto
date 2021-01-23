package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.ImmuneUserConditionDetail;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private TemplateService templateService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        ACommand aCommand = commandService.findCommandByName(command.getConfiguration().getName());
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, context.getUserInitiatedContext().getGuild().getIdLong());
        Optional<Member> any = context.getParameters().getParameters().stream().filter(o -> o instanceof Member).map(this::toMember).findAny();
        if(any.isPresent()) {
            Member member = any.get();
            for (ARole role : commandForServer.getImmuneRoles()) {
                if (roleService.memberHasRole(member, role)) {
                    log.trace("Member {} is immune against command {}, because of role {}.", member.getIdLong(), aCommand.getName(), role.getId());
                    ImmuneUserConditionDetail exception = new ImmuneUserConditionDetail(roleService.getRoleFromGuild(role));
                    return ConditionResult.builder().result(false).conditionDetail(exception).build();
                }
            }
        }
        return ConditionResult.builder().result(true).build();
    }

    private Member toMember(Object o) {
        return (Member) o;
    }
}
