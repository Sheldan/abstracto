package dev.sheldan.abstracto.service;

import dev.sheldan.abstracto.commands.management.CommandReceivedHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.List;

@Service
public class StartupManager implements Startup {

    @Autowired
    private BotService service;

    @Autowired
    private List<? extends  ListenerAdapter> listeners;

    @Override
    public void startBot() throws LoginException {
        service.login();
        listeners.forEach(o -> service.getInstance().addEventListener(o));
    }
}
