package dev.sheldan.abstracto.modmail.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The table responsible to associate certain channels to be mod mail thread channels. It also stores the current state of the thread (answered, initial)
 * ,who the associated user is, who has subscribed to the thread and the messages posted in the thread.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mod_mail_thread")
@Getter
@Setter
@EqualsAndHashCode
public class ModMailThread implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The member who opened the thread or who got contacted
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_in_server_id", nullable = false)
    private AUserInAServer user;

    /**
     * The text channel in which this thread is dealt with
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private AChannel channel;

    /**
     * The server on which this mod mail thread is, for convenience
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Column(name = "closed")
    private Instant closed;

    /**
     * The messages which were officially posted in the context of the mod mail thread. Either via command (from the
     * staff side of view) or by messaging the bot (from the member view)
     */
    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "threadReference")
    @Builder.Default
    private List<ModMailMessage> messages = new ArrayList<>();

    /**
     * The staff members who subscribed to be notified in case there is a new message in a mod mail thread.
     */
    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "threadReference")
    @Builder.Default
    private List<ModMailThreadSubscriber> subscribers = new ArrayList<>();

    /**
     * The current state of the mod mail thread. Whether or not the last post was by staff or user.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private ModMailThreadState state;

}
