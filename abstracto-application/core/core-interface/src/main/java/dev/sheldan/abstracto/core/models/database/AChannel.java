package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;
import net.dv8tion.jda.api.entities.ChannelType;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="channel")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AChannel implements SnowFlake {

    @Id
    @Getter
    public Long id;

    @Getter
    @ManyToMany(mappedBy = "channels")
    private Set<AChannelGroup> groups;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter @Setter
    private AServer server;

    @Getter
    @Enumerated(EnumType.STRING)
    private AChannelType type;

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
