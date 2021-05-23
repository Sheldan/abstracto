package dev.sheldan.abstracto.profanityfilter.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profanity_use")
@Getter
@Setter
@EqualsAndHashCode
public class ProfanityUse {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "profanity_group_id", referencedColumnName = "id", nullable = false)
    private ProfanityGroup profanityGroup;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "profanity_user_in_server_id", referencedColumnName = "id", nullable = false)
    private ProfanityUserInAServer profanityUser;

    @Column(name = "report_message_id", nullable = false)
    @Id
    private Long reportMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_channel_id", nullable = false)
    private AChannel reportChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "profane_message_id", nullable = false)
    private Long profaneMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profane_channel_id", nullable = false)
    private AChannel profaneChannel;

    @Column(name = "confirmed", nullable = false)
    private Boolean confirmed;

    @Column(name = "verified", nullable = false)
    private Boolean verified;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
