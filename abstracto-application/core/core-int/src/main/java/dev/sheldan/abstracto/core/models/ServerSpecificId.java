package dev.sheldan.abstracto.core.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ServerSpecificId implements Serializable {
    @Column(name = "server_id")
    private Long serverId;
    @Column(name = "id")
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerSpecificId that = (ServerSpecificId) o;
        return Objects.equals(serverId, that.serverId) &&
                Objects.equals(id, that.id);
    }

    public ServerSpecificId() {
    }

    public ServerSpecificId(Long serverId, Long id) {
        this.serverId = serverId;
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, id);
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
