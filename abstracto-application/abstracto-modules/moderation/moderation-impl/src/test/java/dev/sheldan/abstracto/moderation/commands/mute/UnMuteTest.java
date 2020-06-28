package dev.sheldan.abstracto.moderation.commands.mute;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.service.MuteService;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnMuteTest {

    @InjectMocks
    private UnMute testUnit;

    @Mock
    private MuteService muteService;

    @Mock
    private MuteManagementService muteManagementService;

    @Mock
    private TemplateService templateService;

    @Mock
    private Member memberToUnMute;

    @Test
    public void testUnMuteCommand() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(memberToUnMute));
        when(muteManagementService.hasActiveMute(memberToUnMute)).thenReturn(true);
        Mute mute = Mute.builder().build();
        when(muteManagementService.getAMuteOf(memberToUnMute)).thenReturn(mute);
        CommandResult result = testUnit.execute(parameters);
        verify(muteService, times(1)).unMuteUser(mute);
        verify(muteService, times(1)).cancelUnMuteJob(mute);
        verify(muteService, times(1)).completelyUnMuteMember(memberToUnMute);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void testUnMuteCommandWithoutExistingMute() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(memberToUnMute));
        when(muteManagementService.hasActiveMute(memberToUnMute)).thenReturn(false);
        String message = "text";
        when(templateService.renderSimpleTemplate(UnMute.NO_ACTIVE_MUTE)).thenReturn(message);
        CommandResult result = testUnit.execute(parameters);
        Assert.assertEquals(ResultState.ERROR, result.getResult());
        Assert.assertEquals(message, result.getMessage());
    }

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
