package dev.sheldan.abstracto.activity.models;

import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity")
@Getter
@Setter
@EqualsAndHashCode
public class CustomActivity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ActivityType type;

    @Column(name = "template_key")
    private String templateKey;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;
}
