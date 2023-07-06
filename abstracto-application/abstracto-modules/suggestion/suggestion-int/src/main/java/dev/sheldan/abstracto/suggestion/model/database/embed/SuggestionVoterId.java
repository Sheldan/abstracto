package dev.sheldan.abstracto.suggestion.model.database.embed;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionVoterId implements Serializable {
    @Column(name = "voter_user_in_server_id")
    private Long voterId;

    @Column(name = "suggestion_id")
    private Long suggestionId;

    @Column(name = "server_id")
    private Long serverId;
}
