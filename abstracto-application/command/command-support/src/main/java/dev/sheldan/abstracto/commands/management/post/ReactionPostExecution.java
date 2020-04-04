package dev.sheldan.abstracto.commands.management.post;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.PostCommandExecution;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.CommandResult;
import dev.sheldan.abstracto.command.execution.ResultState;
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
        if(commandResult.getResult().equals(ResultState.ERROR)) {
            messageService.addReactionToMessage(WARN_REACTION_EMOTE, commandContext.getGuild().getIdLong(), commandContext.getMessage());
            if(commandResult.getMessage() != null && commandResult.getThrowable() == null){
                commandContext.getChannel().sendMessage(commandResult.getMessage()).queue();
            }
        } else {
            if(command.getConfiguration().isCausesReaction()){
                messageService.addReactionToMessage(SUCCESS_REACTION_EMOTE, commandContext.getGuild().getIdLong(), commandContext.getMessage());
            }
        }

    }
}
