package dev.sheldan.abstracto.service;

import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public interface Startup {
    public void startBot() throws LoginException;
}
