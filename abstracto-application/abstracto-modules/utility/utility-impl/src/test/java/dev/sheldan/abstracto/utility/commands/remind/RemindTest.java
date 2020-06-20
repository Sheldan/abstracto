package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.ReminderModel;
import dev.sheldan.abstracto.utility.service.ReminderService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemindTest {

    @InjectMocks
    private Remind testUnit;

    @Mock
    private ReminderService remindService;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<ReminderModel> captor;

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test(expected = InsufficientParameters.class)
    public void testOnlyRemindDateParameter() {
        CommandContext durationParameter = CommandTestUtilities.getWithParameters(Arrays.asList(Duration.ofDays(4)));
        testUnit.execute(durationParameter);
    }

    @Test
    public void executeCommand() {
        String reminderText = "text";
        Duration duration = Duration.ofMinutes(10);
        CommandContext withParameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration, reminderText));
        CommandResult result = testUnit.execute(withParameters);
        verify(remindService, times(1)).createReminderInForUser(withParameters.getUserInitiatedContext().getAUserInAServer(), reminderText, duration, withParameters.getMessage());
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(Remind.REMINDER_EMBED_KEY), captor.capture(), eq(withParameters.getChannel()));
        ReminderModel reminderModel = captor.getValue();
        Assert.assertEquals(reminderText, reminderModel.getRemindText());
        Assert.assertEquals(withParameters.getMessage(), reminderModel.getMessage());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
