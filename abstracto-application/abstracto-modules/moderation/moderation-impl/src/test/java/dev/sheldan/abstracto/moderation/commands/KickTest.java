package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.moderation.models.template.commands.KickLogModel;
import dev.sheldan.abstracto.moderation.service.KickServiceBean;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
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

import static org.mockito.Mockito.*;

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

    @Test
    public void testKickMemberWithoutReason() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(memberToKick));
        when(templateService.renderSimpleTemplate(Kick.KICK_DEFAULT_REASON_TEMPLATE)).thenReturn(REASON);
        CommandResult result = testUnit.execute(parameters);
        verify(kickService, times(1)).kickMember(eq(memberToKick), eq(REASON), logModelArgumentCaptor.capture());
        KickLogModel usedLogModel = logModelArgumentCaptor.getValue();
        Assert.assertEquals(REASON, usedLogModel.getReason());
        Assert.assertEquals(memberToKick, usedLogModel.getKickedUser());
        Assert.assertEquals(parameters.getAuthor(), usedLogModel.getKickingUser());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void testKickMemberWithReason() {
        String customReason = "reason2";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(memberToKick, customReason));
        CommandResult result = testUnit.execute(parameters);
        verify(kickService, times(1)).kickMember(eq(memberToKick), eq(customReason), logModelArgumentCaptor.capture());
        KickLogModel usedLogModel = logModelArgumentCaptor.getValue();
        Assert.assertEquals(customReason, usedLogModel.getReason());
        Assert.assertEquals(memberToKick, usedLogModel.getKickedUser());
        Assert.assertEquals(parameters.getAuthor(), usedLogModel.getKickingUser());
        CommandTestUtilities.checkSuccessfulCompletion(result);
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
