package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "server")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AServer implements SnowFlake {

    @Id
    private Long id;

    private String name;

    @OneToMany(fetch = FetchType.LAZY)
    @Builder.Default
    private List<ARole> roles = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "server",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<AChannel> channels = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "serverReference",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<AUserInAServer> users = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "serverRef",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<AEmote> emotes = new ArrayList<>();



}
