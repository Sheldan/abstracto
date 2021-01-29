package dev.sheldan.abstracto.core.model.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "lock")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ALock implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
}
