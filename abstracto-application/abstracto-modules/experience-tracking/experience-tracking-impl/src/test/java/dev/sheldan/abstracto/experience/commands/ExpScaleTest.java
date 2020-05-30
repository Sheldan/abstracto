package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.internal.JDAImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExpScaleTest {

    @InjectMocks
    private ExpScale testUnit;

    @Mock
    private ConfigService configService;

    @Mock
    private JDAImpl jda;

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit, jda);
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit, jda);
    }

    @Test
    public void testSetExpScaleForGuild() {
        double newScale = 4.5;
        CommandContext context = CommandTestUtilities.getWithParameters(jda, Arrays.asList(newScale));
        CommandResult result = testUnit.execute(context);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(configService, times(1)).setDoubleValue(ExpScale.EXP_MULTIPLIER_KEY, context.getGuild().getIdLong(), newScale);

    }
}
