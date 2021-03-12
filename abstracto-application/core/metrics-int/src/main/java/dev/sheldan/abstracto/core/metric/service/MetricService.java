package dev.sheldan.abstracto.core.metric.service;

import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public interface MetricService {
    void registerCounter(CounterMetric counterMetric, String help);
    void registerGauge(CounterMetric counterMetric, Supplier<Number> f, String help);
    void registerGauge(CounterMetric counterMetric, Supplier<Number> f, String help, String baseUnit);
    <T> void registerGauge(CounterMetric counterMetric, T obj, ToDoubleFunction<T> f, String help);
    <T> void registerGauge(CounterMetric counterMetric, T obj, ToDoubleFunction<T> f, String help, String baseUnit);
    void incrementCounter(CounterMetric name);
    void incrementCounter(CounterMetric name, Long amount);
}