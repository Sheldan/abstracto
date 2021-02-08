package dev.sheldan.abstracto.experience.models.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;


/**
 * Mapping table responsible for tracking the experience and message count of a user in a specific server.
 * For easier lookup also contains the current level and the currently awarded experience role.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_experience")
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AUserExperience implements Serializable {

    /**
     * The {@link AUserInAServer} id which is unique for each user in a server.
     */
    @Id
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AUserInAServer user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    /**
     * The total amount of experience the user has in the guild
     */
    @Column(name = "experience")
    private Long experience;

    /**
     * The total amount of messages the user has written in the guild resulting in the experience.
     */
    @Column(name = "message_count")
    private Long messageCount;

    /**
     * Whether or not the experience gain has been disabled for this user
     */
    @Column(name = "experience_gain_disabled")
    private Boolean experienceGainDisabled;

    /**
     * The {@link AExperienceLevel } which the user currently has.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "level_id", nullable = false)
    private AExperienceLevel currentLevel;

    /**
     * The {@link AExperienceRole} the user currently has. Can be null.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "role_id")
    private AExperienceRole currentExperienceRole;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

    public Integer getLevelOrDefault() {
        return currentLevel != null ? currentLevel.getLevel() : 0;
    }
}
