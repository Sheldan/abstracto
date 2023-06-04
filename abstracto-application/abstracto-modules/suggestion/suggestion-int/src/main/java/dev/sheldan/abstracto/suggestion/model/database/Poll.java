package dev.sheldan.abstracto.suggestion.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="poll")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Poll {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poll_id", nullable = false)
    private Long pollId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PollType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_in_server_id", nullable = false)
    private AUserInAServer creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private AChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PollState state;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "evaluation_job_trigger_key")
    private String evaluationJobTriggerKey;

    @Column(name = "reminder_job_trigger_key")
    private String reminderJobTriggerKey;

    @Column(name = "target_date")
    private Instant targetDate;

    @Column(name = "allow_multiple")
    private Boolean allowMultiple;

    @Column(name = "show_decisions")
    private Boolean showDecisions;

    @Column(name = "allow_addition")
    private Boolean allowAddition;

    @Column(name = "selection_menu_id")
    private String selectionMenuId;

    @Column(name = "add_option_button_id")
    private String addOptionButtonId;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "poll")
    @Builder.Default
    private List<PollOption> options = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "poll")
    @Builder.Default
    private List<PollUserDecision> decisions = new ArrayList<>();

}
