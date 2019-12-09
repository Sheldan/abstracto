package dev.sheldan.abstracto.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Role {

    @Id
    @Getter
    private Long id;

    @Column(unique = true)
    @Getter @Setter
    private String name;

}
