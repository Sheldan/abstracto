package dev.sheldan.abstracto.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

@Service
public class BotService implements Bot {

    private JDA instance;

    @Override
    public void login() throws LoginException {
        JDABuilder builder = new JDABuilder(System.getenv("TOKEN"));

        builder.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE));
        builder.setBulkDeleteSplittingEnabled(false);

        this.instance = builder.build();
    }

    @Override
    public JDA getInstance() {
        return instance;
    }

    @Override
    public void shutdown() {

    }
}
