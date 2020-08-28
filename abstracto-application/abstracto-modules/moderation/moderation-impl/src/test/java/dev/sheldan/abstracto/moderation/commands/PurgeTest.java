package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.utils.ExceptionUtils;
import dev.sheldan.abstracto.moderation.service.PurgeService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
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

    @Mock
    private ExceptionUtils exceptionUtils;

    @Test
    public void testExecutePurgeOfNoMemberCommand() {
        Integer count = 10;
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(count));
        when(purgeService.purgeMessagesInChannel(count, parameters.getChannel(), parameters.getMessage(), null)).thenReturn(CompletableFuture.completedFuture(null));
        CommandResult result = testUnit.execute(parameters);
        verify(exceptionUtils, times(1)).handleExceptionIfTemplatable(null, parameters.getChannel());
        Assert.assertEquals(ResultState.SELF_DESTRUCT, result.getResult());
    }

    @Test
    public void testExecutePurgeOfMemberCommand() {
        Member messageAuthor = Mockito.mock(Member.class);
        Integer count = 10;
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(count, messageAuthor));
        when(purgeService.purgeMessagesInChannel(count, parameters.getChannel(), parameters.getMessage(), messageAuthor)).thenReturn(CompletableFuture.completedFuture(null));
        CommandResult result = testUnit.execute(parameters);
        verify(exceptionUtils, times(1)).handleExceptionIfTemplatable(null, parameters.getChannel());
        Assert.assertEquals(ResultState.SELF_DESTRUCT, result.getResult());
    }

    @Test
    public void testExecutePurgeErroneousCommand() {
        Integer count = 10;
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(count));
        CompletableFuture<Void> failingFuture = new CompletableFuture<>();
        RuntimeException exception = new RuntimeException();
        failingFuture.completeExceptionally(exception);
        when(purgeService.purgeMessagesInChannel(count, parameters.getChannel(), parameters.getMessage(), null)).thenReturn(failingFuture);
        CommandResult result = testUnit.execute(parameters);
        verify(exceptionUtils, times(1)).handleExceptionIfTemplatable(eq(exception), eq(parameters.getChannel()));
        Assert.assertEquals(ResultState.SELF_DESTRUCT, result.getResult());
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
