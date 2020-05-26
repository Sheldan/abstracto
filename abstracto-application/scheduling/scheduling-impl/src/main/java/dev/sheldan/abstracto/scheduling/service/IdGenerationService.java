package dev.sheldan.abstracto.scheduling.service;

import org.quartz.SchedulerException;
import org.quartz.spi.InstanceIdGenerator;

import java.util.UUID;

/**
 * Generates the ID used for triggers etc. The implementation uses a {@link UUID} which is then stored and identifies the instance
 */
public class IdGenerationService implements InstanceIdGenerator {

    @Override
    public String generateInstanceId() throws SchedulerException {
        return UUID.randomUUID().toString();
    }

}