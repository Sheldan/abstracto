package dev.sheldan.abstracto.giveaway.model.database;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "giveaway")
@Getter
@Setter
@EqualsAndHashCode
public class Giveaway {
    @Id
    @EmbeddedId
    private ServerSpecificId giveawayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private AUserInAServer creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefactor_user_id")
    private AUserInAServer benefactor;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "component_id")
    private String componentId;

    @Column(name = "winner_count", nullable = false)
    private Integer winnerCount;

    @Column(name = "target_date", nullable = false)
    private Instant targetDate;

    @Column(name = "cancelled", nullable = false)
    private Boolean cancelled;

    @Column(name = "reminder_trigger_key")
    private String reminderTriggerKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giveaway_channel_id", nullable = false)
    private AChannel giveawayChannel;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @OneToOne(mappedBy = "giveaway")
    private GiveawayKey giveawayKey;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "giveaway")
    @Builder.Default
    private List<GiveawayParticipant> participants = new ArrayList<>();

}
