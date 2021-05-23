package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profanity_group")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ProfanityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String groupName;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "group")
    @Builder.Default
    private List<ProfanityRegex> profanities = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="server_id", nullable = false)
    @Getter
    @Setter
    private AServer server;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

}
