package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "context_command_in_server")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ContextCommandInServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "command_in_server_id", nullable = false)
    private Long commandInServerId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "command_id", nullable = false)
    private ContextCommand commandReference;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "server_id", nullable = false)
    private AServer serverReference;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Column(name = "context_command_id")
    private Long contextCommandId;
}
