package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="role")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ARole implements SnowFlake {

    @Id
    @Getter @Setter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "role_server_id")
    private AServer server;

    @Getter
    @Setter
    private Boolean deleted;

}