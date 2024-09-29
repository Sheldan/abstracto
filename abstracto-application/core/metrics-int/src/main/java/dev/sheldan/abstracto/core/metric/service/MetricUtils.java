package dev.sheldan.abstracto.core.metric.service;

import io.micrometer.context.ContextSnapshotFactory;

import java.util.concurrent.Executor;

public class MetricUtils {
    public static Executor wrapExecutor(Executor e) {
        return ContextSnapshotFactory
                .builder()
                .build()
                .captureAll()
                .wrapExecutor(e);
    }
}
