package dev.sheldan.abstracto.statistic.emotes.model.database.embed;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TrackedEmoteServer implements Serializable {

    @Column(name = "id")
    private Long emoteId;
    @Column(name = "server_id")
    private Long serverId;
}
