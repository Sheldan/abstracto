package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;


@Component
@Slf4j
public class CacheServiceBean {

    private SessionFactory sessionFactory;

    @Autowired
    public CacheServiceBean(EntityManagerFactory factory) {
        SessionFactory unWrapped = factory.unwrap(SessionFactory.class);
        if(unWrapped == null){
            throw new AbstractoRunTimeException("Factory is not a hibernate factory.");
        }
        this.sessionFactory = unWrapped;
    }

    public void clearCaches() {
        sessionFactory.getCache().evictAllRegions();
    }
}
