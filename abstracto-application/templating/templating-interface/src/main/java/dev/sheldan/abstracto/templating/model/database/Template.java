package dev.sheldan.abstracto.templating.model.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "template")
public class Template {

    @Id
    @Getter
    private String key;

    @Getter
    @Column(length = 4000)
    private String content;

    @Getter
    private String section;

    @Getter
    private Instant lastModified;
}
