package dev.sheldan.abstracto.utility.models.database;

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

    @Column(name = "enabled")
    private Boolean checkEnabled;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
