package dev.sheldan.abstracto.modmail.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "modmail_roles")
@Cacheable
@Getter
@Setter
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ModMailRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modMailRoleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_role_server", nullable = false)
    private AServer server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_role", nullable = false)
    private ARole role;
}
