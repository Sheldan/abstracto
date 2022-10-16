package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.DefaultEmote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultEmoteRepository extends JpaRepository<DefaultEmote, Long>  {
    DefaultEmote getByEmoteKey(String emoteKey);

    @Override
    List<DefaultEmote> findAll();
}
