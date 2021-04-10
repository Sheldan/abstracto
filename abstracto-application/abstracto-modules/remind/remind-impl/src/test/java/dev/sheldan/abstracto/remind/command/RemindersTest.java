package dev.sheldan.abstracto.remind.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.template.commands.RemindersModel;
import dev.sheldan.abstracto.remind.service.management.ReminderManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemindersTest {

    @InjectMocks
    private Reminders testUnit;

    @Mock
    private ReminderManagementService reminderManagementService;

    @Mock
    private ChannelService channelService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private AChannel channel;

    @Captor
    private ArgumentCaptor<RemindersModel> modelCaptor;

    @Test
    public void testExecuteCommand() {
        CommandContext context = CommandTestUtilities.getNoParameters();
        Reminder reminder = Mockito.mock(Reminder.class);
        when(reminder.getChannel()).thenReturn(channel);
        Reminder secondReminder = Mockito.mock(Reminder.class);
        when(secondReminder.getChannel()).thenReturn(channel);
        List<Reminder> reminders = Arrays.asList(reminder, secondReminder);
        AUserInAServer user = Mockito.mock(AUserInAServer.class);
        when(userInServerManagementService.loadOrCreateUser(context.getAuthor())).thenReturn(user);
        when(reminderManagementService.getActiveRemindersForUser(user)).thenReturn(reminders);
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        verify(channelService, times(1)).sendEmbedTemplateInTextChannelList(eq(Reminders.REMINDERS_RESPONSE_TEMPLATE), modelCaptor.capture(), eq(context.getChannel()));
        RemindersModel usedModel = modelCaptor.getValue();
        Assert.assertEquals(reminder, usedModel.getReminders().get(0).getReminder());
        Assert.assertEquals(secondReminder, usedModel.getReminders().get(1).getReminder());
        Assert.assertEquals(reminders.size(), usedModel.getReminders().size());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
