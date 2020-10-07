package dev.sheldan.abstracto.moderation.job;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.moderation.config.features.WarningDecayFeature;
import dev.sheldan.abstracto.moderation.service.WarnService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
public class WarnDecayJob extends QuartzJobBean {

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private WarningDecayFeature warningDecayFeature;

    @Autowired
    private WarnService warnService;

    @Override
    @Transactional
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        List<AServer> allServers = serverManagementService.getAllServers();
        log.info("Executing warn decay job.");
        allServers.forEach(server -> {
            boolean featureEnabled = featureFlagService.isFeatureEnabled(warningDecayFeature, server);
            if(featureEnabled) {
                log.info("Executing warn decay for server {}.", server.getId());
                warnService.decayWarningsForServer(server);
            }
        });
    }
}
