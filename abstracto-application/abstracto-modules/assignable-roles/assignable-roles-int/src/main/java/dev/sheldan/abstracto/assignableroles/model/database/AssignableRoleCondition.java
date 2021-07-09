package dev.sheldan.abstracto.assignableroles.model.database;

import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "assignable_role_condition")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AssignableRoleCondition {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AssignableRoleConditionType type;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "assignable_role_id", nullable = false)
    private AssignableRole assignableRole;

    @Column(name = "condition_value")
    private String conditionValue;
}
