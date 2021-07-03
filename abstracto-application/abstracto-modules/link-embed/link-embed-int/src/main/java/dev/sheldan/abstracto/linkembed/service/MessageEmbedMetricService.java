package dev.sheldan.abstracto.linkembed.service;

public interface MessageEmbedMetricService {
    void incrementMessageEmbedDeletedMetric(boolean embeddedUserDeleted);
    void incrementMessageEmbedDeletedEmbeddedMetric();
    void incrementMessageEmbedDeletedEmbeddingMetric();
}
