package dev.sheldan.abstracto.vccontext.model;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="voice_channel_context")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class VoiceChannelContext implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ARole role;

}
