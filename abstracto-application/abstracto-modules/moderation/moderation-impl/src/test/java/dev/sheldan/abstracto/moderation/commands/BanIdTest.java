package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.models.template.commands.BanIdLog;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BanIdTest {

    @InjectMocks
    private BanId testUnit;

    @Mock
    private BanService banService;

    @Mock
    private TemplateService templateService;

    @Captor
    private ArgumentCaptor<BanIdLog> banLogModelCaptor;

    private static final String REASON = "reason";
    private static final Long BANNED_USER_ID = 4L;
    private static final Long SERVER_ID = 3L;

    @Test
    public void testBanIdWithDefaultReason() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(BANNED_USER_ID));
        when(parameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE, SERVER_ID)).thenReturn(REASON);
        when(banService.banUserViaId(eq(SERVER_ID), eq(BANNED_USER_ID), eq(REASON), banLogModelCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        BanIdLog usedModel = banLogModelCaptor.getValue();
        Assert.assertEquals(REASON, usedModel.getReason());
        Assert.assertEquals(BANNED_USER_ID, usedModel.getBannedUserId());
        Assert.assertEquals(parameters.getAuthor(), usedModel.getBanningUser());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testBanWithReason() {
        String customReason = "reason2";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(BANNED_USER_ID, customReason));
        when(parameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE, SERVER_ID)).thenReturn(REASON);
        when(banService.banUserViaId(eq(SERVER_ID), eq(BANNED_USER_ID), eq(customReason), banLogModelCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        BanIdLog usedModel = banLogModelCaptor.getValue();
        Assert.assertEquals(customReason, usedModel.getReason());
        Assert.assertEquals(BANNED_USER_ID, usedModel.getBannedUserId());
        Assert.assertEquals(parameters.getAuthor(), usedModel.getBanningUser());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
