package dev.sheldan.abstracto.core.models;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "server")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AServer implements SnowFlake {

    @Id
    @Getter @Setter
    private Long id;

    @Getter
    private String name;

    @OneToMany(fetch = FetchType.LAZY)
    @Getter
    @Builder.Default
    private List<ARole> roles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @Getter
    @Builder.Default
    private List<AChannel> channels = new ArrayList<>();



}
