package dev.sheldan.abstracto.core.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Slf4j
public class CompletableFutureList<T> {
    private CompletableFuture<Void> mainFuture;
    private List<CompletableFuture<T>> futures;

    public CompletableFutureList(List<CompletableFuture<T>> futures) {
       this.mainFuture =  CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
       this.futures = futures;
    }

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
