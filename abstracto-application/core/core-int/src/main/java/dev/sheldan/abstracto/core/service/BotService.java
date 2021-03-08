package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public interface BotService {
    void login() throws LoginException;
    JDA getInstance();

}
