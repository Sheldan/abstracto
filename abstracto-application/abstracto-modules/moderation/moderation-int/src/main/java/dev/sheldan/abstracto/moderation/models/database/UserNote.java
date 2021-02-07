package dev.sheldan.abstracto.moderation.models.database;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="user_note")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserNote implements Serializable {

    @EmbeddedId
    private ServerSpecificId userNoteId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @ManyToOne
    @JoinColumn(name = "user_in_server_id", nullable = false)
    private AUserInAServer user;

    @Column(length = 2000, name = "note")
    private String note;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
