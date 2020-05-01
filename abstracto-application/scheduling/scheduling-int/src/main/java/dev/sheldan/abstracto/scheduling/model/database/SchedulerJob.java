package dev.sheldan.abstracto.scheduling.model.database;


import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@Table(name = "scheduler_job")
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String groupName;

    private String clazz;

    private String cronExpression;

    private boolean active;

    private boolean recovery;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulerJob that = (SchedulerJob) o;
        return active == that.active &&
                recovery == that.recovery &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(clazz, that.clazz) &&
                Objects.equals(cronExpression, that.cronExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, groupName, clazz, cronExpression, active, recovery);
    }
}