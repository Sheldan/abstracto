package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="posttarget")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PostTarget implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @Getter
    @Setter
    private AChannel channelReference;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="server_id", nullable = false)
    @Getter
    @Setter
    private AServer serverReference;

    @Column(name = "disabled")
    @Getter
    @Setter
    private Boolean disabled;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
