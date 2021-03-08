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
    @Column(name = "id")
    private Long id;

    /**
     * The name of the job
     */
    @Column(name = "name")
    private String name;

    /**
     * The group of the job
     */
    @Column(name = "group_name")
    private String groupName;

    /**
     * The absolute path of a class extending {@link org.springframework.scheduling.quartz.QuartzJobBean} which should be executed by this job
     */
    @Column(name = "clazz")
    private String clazz;

    /**
     * If the job should be executed based on a cron expression, this contains this expression. If it is a one-time job this needs to be null.
     */
    @Column(name = "cron_expression")
    private String cronExpression;

    /**
     * Whether or not the job is active and available to be scheduled.
     */
    @Column(name = "active")
    private boolean active;

    /**
     * Whether or not the job should be re-tried in an recovery of fail over situation.
     */
    @Column(name = "recovery")
    private boolean recovery;

}