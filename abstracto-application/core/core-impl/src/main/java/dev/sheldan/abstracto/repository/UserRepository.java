package dev.sheldan.abstracto.repository;

import dev.sheldan.abstracto.core.models.database.AUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AUser, Long> {
}
