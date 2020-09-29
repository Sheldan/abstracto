package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class UserManagementServiceBean implements UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public AUser createUser(Member member) {
        return createUser(member.getIdLong());
    }

    @Override
    public AUser createUser(Long userId) {
        log.info("Creating user {}", userId);
        AUser aUser = AUser.builder().id(userId).build();
        userRepository.save(aUser);
        return aUser;
    }

    @Override
    public AUser loadUser(Long userId) {
        Optional<AUser> optional = userRepository.findById(userId);
        return optional.orElseGet(() -> this.createUser(userId));
    }
}
