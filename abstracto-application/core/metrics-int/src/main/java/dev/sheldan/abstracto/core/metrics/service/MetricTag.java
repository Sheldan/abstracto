package dev.sheldan.abstracto.core.metrics.service;

import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class MetricTag {
    private String key;
    private String value;

    public static MetricTag getTag(String key, String value) {
        return MetricTag.builder().key(key).value(value).build();
    }
}