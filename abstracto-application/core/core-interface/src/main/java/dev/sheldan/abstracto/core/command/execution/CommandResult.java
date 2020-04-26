package dev.sheldan.abstracto.core.command.execution;


import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommandResult {
    private ResultState result;
    private String message;
    private Throwable throwable;
    private ConditionResult conditionResult;

    public static CommandResult fromSuccess() {
        return CommandResult.builder().result(ResultState.SUCCESSFUL).build();
    }

    public static CommandResult fromError(String message){
        return CommandResult.builder().result(ResultState.ERROR).message(message).build();
    }

    public static CommandResult fromError(String message, Throwable throwable) {
        return CommandResult.builder().result(ResultState.ERROR).message(message).throwable(throwable).build();
    }

    public static CommandResult fromCondition(ConditionResult result) {
        return CommandResult.builder().conditionResult(result).result(ResultState.CONDITION).build();
    }
}
