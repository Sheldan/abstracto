package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="allowed_mention")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class AllowedMention implements Serializable {

    @Id
    @Column(name = "server_id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId
    @JoinColumn(name = "server_id")
    private AServer server;

    @Getter
    @Builder.Default
    @Column(name = "everyone_mention", nullable = false)
    private Boolean everyone = false;

    @Getter
    @Builder.Default
    @Column(name = "user_mention", nullable = false)
    private Boolean user = false;

    @Getter
    @Builder.Default
    @Column(name = "role_mention", nullable = false)
    private Boolean role = false;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    public boolean allAllowed() {
        return everyone && user && role;
    }

}
