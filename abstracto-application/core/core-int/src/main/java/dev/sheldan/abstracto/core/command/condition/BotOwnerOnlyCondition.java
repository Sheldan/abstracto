package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.NotBotOwnerConditionDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.ApplicationTeam;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class BotOwnerOnlyCondition implements CommandCondition {

    @Override
    public CompletableFuture<ConditionResult> shouldExecuteAsync(CommandContext commandContext, Command command) {
        return commandContext.getJda().retrieveApplicationInfo().submit()
            .thenApply(applicationInfo -> {
                ApplicationTeam team = applicationInfo.getTeam();
                boolean hasTeam = team != null;
                if(hasTeam && team.isMember(commandContext.getAuthor().getUser())) {
                    return ConditionResult.fromSuccess();
                } else if(!hasTeam && applicationInfo.getOwner().getId().equals(commandContext.getAuthor().getUser().getId())) {
                    return ConditionResult.fromSuccess();
                } else {
                    return ConditionResult.fromFailure(new NotBotOwnerConditionDetail());
                }
            });
    }

    @Override
    public CompletableFuture<ConditionResult> shouldExecuteAsync(SlashCommandInteractionEvent slashCommandInteractionEvent, Command command) {
        return slashCommandInteractionEvent.getJDA().retrieveApplicationInfo().submit()
            .thenApply(applicationInfo -> {
                ApplicationTeam team = applicationInfo.getTeam();
                boolean hasTeam = team != null;
                if(hasTeam && team.isMember(slashCommandInteractionEvent.getUser())) {
                    return ConditionResult.fromSuccess();
                } else if(!hasTeam && applicationInfo.getOwner().getId().equals(slashCommandInteractionEvent.getInteraction().getUser().getId())) {
                    return ConditionResult.fromSuccess();
                } else {
                    return ConditionResult.fromFailure(new NotBotOwnerConditionDetail());
                }
            });
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
