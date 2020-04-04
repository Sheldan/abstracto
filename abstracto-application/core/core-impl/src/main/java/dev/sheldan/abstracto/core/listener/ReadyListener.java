package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.service.StartupManager;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class ReadyListener extends ListenerAdapter {

    @Autowired
    private StartupManager startup;

    @Value("${abstracto.startup.synchronize}")
    private boolean synchronize;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        if(synchronize){
            startup.synchronize();
        }
    }
}
