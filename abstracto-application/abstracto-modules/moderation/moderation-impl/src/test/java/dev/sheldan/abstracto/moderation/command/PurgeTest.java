package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.service.PurgeService;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PurgeTest {

    @InjectMocks
    private Purge testUnit;

    @Mock
    private PurgeService purgeService;

    @Test
    public void testExecutePurgeOfNoMemberCommand() {
        Integer count = 10;
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(count));
        when(purgeService.purgeMessagesInChannel(count, parameters.getChannel(), parameters.getMessage(), null)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        Assert.assertEquals(ResultState.SELF_DESTRUCT, result.join().getResult());
    }

    @Test
    public void testExecutePurgeOfMemberCommand() {
        Member messageAuthor = Mockito.mock(Member.class);
        Integer count = 10;
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(count, messageAuthor));
        when(messageAuthor.getGuild()).thenReturn(parameters.getGuild());
        when(purgeService.purgeMessagesInChannel(count, parameters.getChannel(), parameters.getMessage(), messageAuthor)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        Assert.assertEquals(ResultState.SELF_DESTRUCT, result.join().getResult());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
