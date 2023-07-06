package dev.sheldan.abstracto.twitch.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="stream_session_section")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class StreamSessionSection {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "streamer_id", nullable = false)
    private Streamer streamer;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_session_id", nullable = false)
    private StreamSession session;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "viewer_count", nullable = false)
    private Integer viewerCount;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
