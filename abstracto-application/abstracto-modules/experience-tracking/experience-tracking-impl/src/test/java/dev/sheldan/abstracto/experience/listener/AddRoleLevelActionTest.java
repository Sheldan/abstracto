package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddRoleLevelActionTest {
    @InjectMocks
    private AddRoleLevelAction action;

    @Mock
    private AUserExperience exp;

    @Mock
    private LevelAction levelAction;

    @Mock
    private AExperienceLevel level;

    private final Integer LOW_LEVEL = 1;
    private final Integer MID_LEVEL = 2;
    private final Integer HIGH_LEVEL = 3;

    @Before
    public void setup() {
        when(levelAction.getLevel()).thenReturn(level);
    }

    @Test // rejoin too low case
    public void noLevelChangeActionNotReached() {
        executeTest(LOW_LEVEL, LOW_LEVEL, HIGH_LEVEL, false);
    }

    @Test // re-join exact case
    public void noLevelChangeActionReached() {
        executeTest(LOW_LEVEL, LOW_LEVEL, LOW_LEVEL, true);
    }

    @Test // normal leveling, higher action
    public void levelChangeActionNotReached() {
        executeTest(LOW_LEVEL, MID_LEVEL, HIGH_LEVEL, false);
    }

    @Test // normal leveling
    public void levelChangeActionReached() {
        executeTest(LOW_LEVEL, MID_LEVEL, MID_LEVEL, true);
    }

    @Test // a case for this is a large experience jump
    public void levelChangeActionOverJumped() {
        executeTest(LOW_LEVEL, HIGH_LEVEL, MID_LEVEL, true);
    }

    @Test // a case for this is a re-join
    public void noLevelChangeActionOverJumped() {
        executeTest(HIGH_LEVEL, HIGH_LEVEL, LOW_LEVEL, true);
    }

    @Test // we dont want to re-execute previous actions (previous = lower level)
    public void levelChangeActionEqualsPrevious() {
        executeTest(LOW_LEVEL, MID_LEVEL, LOW_LEVEL, true);
    }

    @Test // we dont want to re-execute previous actions (previous = way lower level)
    public void levelChangeActionBelow() {
        executeTest(MID_LEVEL, HIGH_LEVEL, LOW_LEVEL, true);
    }

    private void executeTest(Integer oldLevel, Integer currentLevel, Integer actionLevel, boolean expected) {
        when(exp.getLevelOrDefault()).thenReturn(currentLevel);
        when(level.getLevel()).thenReturn(actionLevel);
        Assertions.assertThat(action.shouldExecute(exp, oldLevel, levelAction)).isEqualTo(expected);
    }
}
