package dev.sheldan.abstracto.core.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
@Slf4j
public class CompletableFutureMap<L, T> {
    private final CompletableFuture<Void> mainFuture;
    private final Map<L, CompletableFuture<T>> futures;

    public CompletableFutureMap(Map<L ,CompletableFuture<T>> futures) {
       this.mainFuture = CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]));
       this.futures = futures;
    }

    public List<T> getObjects() {
        List<T> result = new ArrayList<>();
        futures.values().forEach(future -> {
            if(!future.isCompletedExceptionally()) {
                result.add(future.join());
            } else {
                try {
                    future.join();
                } catch (Exception exception) {
                    log.warn("Future completed with exception.", exception);
                }
            }
        });
        return result;
    }

    public T getElement(L key) {
        if(!getFutures().containsKey(key)) {
            return null;
        }
        CompletableFuture<T> future = getFutures().get(key);
        if(!future.isCompletedExceptionally()) {
            return future.join();
        } else {
            return null;
        }
    }
}
