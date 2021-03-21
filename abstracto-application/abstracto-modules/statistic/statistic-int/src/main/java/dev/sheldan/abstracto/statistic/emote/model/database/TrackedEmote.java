package dev.sheldan.abstracto.statistic.emote.model.database;

import dev.sheldan.abstracto.core.models.Fakeable;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingFeatureConfig;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * The instance of an emote which is being tracked by {@link EmoteTrackingFeatureConfig}.
 * This represents an emote by its unique ID and the respective server its being tracked in. This emote might not be part of the server
 * and might have been deleted.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tracked_emote")
@Getter
@Setter
@EqualsAndHashCode
public class TrackedEmote implements Serializable, Fakeable {

    @EmbeddedId
    private ServerSpecificId trackedEmoteId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id",  referencedColumnName = "id", nullable = false)
    private AServer server;

    /**
     * The name of the {@link net.dv8tion.jda.api.entities.Emote} which is the same how it is identified within Discord.
     */
    @Column(name = "name", length = 32)
    private String emoteName;

    /**
     * Whether or not the emote is animated.
     */
    @Column(name = "animated")
    private Boolean animated;

    /**
     * Whether or not the tracking for this emote is enabled. Tracking enabled means, that the listener counts the usages of this emote
     * in the *Messages*.
     */
    @Column(name = "tracking_enabled")
    private Boolean trackingEnabled;

    /**
     * Whether or not the emote has been deleted from the server. This is only relevant for emotes which originated from the server
     * the feature is being used in. This does not have any meaning for external emotes.
     */
    @Column(name = "deleted")
    private Boolean deleted;

    /**
     * Whether or not the emote was *not* from the server the feature is being used in. This means, that the emote is from a foreign server
     * and we cannot identify the true server this emote is from.
     */
    @Column(name = "external")
    private Boolean external;

    /**
     * The URL of the picture which is associated with the emote. This is only used for external emotes and only actively used when
     * a user wants to see the picture of the emote with the command `showExternalTrackedEmote`
     */
    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

    /**
     * Some emotes are build on demand, and do not necessarily be persisted. This happens when the emote is being used as a
     * {@link dev.sheldan.abstracto.core.command.config.Parameter}.
     * If a command uses this as a parameter, it is advised to actually load the  {@link TrackedEmote} before using it, because it
     * is not guaranteed that it actually exists.
     */
    @Transient
    private boolean fake;
}
