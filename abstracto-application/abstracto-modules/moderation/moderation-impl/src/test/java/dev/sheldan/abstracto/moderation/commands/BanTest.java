package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.models.template.commands.BanLog;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
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
public class BanTest {

    @InjectMocks
    private Ban testUnit;

    @Mock
    private BanService banService;

    @Mock
    private TemplateService templateService;

    @Captor
    private ArgumentCaptor<BanLog> banLogModelCaptor;

    private static final String REASON = "reason";

    @Mock
    private Member bannedMember;

    @Test
    public void testBanWithDefaultReason() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(bannedMember));
        when(templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE)).thenReturn(REASON);
        when(banService.banMember(eq(bannedMember), eq(REASON), banLogModelCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        BanLog usedModel = banLogModelCaptor.getValue();
        Assert.assertEquals(REASON, usedModel.getReason());
        Assert.assertEquals(bannedMember, usedModel.getBannedUser());
        Assert.assertEquals(parameters.getAuthor(), usedModel.getBanningUser());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testBanWithReason() {
        String customReason = "reason2";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(bannedMember, customReason));
        when(templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE)).thenReturn(REASON);
        when(banService.banMember(eq(bannedMember), eq(customReason), banLogModelCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        BanLog usedModel = banLogModelCaptor.getValue();
        Assert.assertEquals(customReason, usedModel.getReason());
        Assert.assertEquals(bannedMember, usedModel.getBannedUser());
        Assert.assertEquals(parameters.getAuthor(), usedModel.getBanningUser());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
