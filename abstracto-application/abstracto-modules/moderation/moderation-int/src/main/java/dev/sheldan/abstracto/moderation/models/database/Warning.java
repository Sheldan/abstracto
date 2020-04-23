package dev.sheldan.abstracto.moderation.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="warning")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Warning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @ManyToOne
    @JoinColumn(name = "warnedUserId", nullable = false)
    private AUserInAServer warnedUser;

    @Getter
    @ManyToOne
    @JoinColumn(name = "warningUserId", nullable = false)
    private AUserInAServer warningUser;

    @Getter
    private String reason;

    @Getter
    private Instant warnDate;

    @Getter
    private Boolean decayed;

    @Getter
    private Instant decayDate;


}
