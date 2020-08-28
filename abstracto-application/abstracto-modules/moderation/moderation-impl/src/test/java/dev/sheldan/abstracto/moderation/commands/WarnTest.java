package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnLog;
import dev.sheldan.abstracto.moderation.service.WarnService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WarnTest {

    @InjectMocks
    private Warn testUnit;

    @Mock
    private WarnService warnService;

    @Mock
    private TemplateService templateService;
    private static final String DEFAULT_REASON = "defaultReason";

    @Captor
    private ArgumentCaptor<WarnLog> parameterCaptor;

    @Test
    public void testExecuteWarnCommandWithReason() {
        Member warnedMember = Mockito.mock(Member.class);
        String reason = "reason";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(warnedMember, reason));
        when(templateService.renderSimpleTemplate(Warn.WARN_DEFAULT_REASON_TEMPLATE)).thenReturn(DEFAULT_REASON);
        CommandResult result = testUnit.execute(parameters);
        verify(warnService, times(1)).warnUserWithLog(eq(warnedMember), eq(parameters.getAuthor()), eq(reason), parameterCaptor.capture(), eq(parameters.getChannel()));
        WarnLog value = parameterCaptor.getValue();
        Assert.assertEquals(reason, value.getReason());
        Assert.assertEquals(warnedMember, value.getWarnedUser());
        Assert.assertEquals(parameters.getAuthor(), value.getWarningUser());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void testExecuteWarnCommandWithDefaultReason() {
        Member warnedMember = Mockito.mock(Member.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(warnedMember));
        when(templateService.renderSimpleTemplate(Warn.WARN_DEFAULT_REASON_TEMPLATE)).thenReturn(DEFAULT_REASON);
        CommandResult result = testUnit.execute(parameters);
        verify(warnService, times(1)).warnUserWithLog(eq(warnedMember), eq(parameters.getAuthor()), eq(DEFAULT_REASON), parameterCaptor.capture(), eq(parameters.getChannel()));
        WarnLog value = parameterCaptor.getValue();
        Assert.assertEquals(DEFAULT_REASON, value.getReason());
        Assert.assertEquals(warnedMember, value.getWarnedUser());
        Assert.assertEquals(parameters.getAuthor(), value.getWarningUser());
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
