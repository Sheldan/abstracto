package dev.sheldan.abstracto.activity.repository;

import dev.sheldan.abstracto.activity.models.CustomActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<CustomActivity, Long> {
}
