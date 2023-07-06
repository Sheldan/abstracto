package dev.sheldan.abstracto.core.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CounterId implements Serializable {
    @Column(name = "server_id")
    private Long serverId;
    @Column(name = "counter_key")
    private String counterKey;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CounterId counterId = (CounterId) o;
        return Objects.equals(serverId, counterId.serverId) &&
                Objects.equals(counterKey, counterId.counterKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, counterKey);
    }
}
