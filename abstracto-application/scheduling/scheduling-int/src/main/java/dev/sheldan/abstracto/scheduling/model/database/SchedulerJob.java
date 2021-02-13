package dev.sheldan.abstracto.scheduling.model.database;


import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

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
@EqualsAndHashCode
public class SchedulerJob implements Serializable {

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

}