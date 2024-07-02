package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureConfig;
import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.interaction.InteractionExceptionService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExceptionPostExecution implements PostCommandExecution {

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private InteractionExceptionService interactionExceptionService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.ERROR)) {
            Throwable throwable = commandResult.getThrowable();
            if(throwable != null) {
                if(throwable instanceof CommandNotFoundException){
                    String configValue = configService.getStringValueOrConfigDefault(CoreFeatureConfig.NO_COMMAND_REPORTING_CONFIG_KEY, commandContext.getGuild().getIdLong());
                    if(!BooleanUtils.toBoolean(configValue)) {
                       return;
                    }
                }
                log.info("Exception handling for exception {}.", throwable.getClass().getSimpleName());
                exceptionService.reportExceptionToContext(throwable, commandContext, command);
            }
        }
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent interaction, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.ERROR)) {
            Throwable throwable = commandResult.getThrowable();
            if(throwable != null) {
                if(throwable instanceof CommandNotFoundException && ContextUtils.isNotUserCommand(interaction)){
                    String configValue = configService.getStringValueOrConfigDefault(CoreFeatureConfig.NO_COMMAND_REPORTING_CONFIG_KEY, interaction.getGuild().getIdLong());
                    if(!BooleanUtils.toBoolean(configValue)) {
                        return;
                    }
                }
                log.info("Exception handling for exception {}.", throwable.getClass().getSimpleName());
                interactionExceptionService.reportSlashException(throwable, interaction, command);
            }
        }

    }
    @Override
    public boolean supportsSlash() {
        return true;
    }
}
