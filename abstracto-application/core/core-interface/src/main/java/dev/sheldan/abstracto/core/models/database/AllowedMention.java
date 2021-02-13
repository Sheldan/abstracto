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
    @Column(name = "server_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId
    @JoinColumn(name = "server_id")
    private AServer server;

    @Getter
    @Column(name = "everyone_mention")
    private Boolean everyone;

    @Getter
    @Column(name = "user_mention")
    private Boolean user;

    @Getter
    @Column(name = "role_mention")
    private Boolean role;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

    public boolean allAllowed() {
        return everyone && user && role;
    }

}
