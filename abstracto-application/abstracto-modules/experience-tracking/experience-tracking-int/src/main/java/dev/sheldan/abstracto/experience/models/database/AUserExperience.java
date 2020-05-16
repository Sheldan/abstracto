package dev.sheldan.abstracto.experience.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;


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
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AUserExperience implements Serializable {

    /**
     * The {@link AUserInAServer} id which is unique for each user in a server.
     */
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AUserInAServer user;

    /**
     * The total amount of experience the user has in the guild
     */
    private Long experience;

    /**
     * The total amount of messages the user has written in the guild resulting in the experience.
     */
    private Long messageCount;

    /**
     * Whether or not the experience gain has been disabled for this user
     */
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
    @JoinColumn(name = "experience_role_id")
    private AExperienceRole currentExperienceRole;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AUserExperience that = (AUserExperience) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(experience, that.experience) &&
                Objects.equals(messageCount, that.messageCount) &&
                Objects.equals(currentLevel, that.currentLevel) &&
                Objects.equals(currentExperienceRole, that.currentExperienceRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, experience, messageCount, currentLevel, currentExperienceRole);
    }
}
