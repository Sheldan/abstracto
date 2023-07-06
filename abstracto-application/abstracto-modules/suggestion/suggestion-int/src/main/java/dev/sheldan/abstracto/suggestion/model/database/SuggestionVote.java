package dev.sheldan.abstracto.suggestion.model.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.database.embed.SuggestionVoterId;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="suggestion_vote")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class SuggestionVote {

    @Id
    @EmbeddedId
    private SuggestionVoterId suggestionVoteId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @MapsId("voterId")
    @JoinColumn(name = "voter_user_in_server_id", nullable = false)
    private AUserInAServer voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            {
                    @JoinColumn(updatable = false, insertable = false, name = "suggestion_id", referencedColumnName = "id"),
                    @JoinColumn(updatable = false, insertable = false, name = "server_id", referencedColumnName = "server_id")
            })
    private Suggestion suggestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private SuggestionDecision decision;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
