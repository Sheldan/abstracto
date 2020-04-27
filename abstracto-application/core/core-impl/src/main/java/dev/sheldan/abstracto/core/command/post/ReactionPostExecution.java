package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReactionPostExecution implements PostCommandExecution {

    public static final String WARN_REACTION_EMOTE = "warnReaction";
    public static final String SUCCESS_REACTION_EMOTE = "successReaction";
    @Autowired
    private MessageService messageService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.ERROR)) {
            messageService.addReactionToMessage(WARN_REACTION_EMOTE, commandContext.getGuild().getIdLong(), commandContext.getMessage());
            if(commandResult.getMessage() != null && commandResult.getThrowable() == null){
                commandContext.getChannel().sendMessage(commandResult.getMessage()).queue();
            }
        } else if(result.equals(ResultState.SUCCESSFUL) && command.getConfiguration().isCausesReaction()) {
            messageService.addReactionToMessage(SUCCESS_REACTION_EMOTE, commandContext.getGuild().getIdLong(), commandContext.getMessage());
        }

    }
}
