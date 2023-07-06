package dev.sheldan.abstracto.twitch.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="streamer")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Streamer {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "template_key")
    private String templateKey;

    @Column(name = "current_game_id")
    private String currentGameId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_channel_id")
    private AChannel notificationChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_in_server_id", nullable = false)
    private AUserInAServer creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "streamer_user_in_server_id")
    private AUserInAServer streamerUser;

    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_session_id")
    private StreamSession currentSession;

    @Column(name = "show_notifications", nullable = false)
    private Boolean showNotifications;

    @Column(name = "online", nullable = false)
    private Boolean online;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @EqualsAndHashCode.Exclude
    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "streamer")
    @Builder.Default
    private List<StreamSession> sessions = new ArrayList<>();
}
