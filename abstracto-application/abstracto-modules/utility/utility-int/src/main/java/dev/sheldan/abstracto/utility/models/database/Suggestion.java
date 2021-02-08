package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="suggestion")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Suggestion implements Serializable {

    @Id
    @Getter
    @EmbeddedId
    private ServerSpecificId suggestionId;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggester_user_in_server_id")
    private AUserInAServer suggester;

    @Getter
    @Column(name = "message_id")
    private Long messageId;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private AChannel channel;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @Getter
    @Column(name = "suggestion_date")
    private Instant suggestionDate;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private SuggestionState state;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
