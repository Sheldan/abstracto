package dev.sheldan.abstracto.core.metrics.service;

import java.util.List;

public interface Metric {
    String getName();
    void setName(String name);
    List<MetricTag> getTagList();
    void setTagList(List<MetricTag> tagList);
}
