package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="channel_group")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AChannelGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_type_id", nullable = false)
    private ChannelGroupType channelGroupType;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AChannelGroupCommand> channelGroupCommands;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @ManyToMany
    @JoinTable(
            name = "channel_in_group",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id"))
    private List<AChannel> channels;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;


}
