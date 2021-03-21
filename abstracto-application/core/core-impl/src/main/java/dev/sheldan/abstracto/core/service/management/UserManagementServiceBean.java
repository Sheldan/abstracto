package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.models.TableLocks;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.repository.UserRepository;
import dev.sheldan.abstracto.core.service.LockService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Slf4j
public class UserManagementServiceBean implements UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserManagementServiceBean self;

    @Autowired
    private LockService lockService;

    @Override
    public AUser createUser(Member member) {
        return createUser(member.getIdLong());
    }

    @Override
    public AUser createUser(Long userId) {
        AUser user;
        try {
            user = self.tryCreateUser(userId);
        } catch (DataIntegrityViolationException ex) {
            log.info("Concurrency exception creating user - retrieving.");
            user = loadUser(userId);
        }
        return user;
    }

    @Override
    public AUser loadUser(Long userId) {
        return userRepository.getOne(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AUser tryCreateUser(Long userId) {
        lockService.lockTable(TableLocks.USER);
        AUser aUser = AUser.builder().id(userId).build();
        return userRepository.save(aUser);
    }

    @Override
    public AUser loadOrCreateUser(Long userId) {
        Optional<AUser> optional = loadUserOptional(userId);
        return optional.orElseGet(() -> this.createUser(userId));
    }

    @Override
    public Optional<AUser> loadUserOptional(Long userId) {
        return userRepository.findById(userId);
    }
}
