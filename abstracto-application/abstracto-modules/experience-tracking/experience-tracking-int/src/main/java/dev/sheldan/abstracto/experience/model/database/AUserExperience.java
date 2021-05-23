package dev.sheldan.abstracto.experience.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

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
public class AUserExperience implements Serializable {

    /**
     * The ID of the {@link AUserInAServer user} which is represented by this object
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The {@link AUserInAServer user} which is represented by this object
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AUserInAServer user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    /**
     * The total amount of experience the user has in the guild
     */
    @Column(name = "experience", nullable = false)
    private Long experience;

    /**
     * The total amount of messages the user has written in the guild resulting in the experience.
     */
    @Column(name = "message_count", nullable = false)
    private Long messageCount;

    /**
     * Whether or not the experience gain has been disabled for this user
     */
    @Builder.Default
    @Column(name = "experience_gain_disabled", nullable = false)
    private Boolean experienceGainDisabled = false;

    /**
     * The {@link AExperienceLevel level} which the user currently has.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "level_id", nullable = false)
    private AExperienceLevel currentLevel;

    /**
     * The {@link AExperienceRole role} the user currently has. Can be null.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "role_id")
    private AExperienceRole currentExperienceRole;

    /**
     * The {@link Instant} this entity was created
     */
    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    /**
     * The {@link Instant} this entity was updated
     */
    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    public Integer getLevelOrDefault() {
        return currentLevel != null ? currentLevel.getLevel() : 0;
    }
}
