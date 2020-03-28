package dev.sheldan.abstracto.scheduling.service;

import org.quartz.SchedulerException;
import org.quartz.spi.InstanceIdGenerator;

import java.util.UUID;

public class IdGenerationService implements InstanceIdGenerator {

    @Override
    public String generateInstanceId() throws SchedulerException {
        return UUID.randomUUID().toString();
    }

}