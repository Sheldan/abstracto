package dev.sheldan.abstracto.core.job;

import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PaginatorServiceBean;
import dev.sheldan.abstracto.core.service.management.ComponentPayloadManagementService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
@Setter
public class PaginatorCleanupJob extends QuartzJobBean {

    private String paginatorId;
    private String accessorId;

    @Autowired
    private PaginatorServiceBean paginatorServiceBean;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        PaginatorServiceBean.PaginatorInfo info = paginatorServiceBean.getPaginatorInfo(paginatorId);
        log.info("Executing paginator cleanup for paginator {}", paginatorId);
        if(info != null && info.getLastAccessor().equals(accessorId)) {
            log.info("Last accessor was {} - which was the start of this job - deleting", info.getLastAccessor());
            paginatorServiceBean.cleanupPaginator(info);
        } else {
            log.info("The last accessor did either not start this job, or there was no configuration found.");
        }
    }
}
