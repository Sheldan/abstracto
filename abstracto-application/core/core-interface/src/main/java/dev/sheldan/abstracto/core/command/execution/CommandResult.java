package dev.sheldan.abstracto.core.command.execution;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class CommandResult {
    private ResultState result;
    private String message;
    private Throwable throwable;

    public static CommandResult fromSuccess() {
        return CommandResult.builder().result(ResultState.SUCCESSFUL).build();
    }

    public static CommandResult fromError(String message){
        return CommandResult.builder().result(ResultState.ERROR).message(message).build();
    }

    public static CommandResult fromError(String message, Throwable throwable) {
        return CommandResult.builder().result(ResultState.ERROR).message(message).throwable(throwable).build();
    }
}
