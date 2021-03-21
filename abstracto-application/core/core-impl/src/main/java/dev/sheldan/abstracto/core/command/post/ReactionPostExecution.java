package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureConfig;
import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.ReactionService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReactionPostExecution implements PostCommandExecution {

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ConfigService configService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.ERROR) || result.equals(ResultState.REPORTED_ERROR)) {
            if(commandResult.getThrowable() instanceof CommandNotFoundException){
                String configValue = configService.getStringValueOrConfigDefault(CoreFeatureConfig.NO_COMMAND_REPORTING_CONFIG_KEY, commandContext.getGuild().getIdLong());
                if(!BooleanUtils.toBoolean(configValue)) {
                    return;
                }
            }
            reactionService.addReactionToMessage(CoreFeatureConfig.WARN_REACTION_KEY, commandContext.getGuild().getIdLong(), commandContext.getMessage());
            if(commandResult.getMessage() != null && commandResult.getThrowable() == null){
                channelService.sendTextToChannel(commandResult.getMessage(), commandContext.getChannel());
            }
        } else if(result.equals(ResultState.SUCCESSFUL) && command.getConfiguration().isCausesReaction()) {
            reactionService.addReactionToMessage(CoreFeatureConfig.SUCCESS_REACTION_KEY, commandContext.getGuild().getIdLong(), commandContext.getMessage());
        }

    }
}
