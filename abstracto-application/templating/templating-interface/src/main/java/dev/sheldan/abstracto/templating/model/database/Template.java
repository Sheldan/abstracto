package dev.sheldan.abstracto.templating.model.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents the template stored in the database.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "template")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Template {

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
        Template template = (Template) o;
        return Objects.equals(key, template.key) &&
                Objects.equals(content, template.content) &&
                Objects.equals(section, template.section) &&
                Objects.equals(lastModified, template.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, content, section, lastModified);
    }
}
