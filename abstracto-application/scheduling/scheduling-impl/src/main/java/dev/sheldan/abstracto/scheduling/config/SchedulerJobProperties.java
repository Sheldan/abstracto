package dev.sheldan.abstracto.scheduling.config;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerJobProperties {
    private String name;
    private String group;
    private String cronExpression;
    private String clazz;
    private Boolean active;
}