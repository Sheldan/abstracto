package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="channelGroup")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AChannelGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Setter
    private String groupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "group_server", nullable = false)
    private AServer server;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @ManyToMany
    @JoinTable(
            name = "channel_in_group",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id"))
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AChannel> channels;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AChannelGroup that = (AChannelGroup) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(server, that.server) &&
                Objects.equals(channels, that.channels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupName, server, channels);
    }
}
