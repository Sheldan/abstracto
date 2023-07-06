package dev.sheldan.abstracto.core.model.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "lock")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ALock implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
}
