package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "channel_group_type")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChannelGroupType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "group_type_key", nullable = false)
    private String groupTypeKey;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Builder.Default
    @Column(name = "allows_channel_in_multiple", nullable = false)
    private Boolean allowsChannelsInMultiple = true;

    @Builder.Default
    @Column(name = "allows_commands_in_multiple", nullable = false)
    private Boolean allowsCommandsInMultiple = true;
}
