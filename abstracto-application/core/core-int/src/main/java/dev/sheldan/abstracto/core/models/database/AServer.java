package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "server")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class AServer implements SnowFlake, Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

    @Setter
    @Column(name = "admin_mode")
    private Boolean adminMode;

    @Transient
    private boolean fake;

    @OneToOne(mappedBy = "server", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AllowedMention allowedMention;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "server")
    @Builder.Default
    private List<ARole> roles = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "server")
    @Builder.Default
    private List<AChannel> channels = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "server")
    @Builder.Default
    private List<AChannelGroup> channelGroups = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "serverReference")
    @Builder.Default
    private List<AUserInAServer> users = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "serverRef")
    @Builder.Default
    private List<AEmote> emotes = new ArrayList<>();


}
