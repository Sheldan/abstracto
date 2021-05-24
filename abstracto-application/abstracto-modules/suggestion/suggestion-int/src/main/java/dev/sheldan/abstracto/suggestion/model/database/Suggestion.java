package dev.sheldan.abstracto.suggestion.model.database;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

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
public class Suggestion implements Serializable {

    @Id
    @Getter
    @EmbeddedId
    private ServerSpecificId suggestionId;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggester_user_in_server_id", nullable = false)
    private AUserInAServer suggester;

    @Getter
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private AChannel channel;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private SuggestionState state;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Column(name = "suggestion_text", nullable = false)
    private String suggestionText;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_channel_id")
    private AChannel commandChannel;

    @Column(name = "command_message_id")
    private Long commandMessageId;

    @Column(name = "job_trigger_key")
    private String suggestionReminderJobTriggerKey;

}
