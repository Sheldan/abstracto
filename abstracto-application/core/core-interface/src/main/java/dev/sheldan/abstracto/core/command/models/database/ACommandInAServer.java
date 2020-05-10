package dev.sheldan.abstracto.core.command.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ACommandInAServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commandInServerId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "commandReference", nullable = false)
    private ACommand commandReference;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "serverReference", nullable = false)
    private AServer serverReference;

    @ManyToMany(fetch = FetchType.LAZY)
    @Getter
    @JoinColumn(name = "allowed_role_id")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<ARole> allowedRoles;

    @ManyToMany(fetch = FetchType.LAZY)
    @Getter
    @JoinColumn(name = "immune_role_id")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<ARole> immuneRoles;

    @Getter
    @Setter
    @Column
    private Boolean restricted;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ACommandInAServer that = (ACommandInAServer) o;
        return Objects.equals(commandInServerId, that.commandInServerId) &&
                Objects.equals(commandReference, that.commandReference) &&
                Objects.equals(serverReference, that.serverReference) &&
                Objects.equals(allowedRoles, that.allowedRoles) &&
                Objects.equals(immuneRoles, that.immuneRoles) &&
                Objects.equals(restricted, that.restricted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandInServerId, commandReference, serverReference, allowedRoles, immuneRoles, restricted);
    }
}


