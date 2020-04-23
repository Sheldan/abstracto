package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AUser {

    @Id
    private Long id;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<AUserInAServer> servers;
}
