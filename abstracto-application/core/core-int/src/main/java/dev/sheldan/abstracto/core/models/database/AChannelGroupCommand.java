package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "channel_group_command")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class AChannelGroupCommand implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "command_in_group_id", nullable = false)
    private Long commandInGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id", nullable = false)
    @Setter
    private ACommand command;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="server_id", nullable = false)
    private AServer server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @Setter
    private AChannelGroup group;

    @Setter
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
