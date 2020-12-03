package dev.sheldan.abstracto.utility.models.database.embed;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PostIdentifier implements Serializable {
    @Column(name = "message_id")
    private Long messageId;
    @Column(name = "position")
    private Integer position;
}
