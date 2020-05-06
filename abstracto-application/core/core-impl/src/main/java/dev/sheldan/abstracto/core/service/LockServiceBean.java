package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.models.TableLocks;
import dev.sheldan.abstracto.core.repository.LockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LockServiceBean implements LockService {

    @Autowired
    private LockRepository lockRepository;

    @Override
    public void lockTable(TableLocks toLock) {
        int ordinal = toLock.ordinal();
        lockRepository.findArticleForRead((long) ordinal);
    }
}
