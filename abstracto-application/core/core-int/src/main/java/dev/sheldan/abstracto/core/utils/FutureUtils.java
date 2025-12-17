package dev.sheldan.abstracto.core.utils;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor
public class FutureUtils {

    public static <T> CompletableFuture<Void> toSingleFutureGeneric(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static <T> CompletableFuture<Void> toSingleFutureGenericList(List<List<CompletableFuture<T>>> futures) {
        List<CompletableFuture<T>> allFutures = futures.stream().flatMap(List::stream).toList();
        return toSingleFutureGeneric(allFutures);
    }

    public static CompletableFuture<Void> toSingleFuture(List<CompletableFuture<?>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
