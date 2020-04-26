package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConditionPostExecution implements PostCommandExecution {
    public static final String WARN_REACTION_EMOTE = "warnReaction";

    @Autowired
    private MessageService messageService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        if(commandResult.getResult().equals(ResultState.CONDITION)) {
            messageService.addReactionToMessage(WARN_REACTION_EMOTE, commandContext.getGuild().getIdLong(), commandContext.getMessage());
            if(commandResult.getConditionResult() != null && commandResult.getConditionResult().getReason() != null){
                commandContext.getChannel().sendMessage(commandResult.getConditionResult().getReason()).queue();
            }
        }
    }
}
