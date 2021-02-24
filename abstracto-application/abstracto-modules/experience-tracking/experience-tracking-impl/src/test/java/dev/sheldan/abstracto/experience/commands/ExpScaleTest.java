package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeatureConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExpScaleTest {

    @InjectMocks
    private ExpScale testUnit;

    @Mock
    private ConfigService configService;

    @Test
    public void testSetExpScaleForGuild() {
        double newScale = 4.5;
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(newScale));
        CommandResult result = testUnit.execute(context);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(configService, times(1)).setDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, context.getGuild().getIdLong(), newScale);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
