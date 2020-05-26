package dev.sheldan.abstracto.scheduling.model;


import lombok.*;

/**
 * The properties which are available to be configured via a property file
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerJobProperties {
    /**
     * The name of the job. Necessary to identify the job.
     */
    private String name;
    /**
     * The group in which the job should reside. Necessary to identify the job.
     */
    private String group;
    /**
     * If the job executes on a cron schedule, this should contain the cron expression for this. If it is a one-time job, this needs to be null.
     */
    private String cronExpression;
    /**
     * The absolute class name of the job bean extending {@link org.springframework.scheduling.quartz.QuartzJobBean} which should be executed
     */
    private String clazz;
    /**
     * Whether or not the job is active, and should be scheduled.
     */
    private Boolean active;
    /**
     * Whether or not the job should be re-tried in an recovery of fail over situation.
     */
    private Boolean recovery;
}