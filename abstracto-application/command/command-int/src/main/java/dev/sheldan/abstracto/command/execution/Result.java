package dev.sheldan.abstracto.command.execution;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class Result {
    private ResultState result;
    private String message;
    private Throwable throwable;

    public static Result fromSuccess() {
        return Result.builder().result(ResultState.SUCCESSFUL).build();
    }

    public static Result fromError(String message){
        return Result.builder().result(ResultState.ERROR).message(message).build();
    }

    public static Result fromError(String message, Throwable throwable) {
        return Result.builder().result(ResultState.ERROR).message(message).throwable(throwable).build();
    }
}
