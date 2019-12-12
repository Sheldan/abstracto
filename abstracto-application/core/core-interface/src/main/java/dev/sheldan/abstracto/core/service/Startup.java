package dev.sheldan.abstracto.core.service;

import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public interface Startup {
    void startBot() throws LoginException;
    void synchronize();
}
