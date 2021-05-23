package dev.sheldan.abstracto.core.command.model.database;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity(name = "command_in_server_alias")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ACommandInServerAlias implements Serializable {

    @EmbeddedId
    private CommandInServerAliasId aliasId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("commandInServerId")
    @JoinColumn(name = "command_in_server_id", referencedColumnName = "command_in_server_id", nullable = false)
    private ACommandInAServer commandInAServer;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

}
