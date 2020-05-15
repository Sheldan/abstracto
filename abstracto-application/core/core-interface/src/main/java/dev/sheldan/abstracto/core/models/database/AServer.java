package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
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

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            mappedBy = "server")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<ARole> roles = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "server")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AChannel> channels = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "server")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AChannelGroup> channelGroups = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "serverReference")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AUserInAServer> users = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "serverRef")
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
