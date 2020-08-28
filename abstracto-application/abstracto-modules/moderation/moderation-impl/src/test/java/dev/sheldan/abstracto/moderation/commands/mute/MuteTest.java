package dev.sheldan.abstracto.moderation.commands.mute;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteLog;
import dev.sheldan.abstracto.moderation.service.MuteService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MuteTest {

    @InjectMocks
    private Mute testUnit;

    @Mock
    private MuteService muteService;

    @Captor
    private ArgumentCaptor<MuteLog> muteLogArgumentCaptor;

    @Test
    public void testMuteMember() {
        Member mutedMember = Mockito.mock(Member.class);
        String reason = "reason";
        Duration duration = Duration.ofMinutes(1);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(mutedMember, duration, reason));
        CommandResult result = testUnit.execute(parameters);
        verify(muteService, times(1)).muteMemberWithLog(eq(mutedMember), eq(parameters.getAuthor()), eq(reason), any(Instant.class), muteLogArgumentCaptor.capture(), eq(parameters.getMessage()));
        CommandTestUtilities.checkSuccessfulCompletion(result);
        MuteLog muteLog = muteLogArgumentCaptor.getValue();
        Assert.assertEquals(mutedMember, muteLog.getMutedUser());
        Assert.assertEquals(parameters.getAuthor(), muteLog.getMutingUser());
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
