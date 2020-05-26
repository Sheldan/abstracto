package dev.sheldan.abstracto.scheduling.factory;

import dev.sheldan.abstracto.scheduling.model.SchedulerJobProperties;
import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import org.springframework.stereotype.Component;

@Component
public class SchedulerJobConverter {

    /**
     * Converts a {@link SchedulerJobProperties} instance to a usable {@link SchedulerJob} instance
     * @param properties The instance directly coming from a property file
     * @return A instanc eof {@link SchedulerJob} which represents an instance from the database
     */
    public SchedulerJob fromJobProperties(SchedulerJobProperties properties) {
        return SchedulerJob
                .builder()
                .name(properties.getName())
                .groupName(properties.getGroup())
                .active(properties.getActive())
                .cronExpression(properties.getCronExpression())
                .clazz(properties.getClazz())
                .recovery(properties.getRecovery())
                .build();
    }
}
