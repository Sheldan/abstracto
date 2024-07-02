package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.AdminModeDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.service.ServerService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminModeCondition implements CommandCondition {

    @Autowired
    private ServerService service;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        boolean adminModeActive = service.adminModeActive(context.getGuild());
        if(adminModeActive){
            if(context.getAuthor().hasPermission(Permission.ADMINISTRATOR)) {
                return ConditionResult
                        .builder()
                        .result(true)
                        .build();
            } else {
                return ConditionResult
                        .builder()
                        .result(false)
                        .conditionDetail(new AdminModeDetail())
                        .build();
            }
        }
        return ConditionResult
                .builder()
                .result(true)
                .build();
    }

    @Override
    public ConditionResult shouldExecute(SlashCommandInteractionEvent slashCommandInteractionEvent, Command command) {
        if(ContextUtils.isUserCommand(slashCommandInteractionEvent)) {
            return ConditionResult.SUCCESS;
        }
        boolean adminModeActive = service.adminModeActive(slashCommandInteractionEvent.getGuild());
        if(adminModeActive){
            if(slashCommandInteractionEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                return ConditionResult
                        .builder()
                        .result(true)
                        .build();
            } else {
                return ConditionResult
                        .builder()
                        .result(false)
                        .conditionDetail(new AdminModeDetail())
                        .build();
            }
        }
        return ConditionResult
                .builder()
                .result(true)
                .build();
    }

    @Override
    public boolean supportsSlashCommands() {
        return true;
    }
}
