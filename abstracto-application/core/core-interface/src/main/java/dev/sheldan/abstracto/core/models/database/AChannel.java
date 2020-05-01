package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;
import net.dv8tion.jda.api.entities.ChannelType;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="channel")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AChannel implements SnowFlake {

    @Id
    @Getter
    @Column(name = "id")
    public Long id;

    @Getter
    @ManyToMany(mappedBy = "channels")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AChannelGroup> groups;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id", nullable = false)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AChannel channel = (AChannel) o;
        return Objects.equals(id, channel.id) &&
                Objects.equals(groups, channel.groups) &&
                Objects.equals(server, channel.server) &&
                type == channel.type &&
                Objects.equals(deleted, channel.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groups, server, type, deleted);
    }
}
