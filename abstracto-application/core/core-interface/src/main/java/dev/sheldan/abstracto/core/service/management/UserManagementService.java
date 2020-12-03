package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AUser;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;

public interface UserManagementService {
    AUser createUser(Member member);
    AUser createUser(Long userId);
    AUser loadUser(Long userId);
    Optional<AUser> loadUserOptional(Long userId);
}
