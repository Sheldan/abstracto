package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="suggestion")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @ManyToOne
    @JoinColumn(name = "suggesterId",
            nullable = false)
    private AUserInAServer suggester;

    @Getter
    private Long messageId;

    @Getter
    @ManyToOne
    @JoinColumn(name = "channelId")
    private AChannel channel;

    @Getter
    @ManyToOne
    @JoinColumn(name = "serverId")
    private AServer server;

    @Getter
    private Instant suggestionDate;

    @Getter
    @Enumerated(EnumType.STRING)
    private SuggestionState state;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Suggestion that = (Suggestion) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(suggester, that.suggester) &&
                Objects.equals(messageId, that.messageId) &&
                Objects.equals(channel, that.channel) &&
                Objects.equals(server, that.server) &&
                Objects.equals(suggestionDate, that.suggestionDate) &&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, suggester, messageId, channel, server, suggestionDate, state);
    }
}
