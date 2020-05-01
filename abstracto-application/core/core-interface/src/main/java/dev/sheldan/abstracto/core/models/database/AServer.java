package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "server")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AServer implements SnowFlake, Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    private String name;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    @JoinColumn(name = "role_server_id")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<ARole> roles = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    @Builder.Default
    @JoinColumn(name = "server_id")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AChannel> channels = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    @Builder.Default
    @JoinColumn(name = "group_server")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AChannelGroup> channelGroups = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    @JoinColumn(name = "serverReference")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AUserInAServer> users = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    @JoinColumn(name = "emote_server_id")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AEmote> emotes = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AServer aServer = (AServer) o;
        return Objects.equals(id, aServer.id) &&
                Objects.equals(name, aServer.name) &&
                Objects.equals(roles, aServer.roles) &&
                Objects.equals(channels, aServer.channels) &&
                Objects.equals(channelGroups, aServer.channelGroups) &&
                Objects.equals(users, aServer.users) &&
                Objects.equals(emotes, aServer.emotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, roles, channels, channelGroups, users, emotes);
    }
}
