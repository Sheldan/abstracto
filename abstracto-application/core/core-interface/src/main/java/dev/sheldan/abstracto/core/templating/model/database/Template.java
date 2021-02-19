package dev.sheldan.abstracto.core.templating.model.database;

import lombok.*;

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
@Getter
public class Template implements Serializable {

    /**
     * The globally unique key of the template
     */
    @Id
    @Column(name = "key")
    private String key;

    /**
     * The content of the template
     */
    @Column(length = 4000, name = "content")
    private String content;

    @Column(name = "section")
    private String section;

    @Column(name = "last_modified")
    private Instant lastModified;

    /**
     * The time this template was created in the database
     */
    @Column(name = "created")
    private Instant created;

    /**
     * The time this template was updated in the database, only works when using the bot, does not work with triggers.
     */
    @Column(name = "updated")
    private Instant updated;

}
