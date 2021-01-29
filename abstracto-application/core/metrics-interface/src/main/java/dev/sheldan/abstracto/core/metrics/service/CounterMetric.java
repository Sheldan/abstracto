package dev.sheldan.abstracto.core.metrics.service;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode
public class CounterMetric implements Metric {
    private String name;
    @Builder.Default
    private List<MetricTag> tagList = new ArrayList<>();

}
