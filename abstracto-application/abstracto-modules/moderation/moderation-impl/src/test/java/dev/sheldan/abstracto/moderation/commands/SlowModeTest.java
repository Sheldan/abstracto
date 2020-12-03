package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.moderation.service.SlowModeService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

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
        when(slowModeService.setSlowMode(parameters.getChannel(), Duration.ofMinutes(1))).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testDisableSlowModeCurrentChannel() {
        String duration = "off";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration));
        when(slowModeService.setSlowMode(parameters.getChannel(), Duration.ZERO)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testDisableSlowModeOtherChannel() {
        String duration = "off";
        TextChannel channel = Mockito.mock(TextChannel.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration, channel));
        when(slowModeService.setSlowMode(channel, Duration.ZERO)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testExecuteSlowModeWithDurationOtherChannel() {
        String duration = "1m";
        TextChannel channel = Mockito.mock(TextChannel.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration, channel));
        when(slowModeService.setSlowMode(channel, Duration.ofMinutes(1))).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
