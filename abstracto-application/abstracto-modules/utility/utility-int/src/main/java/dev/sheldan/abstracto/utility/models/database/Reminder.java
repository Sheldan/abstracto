package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="reminder")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @ManyToOne
    @JoinColumn(name = "remindedUser")
    private AUserInAServer toBeReminded;

    @Getter
    private Long messageId;

    @Getter
    @ManyToOne
    @JoinColumn(name = "channelId")
    private AChannel channel;

    @Getter
    @ManyToOne
    @JoinColumn(name = "serverId")
    private AServer server;

    @Getter
    private Instant reminderDate;

    @Getter
    private Instant targetDate;

    @Getter
    private String text;

    @Getter
    private boolean reminded;

}
