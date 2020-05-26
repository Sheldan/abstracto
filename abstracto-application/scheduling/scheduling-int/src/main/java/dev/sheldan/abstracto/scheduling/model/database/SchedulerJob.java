package dev.sheldan.abstracto.scheduling.model.database;


import lombok.*;

import javax.persistence.*;
import java.util.Objects;

/**
 * The scheduler job instance according to the properties stored in the database. This is needed in order to have a
 * reference of the jobs which *can* be scheduled.
 */
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

    /**
     * The name of the job
     */
    private String name;

    /**
     * The group of the job
     */
    private String groupName;

    /**
     * The absolute path of a class extending {@link org.springframework.scheduling.quartz.QuartzJobBean} which should be executed by this job
     */
    private String clazz;

    /**
     * If the job should be executed based on a cron expression, this contains this expression. If it is a one-time job this needs to be null.
     */
    private String cronExpression;

    /**
     * Whether or not the job is active and available to be scheduled.
     */
    private boolean active;

    /**
     * Whether or not the job should be re-tried in an recovery of fail over situation.
     */
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