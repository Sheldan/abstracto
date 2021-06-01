package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
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
    private ArgumentCaptor<Member> banLogModelCaptor;

    private static final String REASON = "reason";
    private static final Long SERVER_ID = 1L;

    @Mock
    private User bannedMember;

    @Test
    public void testBanWithDefaultReason() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(bannedMember));
        when(parameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE, SERVER_ID)).thenReturn(REASON);
        when(banService.banUser(eq(bannedMember), eq(REASON), banLogModelCaptor.capture(), any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        Member banningMember = banLogModelCaptor.getValue();
        Assert.assertEquals(parameters.getAuthor(), banningMember);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testBanWithReason() {
        String customReason = "reason2";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(bannedMember, customReason));
        when(parameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE, SERVER_ID)).thenReturn(REASON);
        when(banService.banUser(eq(bannedMember), eq(customReason), banLogModelCaptor.capture(), any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        Member banningMember = banLogModelCaptor.getValue();
        Assert.assertEquals(parameters.getAuthor(), banningMember);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
