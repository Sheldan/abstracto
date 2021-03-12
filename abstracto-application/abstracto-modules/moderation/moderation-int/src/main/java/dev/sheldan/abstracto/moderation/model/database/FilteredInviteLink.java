package dev.sheldan.abstracto.moderation.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "filtered_invite_link")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class FilteredInviteLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    /**
     * The amount of times, this invite code has been tried.
     */
    @Column(name = "uses")
    private Long uses;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
