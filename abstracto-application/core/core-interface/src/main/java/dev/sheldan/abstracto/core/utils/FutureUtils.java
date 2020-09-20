package dev.sheldan.abstracto.core.utils;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor
public class FutureUtils {

    public static <T> CompletableFuture<Void> toSingleFutureGeneric(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
