package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;
import net.dv8tion.jda.api.entities.ChannelType;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="channel")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AChannel implements SnowFlake {

    @Id
    @Getter
    @Column(name = "id")
    public Long id;

    @Getter
    @ManyToMany(mappedBy = "channels")
    private List<AChannelGroup> groups;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id")
    private AServer server;

    @Getter
    @Enumerated(EnumType.STRING)
    private AChannelType type;

    @Getter
    @Setter
    @Column
    private Boolean deleted;


}
