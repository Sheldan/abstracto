package dev.sheldan.abstracto.core.templating.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * Represents the template stored in the database.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "custom_template")
@Getter
@Setter
@EqualsAndHashCode
public class CustomTemplate implements Serializable {

    /**
     * The globally unique key of the template
     */
    @Id
    @Column(name = "key", nullable = false)
    private String key;

    /**
     * The content of the template
     */
    @Column(length = 4000, name = "content", nullable = false)
    private String content;

    @Column(name = "section")
    private String section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "last_modified")
    private Instant lastModified;

    /**
     * The time this template was created in the database
     */
    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    /**
     * The time this template was updated in the database, only works when using the bot, does not work with triggers.
     */
    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
