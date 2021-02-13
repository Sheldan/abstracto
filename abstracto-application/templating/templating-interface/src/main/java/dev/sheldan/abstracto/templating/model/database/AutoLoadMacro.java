package dev.sheldan.abstracto.templating.model.database;

import lombok.*;

import javax.persistence.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auto_load_macro")
@Getter
@EqualsAndHashCode
public class AutoLoadMacro {

    @Id
    private String key;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private Template template;
}
