package dev.sheldan.abstracto.core.command.model.database;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CommandInServerAliasId implements Serializable {
    @Column(name = "command_in_server_id")
    private Long commandInServerId;
    @Column(name = "name")
    private String name;
}
