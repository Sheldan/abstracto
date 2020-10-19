package dev.sheldan.abstracto.templating.model.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * Represents the template stored in the database.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "template")
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Template implements Serializable {

    /**
     * The globally unique key of the template
     */
    @Id
    @Getter
    private String key;

    /**
     * The content of the template
     */
    @Getter
    @Column(length = 4000)
    private String content;

    @Getter
    private String section;

    @Getter
    private Instant lastModified;

    /**
     * The time this template was created in the database
     */
    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    /**
     * The time this template was updated in the database, only works when using the bot, does not work with triggers.
     */
    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

}
