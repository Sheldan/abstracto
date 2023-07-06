package dev.sheldan.abstracto.repostdetection.model.database.embed;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
