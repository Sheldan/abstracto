package dev.sheldan.abstracto.profanityfilter.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profanity_user_in_server")
@Getter
@Setter
@EqualsAndHashCode
public class ProfanityUserInAServer {
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * The {@link AUserInAServer user} which is represented by this object
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AUserInAServer user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

    @OneToMany(mappedBy = "profanityUser", fetch = FetchType.LAZY)
    private List<ProfanityUse> usedProfanities;
}
