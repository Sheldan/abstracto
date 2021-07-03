package dev.sheldan.abstracto.core.interaction;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InteractionResult {
    private InteractionResultState result;
    private String message;
    private Throwable throwable;

    public static InteractionResult fromSuccess() {
        return InteractionResult.builder().result(InteractionResultState.SUCCESSFUL).build();
    }

    public static InteractionResult fromError(String message){
        return InteractionResult.builder().result(InteractionResultState.ERROR).message(message).build();
    }

    public static InteractionResult fromError(String message, Throwable throwable) {
        return InteractionResult.builder().result(InteractionResultState.ERROR).message(message).throwable(throwable).build();
    }

    public static InteractionResult fromIgnored() {
        return InteractionResult.builder().result(InteractionResultState.IGNORED).build();
    }

}
