package dev.sheldan.abstracto.core.command.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity(name = "command_in_server")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ACommandInAServer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "command_in_server_id")
    private Long commandInServerId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "command_id", nullable = false)
    private ACommand commandReference;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "server_id", nullable = false)
    private AServer serverReference;

    @ManyToMany
    @JoinTable(
            name = "command_in_server_allowed_role",
            joinColumns = @JoinColumn(name = "command_in_server_id", referencedColumnName = "command_in_server_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @Getter
    private List<ARole> allowedRoles;

    @ManyToMany
    @JoinTable(
            name = "command_in_server_immune_role",
            joinColumns = @JoinColumn(name = "command_in_server_id", referencedColumnName = "command_in_server_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @Getter
    private List<ARole> immuneRoles;

    @Getter
    @Setter
    @Column(name = "restricted")
    private Boolean restricted;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;


}


