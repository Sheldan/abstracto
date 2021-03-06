package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.model.template.command.KickLogModel;
import dev.sheldan.abstracto.moderation.service.KickServiceBean;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
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
public class KickTest {

    @InjectMocks
    private Kick testUnit;

    @Mock
    private TemplateService templateService;

    @Mock
    private KickServiceBean kickService;

    @Mock
    private Member memberToKick;

    @Captor
    private ArgumentCaptor<KickLogModel> logModelArgumentCaptor;

    private static final String REASON = "reason";
    private static final Long SERVER_ID = 1L;

    @Test
    public void testKickMemberWithoutReason() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(memberToKick));
        when(memberToKick.getGuild()).thenReturn(parameters.getGuild());
        when(parameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(templateService.renderSimpleTemplate(Kick.KICK_DEFAULT_REASON_TEMPLATE, SERVER_ID)).thenReturn(REASON);
        when(kickService.kickMember(eq(memberToKick), eq(REASON), logModelArgumentCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        KickLogModel usedLogModel = logModelArgumentCaptor.getValue();
        Assert.assertEquals(REASON, usedLogModel.getReason());
        Assert.assertEquals(memberToKick, usedLogModel.getKickedUser());
        Assert.assertEquals(parameters.getAuthor(), usedLogModel.getMember());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testKickMemberWithReason() {
        String customReason = "reason2";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(memberToKick, customReason));
        when(memberToKick.getGuild()).thenReturn(parameters.getGuild());
        when(kickService.kickMember(eq(memberToKick), eq(customReason), logModelArgumentCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        KickLogModel usedLogModel = logModelArgumentCaptor.getValue();
        Assert.assertEquals(customReason, usedLogModel.getReason());
        Assert.assertEquals(memberToKick, usedLogModel.getKickedUser());
        Assert.assertEquals(parameters.getAuthor(), usedLogModel.getMember());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
