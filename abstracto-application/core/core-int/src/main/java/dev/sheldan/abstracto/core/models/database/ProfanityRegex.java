package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "profanity_regex")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ProfanityRegex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String regexName;

    @Column(name = "regex")
    private String regex;

    @Column(name = "replacement")
    private String replacement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="profanity_group_id", nullable = false)
    @Getter
    @Setter
    private ProfanityGroup group;

    @Column(name = "created")
    private Instant created;
}
