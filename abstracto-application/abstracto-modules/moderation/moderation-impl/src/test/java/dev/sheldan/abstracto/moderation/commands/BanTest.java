package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.moderation.models.template.commands.BanLog;
import dev.sheldan.abstracto.moderation.service.BanService;
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
        CommandResult result = testUnit.execute(parameters);
        verify(banService, times(1)).banMember(eq(bannedMember), eq(REASON), banLogModelCaptor.capture());
        BanLog usedModel = banLogModelCaptor.getValue();
        Assert.assertEquals(REASON, usedModel.getReason());
        Assert.assertEquals(bannedMember, usedModel.getBannedUser());
        Assert.assertEquals(parameters.getAuthor(), usedModel.getBanningUser());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void testBanWithReason() {
        String customReason = "reason2";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(bannedMember, customReason));
        when(templateService.renderSimpleTemplate(Ban.BAN_DEFAULT_REASON_TEMPLATE)).thenReturn(REASON);
        CommandResult result = testUnit.execute(parameters);
        verify(banService, times(1)).banMember(eq(bannedMember), eq(customReason), banLogModelCaptor.capture());
        BanLog usedModel = banLogModelCaptor.getValue();
        Assert.assertEquals(customReason, usedModel.getReason());
        Assert.assertEquals(bannedMember, usedModel.getBannedUser());
        Assert.assertEquals(parameters.getAuthor(), usedModel.getBanningUser());
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
