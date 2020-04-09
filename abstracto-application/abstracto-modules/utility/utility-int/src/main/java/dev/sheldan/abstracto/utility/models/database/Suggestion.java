package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="suggestion")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @ManyToOne
    @JoinColumn(name = "suggesterId")
    private AUserInAServer suggester;

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
    private Instant suggestionDate;

    @Getter
    @Enumerated(EnumType.STRING)
    private SuggestionState state;

}
