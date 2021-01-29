package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnContext;
import dev.sheldan.abstracto.moderation.service.WarnService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

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
    private ArgumentCaptor<WarnContext> parameterCaptor;

    @Test
    public void testExecuteWarnCommandWithReason() {
        Member warnedMember = Mockito.mock(Member.class);
        String reason = "reason";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(warnedMember, reason));
        when(templateService.renderSimpleTemplate(Warn.WARN_DEFAULT_REASON_TEMPLATE)).thenReturn(DEFAULT_REASON);
        when(warnService.warnUserWithLog(parameterCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        WarnContext value = parameterCaptor.getValue();
        Assert.assertEquals(reason, value.getReason());
        Assert.assertEquals(warnedMember, value.getWarnedMember());
        Assert.assertEquals(parameters.getAuthor(), value.getMember());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testExecuteWarnCommandWithDefaultReason() {
        Member warnedMember = Mockito.mock(Member.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(warnedMember));
        when(templateService.renderSimpleTemplate(Warn.WARN_DEFAULT_REASON_TEMPLATE)).thenReturn(DEFAULT_REASON);
        when(warnService.warnUserWithLog(parameterCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        WarnContext value = parameterCaptor.getValue();
        Assert.assertEquals(DEFAULT_REASON, value.getReason());
        Assert.assertEquals(warnedMember, value.getWarnedMember());
        Assert.assertEquals(parameters.getAuthor(), value.getMember());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
