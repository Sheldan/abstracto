package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.RemindersModel;
import dev.sheldan.abstracto.utility.service.management.ReminderManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemindersTest {

    @InjectMocks
    private Reminders testUnit;

    @Mock
    private ReminderManagementService reminderManagementService;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<RemindersModel> modelCaptor;

    @Test
    public void testExecuteCommand() {
        CommandContext context = CommandTestUtilities.getNoParameters();
        Reminder reminder = Reminder.builder().build();
        Reminder secondReminder = Reminder.builder().build();
        List<Reminder> reminders = Arrays.asList(reminder, secondReminder);
        when(reminderManagementService.getActiveRemindersForUser(context.getUserInitiatedContext().getAUserInAServer())).thenReturn(reminders);
        CommandResult result = testUnit.execute(context);
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(Reminders.REMINDERS_RESPONSE_TEMPLATE), modelCaptor.capture(), eq(context.getChannel()));
        RemindersModel usedModel = modelCaptor.getValue();
        Assert.assertEquals(reminder, usedModel.getReminders().get(0));
        Assert.assertEquals(secondReminder, usedModel.getReminders().get(1));
        Assert.assertEquals(reminders.size(), usedModel.getReminders().size());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
