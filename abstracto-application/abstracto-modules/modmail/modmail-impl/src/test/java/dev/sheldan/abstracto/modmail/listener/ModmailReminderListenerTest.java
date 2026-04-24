package dev.sheldan.abstracto.modmail.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureConfig;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailMode;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.listener.ModmailThreadActionListenerModel;
import dev.sheldan.abstracto.modmail.service.management.ModMailRoleManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ModmailReminderListenerTest {
    @InjectMocks
    private ModmailReminderListener unitUnderTest;

    @Mock
    private ChannelService channelService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ModMailThreadManagementService modMailThreadManagementService;

    @Mock
    private ConfigService configService;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private ModMailRoleManagementService modMailRoleManagementService;

    private static final long SERVER_ID = 1L;
    private static final long THREAD_ID = 2L;
    private static final long USER_ID = 3L;

    @Before
    public void setup() {
        when(featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, SERVER_ID, ModMailMode.THREAD_REMINDER)).thenReturn(true);
        when(configService.getStringValueOrConfigDefault(ModMailFeatureConfig.MOD_MAIL_REMINDER_DURATION, SERVER_ID)).thenReturn("5m");
        MessageToSend messageToSend = MessageToSend
            .builder()
            .build();
        when(templateService.renderEmbedTemplate(anyString(), any(), any())).thenReturn(messageToSend);
        when(modMailRoleManagementService.getRolesForServer(any())).thenReturn(List.of());
    }

    @Test
    public void executeInitialReminder() {
        Instant updatedTimeStamp = Instant.now().minus(10, ChronoUnit.MINUTES);
        Instant snoozedUntil = Instant.now().minus(1, ChronoUnit.SECONDS);
        executeTest(updatedTimeStamp, snoozedUntil, true);
    }

    @Test
    public void shouldNotExecuteAfterSnoozing() {
        Instant updatedTimeStamp = Instant.now().minus(10, ChronoUnit.MINUTES);
        Instant snoozedUntil = Instant.now().plus(5, ChronoUnit.MINUTES);
        executeTest(updatedTimeStamp, snoozedUntil, false);
    }

    @Test
    public void shouldExecuteAfterSnoozingButSnoozingHasPassed() {
        Instant updatedTimeStamp = Instant.now().minus(25, ChronoUnit.MINUTES);
        Instant snoozedUntil = Instant.now().minus(2, ChronoUnit.MINUTES);
        executeTest(updatedTimeStamp, snoozedUntil, true);
    }

    private void executeTest(Instant updatedTimeStamp, Instant snoozedUntil, boolean shouldExecute) {
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        when(thread.getUpdated()).thenReturn(updatedTimeStamp);
        when(thread.getRemindersSnoozedUntil()).thenReturn(snoozedUntil);
        AUser user = AUser
            .builder()
            .id(USER_ID)
            .build();
        AServer server = AServer
            .builder()
            .id(SERVER_ID)
            .build();
        AUserInAServer aUserInAServer = AUserInAServer
            .builder()
            .serverReference(server)
            .userReference(user)
            .userInServerId(USER_ID)
            .build();
        when(thread.getServer()).thenReturn(server);
        when(thread.getUser()).thenReturn(aUserInAServer);
        when(modMailThreadManagementService.getById(THREAD_ID)).thenReturn(thread);

        ModmailThreadActionListenerModel model = getModel();

        unitUnderTest.execute(model);
        if(shouldExecute) {
            verify(channelService).sendMessageEmbedToSendToAChannel(any(), any());
        } else {
            verify(channelService, times(0)).sendMessageEmbedToSendToAChannel(any(), any());
        }
    }

    private static ModmailThreadActionListenerModel getModel() {
        return ModmailThreadActionListenerModel
            .builder()
            .serverId(SERVER_ID)
            .threadId(THREAD_ID)
            .build();
    }
}
