package dev.sheldan.abstracto.repostdetection.model.database;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "repost_check_channel_group")
@Getter
@Setter
@EqualsAndHashCode
public class RepostCheckChannelGroup {

    @Id
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AChannelGroup channelGroup;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean checkEnabled = true;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
