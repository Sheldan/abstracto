package dev.sheldan.abstracto.invitefilter.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="allowed_invite_link")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AllowedInviteLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "target_server_id", nullable = false)
    private Long targetServerId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
