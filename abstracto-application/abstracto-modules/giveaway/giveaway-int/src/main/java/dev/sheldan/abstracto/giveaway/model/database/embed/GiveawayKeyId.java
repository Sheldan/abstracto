package dev.sheldan.abstracto.giveaway.model.database.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class GiveawayKeyId implements Serializable {
    @Column(name = "id")
    private Long keyId;

    @Column(name = "server_id")
    private Long serverId;

}
