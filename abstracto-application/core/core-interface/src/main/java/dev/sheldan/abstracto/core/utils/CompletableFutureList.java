package dev.sheldan.abstracto.core.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
public class CompletableFutureList<T> {
    private CompletableFuture<Void> mainFuture;
    private List<CompletableFuture<T>> futures;
}
