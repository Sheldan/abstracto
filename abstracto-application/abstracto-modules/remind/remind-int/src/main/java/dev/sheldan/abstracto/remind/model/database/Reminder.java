package dev.sheldan.abstracto.remind.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="reminder")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Reminder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_in_server_id")
    private AUserInAServer remindedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AUser remindedAUser;

    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private AChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private AServer server;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant reminderDate;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Column(name = "target_date", nullable = false)
    private Instant targetDate;

    @Column(name = "text")
    private String text;

    @Builder.Default
    @Column(name = "send_dm", nullable = false)
    private Boolean sendInDm = false;

    @Builder.Default
    @Column(name = "user_command", nullable = false)
    private Boolean userCommand = false;

    @Builder.Default
    @Column(name = "reminded", nullable = false)
    private Boolean reminded = false;

    @Column(name = "job_trigger_key")
    private String jobTriggerKey;

}
