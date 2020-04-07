package dev.sheldan.abstracto.scheduling.model.database;


import lombok.*;

import javax.persistence.*;

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
}