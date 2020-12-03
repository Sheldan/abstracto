package dev.sheldan.abstracto.utility.models.database.embed;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RepostIdentifier implements Serializable {
    @Column(name = "message_id")
    private Long messageId;
    @Column(name = "position")
    private Integer position;
    @Column(name = "user_in_server_id")
    private Long userInServerId;
}
