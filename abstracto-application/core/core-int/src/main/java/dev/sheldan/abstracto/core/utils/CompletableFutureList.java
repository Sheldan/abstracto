package dev.sheldan.abstracto.core.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A wrapper object which contains a list of {@link CompletableFuture futures} of the same type, and a primary {@link CompletableFuture future} which completes, when *all* of the futures complete
 * @param <T> The return value of the individual {@link CompletableFuture futures}
 */
@Getter
@Setter
@Slf4j
public class CompletableFutureList<T> {
    /**
     * The primary {@link CompletableFuture future} which completes once all futures in the list complete, this will complete erroneously, if any of them do so
     */
    private CompletableFuture<Void> mainFuture;
    /**
     * The list of {@link CompletableFuture futures} which are wrapped and should complete.
     */
    private List<CompletableFuture<T>> futures;

    public CompletableFutureList(List<CompletableFuture<T>> futures) {
       this.mainFuture =  CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
       this.futures = futures;
    }

    /**
     * Returns all results of the {@link CompletableFuture futures}, for those who completed, the ones which were not completed successfully are not returned
     * @return A {@link List list} of objects, which were returned by the {@link CompletableFuture futures}.
     */
    public List<T> getObjects() {
        List<T> result = new ArrayList<>();
        futures.forEach(future -> {
            if(!future.isCompletedExceptionally()) {
                result.add(future.join());
            } else {
                log.warn("Future completed with exception {}.", future.join());
            }
        });
        return result;
    }
}
