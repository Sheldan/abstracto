package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="posttarget")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @Getter @Setter
    private AChannel channelReference;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="server_id", nullable = false)
    @Getter @Setter
    private AServer serverReference;

}
