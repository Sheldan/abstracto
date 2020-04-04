package dev.sheldan.abstracto.core.command.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "module")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AModule {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    @JoinColumn(name = "module_id")
    private List<ACommand> commands = new ArrayList<>();
}
