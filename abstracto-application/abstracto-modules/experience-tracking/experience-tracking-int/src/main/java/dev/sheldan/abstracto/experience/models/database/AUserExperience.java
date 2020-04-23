package dev.sheldan.abstracto.experience.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;


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
}
