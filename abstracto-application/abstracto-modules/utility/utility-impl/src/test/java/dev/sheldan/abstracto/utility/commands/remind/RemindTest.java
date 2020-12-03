package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
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
import java.util.concurrent.CompletableFuture;

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

    @Test
    public void executeCommand() {
        String reminderText = "text";
        Duration duration = Duration.ofMinutes(10);
        CommandContext withParameters = CommandTestUtilities.getWithParameters(Arrays.asList(duration, reminderText));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(withParameters);
        verify(remindService, times(1)).createReminderInForUser(withParameters.getUserInitiatedContext().getAUserInAServer(), reminderText, duration, withParameters.getMessage());
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(Remind.REMINDER_EMBED_KEY), captor.capture(), eq(withParameters.getChannel()));
        ReminderModel reminderModel = captor.getValue();
        Assert.assertEquals(reminderText, reminderModel.getRemindText());
        Assert.assertEquals(withParameters.getMessage(), reminderModel.getMessage());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
