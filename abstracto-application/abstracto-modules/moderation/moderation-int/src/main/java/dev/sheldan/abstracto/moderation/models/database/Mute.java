package dev.sheldan.abstracto.moderation.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="mute")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Mute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mutedUser", nullable = false)
    private AUserInAServer mutedUser;

    @ManyToOne
    @JoinColumn(name = "mutingUser", nullable = false)
    private AUserInAServer mutingUser;

    private String reason;

    private Instant muteDate;

    private Instant muteTargetDate;

    private Boolean muteEnded;

    @Column
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "mutingServer", nullable = false)
    private AServer mutingServer;

    @ManyToOne
    @JoinColumn(name = "mutingChannel")
    private AChannel mutingChannel;

    private String triggerKey;


}
