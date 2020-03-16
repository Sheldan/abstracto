package dev.sheldan.abstracto.templating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "template")
public class TemplateDto {

    @Id
    @Getter
    private String key;

    @Getter
    private String content;

    @Getter
    private String section;

    @Getter
    private Instant lastModified;
}
