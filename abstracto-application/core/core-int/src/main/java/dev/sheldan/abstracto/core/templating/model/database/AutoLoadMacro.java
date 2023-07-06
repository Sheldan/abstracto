package dev.sheldan.abstracto.core.templating.model.database;

import lombok.*;

import jakarta.persistence.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auto_load_macro")
@Getter
@EqualsAndHashCode
public class AutoLoadMacro {

    @Id
    @Column(name = "key", nullable = false)
    private String key;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private Template template;
}
