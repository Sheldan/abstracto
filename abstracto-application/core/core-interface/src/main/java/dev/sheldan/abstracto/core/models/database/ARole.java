package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="role")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ARole implements SnowFlake {

    @Id
    @Getter @Setter
    private Long id;

    @Column(unique = true)
    @Getter @Setter
    private String name;

}
