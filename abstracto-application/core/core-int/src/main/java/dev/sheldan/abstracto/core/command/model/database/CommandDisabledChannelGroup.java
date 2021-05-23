package dev.sheldan.abstracto.core.command.model.database;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "command_disabled_channel_group")
@Getter
@Setter
@EqualsAndHashCode
public class CommandDisabledChannelGroup {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AChannelGroup channelGroup;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
