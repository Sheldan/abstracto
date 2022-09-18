package dev.sheldan.abstracto.moderation.command.mute;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.command.Mute;
import dev.sheldan.abstracto.moderation.model.template.command.MuteContext;
import dev.sheldan.abstracto.moderation.service.MuteService;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MuteTest {

    @InjectMocks
    private Mute testUnit;

    @Mock
    private MuteService muteService;

    @Mock
    private TemplateService templateService;

    @Captor
    private ArgumentCaptor<MuteContext> muteLogArgumentCaptor;

    @Test
    public void testMuteMember() {
        Member mutedMember = Mockito.mock(Member.class);
        String reason = "reason";
        Duration duration = Duration.ofMinutes(1);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(mutedMember, duration, reason));
        when(mutedMember.getGuild()).thenReturn(parameters.getGuild());
        when(muteService.muteMemberWithLog(muteLogArgumentCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        MuteContext muteLog = muteLogArgumentCaptor.getValue();
        Assert.assertEquals(mutedMember, muteLog.getMutedUser());
        Assert.assertEquals(parameters.getAuthor(), muteLog.getMutingUser());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
