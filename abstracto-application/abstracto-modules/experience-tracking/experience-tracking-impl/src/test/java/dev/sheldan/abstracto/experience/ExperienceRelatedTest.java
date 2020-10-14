package dev.sheldan.abstracto.experience;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.core.test.MockUtils;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;


public abstract class ExperienceRelatedTest {

    @Mock
    private AServer firstServer;

    @Mock
    private AServer secondServer;

    protected List<AUserExperience> getUserExperiences(int count, AServer server) {
        List<AUserExperience> experiences = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            experiences.add(getUserExperienceObject(server, i));
        }
        return experiences;
    }

    protected AUserExperience getUserExperienceObject(AServer server, long i) {
        AUserInAServer userObject = MockUtils.getUserObject(i, server);
        AExperienceLevel level = AExperienceLevel
                .builder()
                .level((int)i)
                .experienceNeeded(i * 100)
                .build();
        return AUserExperience
                .builder()
                .user(userObject)
                .experience(i)
                .currentLevel(level)
                .build();
    }

    protected List<AExperienceLevel> getLevelConfiguration() {
        AExperienceLevel level0 = AExperienceLevel.builder().experienceNeeded(0L).level(0).build();
        AExperienceLevel level1 = AExperienceLevel.builder().experienceNeeded(100L).level(1).build();
        AExperienceLevel level2 = AExperienceLevel.builder().experienceNeeded(200L).level(2).build();
        AExperienceLevel level3 = AExperienceLevel.builder().experienceNeeded(300L).level(3).build();
        return new ArrayList<>(Arrays.asList(level0, level1, level2, level3));
    }

    protected List<AExperienceRole> getExperienceRoles(List<AExperienceLevel> levelsWithRoles) {
        List<AExperienceRole> roles = new ArrayList<>();
        for (int i = 0; i < levelsWithRoles.size(); i++) {
            AExperienceLevel level = levelsWithRoles.get(i);
            ARole role = Mockito.mock(ARole.class);
            when(role.getId()).thenReturn((long)i);
            AExperienceRole experienceRole = Mockito.mock(AExperienceRole.class);
            when(experienceRole.getLevel()).thenReturn(level);
            when(experienceRole.getId()).thenReturn((long) i);
            when(experienceRole.getRole()).thenReturn(role);
            roles.add(experienceRole);
        }
        return roles;
    }

}
