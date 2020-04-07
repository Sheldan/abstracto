package dev.sheldan.abstracto.utility.models;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="embedded_message")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmbeddedMessage {

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddedUser")
    private AUserInAServer embeddedUser;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddingUser")
    private AUserInAServer embeddingUser;

    @Getter
    @ManyToOne
    @JoinColumn(name = "originalServer")
    private AServer embeddedServer;

    @Getter
    @ManyToOne
    @JoinColumn(name = "originalChannel")
    private AChannel embeddedChannel;

    @Column
    private Long embeddedMessageId;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddingServer")
    private AServer embeddingServer;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddingChannel")
    private AChannel embeddingChannel;

    @Column
    @Id
    private Long embeddingMessageId;
}
