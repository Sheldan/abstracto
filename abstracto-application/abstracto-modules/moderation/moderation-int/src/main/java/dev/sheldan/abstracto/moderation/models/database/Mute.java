package dev.sheldan.abstracto.moderation.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mute mute = (Mute) o;
        return Objects.equals(id, mute.id) &&
                Objects.equals(mutedUser, mute.mutedUser) &&
                Objects.equals(mutingUser, mute.mutingUser) &&
                Objects.equals(reason, mute.reason) &&
                Objects.equals(muteDate, mute.muteDate) &&
                Objects.equals(muteTargetDate, mute.muteTargetDate) &&
                Objects.equals(muteEnded, mute.muteEnded) &&
                Objects.equals(messageId, mute.messageId) &&
                Objects.equals(mutingServer, mute.mutingServer) &&
                Objects.equals(mutingChannel, mute.mutingChannel) &&
                Objects.equals(triggerKey, mute.triggerKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mutedUser, mutingUser, reason, muteDate, muteTargetDate, muteEnded, messageId, mutingServer, mutingChannel, triggerKey);
    }
}
