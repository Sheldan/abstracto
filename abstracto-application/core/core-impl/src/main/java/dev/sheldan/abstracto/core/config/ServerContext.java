package dev.sheldan.abstracto.core.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerContext {
    private Long serverId;

    public void clear() {
        this.serverId = null;
    }
}
