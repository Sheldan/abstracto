package dev.sheldan.abstracto.core.templating.repository;

import dev.sheldan.abstracto.core.templating.model.EffectiveTemplate;
import dev.sheldan.abstracto.core.templating.model.database.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository used to load the templates from the database.
 */
@Repository
public interface EffectiveTemplateRepository extends JpaRepository<Template, String> {
    @Query(value = "SELECT \n" +
            "t.key AS key, \n" +
            "COALESCE(c_t.content, t.content) AS content,\n" +
            "COALESCE(c_t.last_modified, t.last_modified) AS lastModified\n" +
            "FROM template t\n" +
            "LEFT OUTER JOIN custom_template c_t\n" +
            "ON t.key = c_t.key and c_t.server_id = :serverId\n" +
            "WHERE t.key = :key", nativeQuery = true)
    Optional<EffectiveTemplate> findByKeyAndServerId(@Param("key") String key, @Param("serverId") Long serverId);

    @Query(value = "SELECT \n" +
            "t.key AS key, \n" +
            "t.content AS content,\n" +
            "t.last_modified AS lastModified\n" +
            "FROM template t\n" +
            "WHERE t.key = :key", nativeQuery = true)
    Optional<EffectiveTemplate> findByKey(@Param("key") String key);

}
