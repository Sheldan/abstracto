package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.moderation.service.PurgeService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
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

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PurgeTest {

    @InjectMocks
    private Purge testUnit;

    @Mock
    private PurgeService purgeService;

    @Mock
    private TemplateService templateService;

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
        when(purgeService.purgeMessagesInChannel(count, parameters.getChannel(), parameters.getMessage(), messageAuthor)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        Assert.assertEquals(ResultState.SELF_DESTRUCT, result.join().getResult());
    }

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTestAsync(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTestAsync(testUnit);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
