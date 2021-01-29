package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReactionPostExecution implements PostCommandExecution {

    public static final String WARN_REACTION_EMOTE = "warnReaction";
    public static final String SUCCESS_REACTION_EMOTE = "successReaction";
    @Autowired
    private ReactionService reactionService;

    @Autowired
    private ChannelService channelService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.ERROR) || result.equals(ResultState.REPORTED_ERROR)) {
            reactionService.addReactionToMessage(WARN_REACTION_EMOTE, commandContext.getGuild().getIdLong(), commandContext.getMessage());
            if(commandResult.getMessage() != null && commandResult.getThrowable() == null){
                channelService.sendTextToChannel(commandResult.getMessage(), commandContext.getChannel());
            }
        } else if(result.equals(ResultState.SUCCESSFUL) && command.getConfiguration().isCausesReaction()) {
            reactionService.addReactionToMessage(SUCCESS_REACTION_EMOTE, commandContext.getGuild().getIdLong(), commandContext.getMessage());
        }

    }
}
