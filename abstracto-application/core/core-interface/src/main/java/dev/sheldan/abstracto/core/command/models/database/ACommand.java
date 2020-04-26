package dev.sheldan.abstracto.core.command.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "command")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ACommand {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "module_id", nullable = false)
    private AModule module;

    @ManyToMany(fetch = FetchType.LAZY)
    @Getter
    @JoinColumn(name = "allowed_role_id")
    private List<ARole> allowedRoles;


}
