package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.StartupServiceBean;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class ReadyListener extends ListenerAdapter {

    @Autowired
    private StartupServiceBean startup;

    @Value("${abstracto.startup.synchronize}")
    private boolean synchronize;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private BotService botService;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        if(synchronize){
            startup.synchronize();
        }
        schedulerService.startScheduler();
    }
}
