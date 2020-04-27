package dev.sheldan.abstracto.core.command.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ACommandInAServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commandInServerId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "commandReference", nullable = false)
    private ACommand commandReference;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "serverReference", nullable = false)
    private AServer serverReference;

    @ManyToMany(fetch = FetchType.LAZY)
    @Getter
    @JoinColumn(name = "allowed_role_id")
    private List<ARole> allowedRoles;

    @ManyToMany(fetch = FetchType.LAZY)
    @Getter
    @JoinColumn(name = "immune_role_id")
    private List<ARole> immuneRoles;

    @Getter
    @Setter
    @Column
    private Boolean restricted;


}


