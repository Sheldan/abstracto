package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.moderation.service.SlowModeService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SlowModeTest {

    @InjectMocks
    private SlowMode testUnit;

    @Mock
    private SlowModeService slowModeService;

    @Test
    public void testExecuteSlowModeWithDurationCurrentChannel() {
        String duration = "1m";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration));
        CommandResult result = testUnit.execute(parameters);
        verify(slowModeService, times(1)).setSlowMode(parameters.getChannel(), Duration.ofMinutes(1));
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void testDisableSlowModeCurrentChannel() {
        String duration = "off";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration));
        CommandResult result = testUnit.execute(parameters);
        verify(slowModeService, times(1)).setSlowMode(parameters.getChannel(), Duration.ZERO);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void testDisableSlowModeOtherChannel() {
        String duration = "off";
        TextChannel channel = Mockito.mock(TextChannel.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration, channel));
        CommandResult result = testUnit.execute(parameters);
        verify(slowModeService, times(1)).setSlowMode(channel, Duration.ZERO);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void testExecuteSlowModeWithDurationOtherChannel() {
        String duration = "1m";
        TextChannel channel = Mockito.mock(TextChannel.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration, channel));
        CommandResult result = testUnit.execute(parameters);
        verify(slowModeService, times(1)).setSlowMode(channel, Duration.ofMinutes(1));
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameterException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
