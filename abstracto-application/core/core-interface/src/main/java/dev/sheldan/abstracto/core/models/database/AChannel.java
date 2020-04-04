package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;
import net.dv8tion.jda.api.entities.ChannelType;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

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

    public static AChannelType getAChannelType(ChannelType type) {
        switch (type) {
            case TEXT: return AChannelType.TEXT;
            case PRIVATE: return AChannelType.DM;
            case VOICE: return AChannelType.VOICE;
            case CATEGORY: return AChannelType.CATEGORY;
            default: return AChannelType.UNKOWN;
        }
    }
}
