package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.WarningDecayFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.mode.WarnDecayMode;
import dev.sheldan.abstracto.moderation.config.feature.mode.WarningMode;
import dev.sheldan.abstracto.moderation.config.posttarget.WarningPostTarget;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import dev.sheldan.abstracto.moderation.model.template.command.WarnContext;
import dev.sheldan.abstracto.moderation.model.template.command.WarnNotification;
import dev.sheldan.abstracto.moderation.model.template.job.WarnDecayLogModel;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.service.WarnServiceBean.WARNINGS_COUNTER_KEY;
import static dev.sheldan.abstracto.moderation.service.WarnServiceBean.WARN_LOG_TEMPLATE;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WarnServiceBeanTest {

    public static final long WARN_ID = 8L;
    @InjectMocks
    private WarnServiceBean testUnit;

    @Mock
    private WarnManagementService warnManagementService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private TemplateService templateService;

    @Mock
    private MemberService memberService;

    @Mock
    private ConfigService configService;

    @Mock
    private WarnServiceBean self;

    @Mock
    private Member warnedMember;

    @Mock
    private Member warningMember;

    @Mock
    private Member secondWarnedMember;

    @Mock
    private Guild guild;

    @Mock
    private MessageToSend messageToSend;

    @Mock
    private User warnedSimpleUser;

    @Mock
    private WarnContext context;

    @Captor
    private ArgumentCaptor<WarnDecayLogModel> warnDecayLogModelArgumentCaptor;

    @Captor
    private ArgumentCaptor<WarnNotification> notificationCaptor;

    @Mock
    private AServer server;

    @Mock
    private AUserInAServer warningUser;

    @Mock
    private AUserInAServer firstWarnedUser;

    @Mock
    private AUserInAServer secondWarnedUser;

    @Mock
    private AUser firstAUser;

    @Mock
    private AUser secondAUser;

    @Mock
    private AUser thirdAUser;

    @Mock
    private Warning firstWarning;

    @Mock
    private Warning secondWarning;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private CounterService counterService;

    @Mock
    private MessageService messageService;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private DefaultConfigManagementService defaultConfigManagementService;

    private static final String NOTIFICATION_TEXT = "text";
    private static final String GUILD_NAME = "guild";
    private static final Long SERVER_ID = 4L;

    @Test
    public void testDecayWarning() {
        Instant date = Instant.now();
        when(firstWarning.getWarnId()).thenReturn(new ServerSpecificId(SERVER_ID, 4L));
        testUnit.decayWarning(firstWarning, date);
        verify(firstWarning, times(1)).setDecayed(true);
        verify(firstWarning, times(1)).setDecayDate(date);
    }

    @Test
    public void testDecayWarningsForServer() {
        setupWarnDecay();
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.AUTOMATIC_WARN_DECAY, server, WarnDecayMode.AUTOMATIC_WARN_DECAY_LOG)).thenReturn(true);
        testUnit.decayWarningsForServer(server);
        verify(self, times(1)).renderAndSendWarnDecayLogs(eq(SERVER_ID), any());
    }

    @Test
    public void testDecayWarningsForServerWithoutLog() {
        setupWarnDecay();
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.AUTOMATIC_WARN_DECAY, server, WarnDecayMode.AUTOMATIC_WARN_DECAY_LOG)).thenReturn(false);
        testUnit.decayWarningsForServer(server);
        verify(self, times(0)).renderAndSendWarnDecayLogs(eq(SERVER_ID), any());
    }

    @Test
    public void testDecayAllWarningsForServer() {
        setupWarnDecay();
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.WARNING, server, WarningMode.WARN_DECAY_LOG)).thenReturn(true);
        testUnit.decayAllWarningsForServer(server);
        verify(self, times(1)).renderAndSendWarnDecayLogs(eq(SERVER_ID), any());
    }

    @Test
    public void testDecayAllWarningsForServerWithoutLog() {
        setupWarnDecay();
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.WARNING, server, WarningMode.WARN_DECAY_LOG)).thenReturn(false);
        testUnit.decayAllWarningsForServer(server);
        verify(self, times(0)).renderAndSendWarnDecayLogs(eq(SERVER_ID), any());
    }


    @Test
    public void testDecayAllWarningsWithoutWarningsWithoutLog() {
        List<Warning> warnings = Collections.emptyList();
        when(server.getId()).thenReturn(SERVER_ID);
        when(warnManagementService.getActiveWarningsInServerOlderThan(eq(server), any(Instant.class))).thenReturn(warnings);
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.WARNING, server, WarningMode.WARN_DECAY_LOG)).thenReturn(false);
        testUnit.decayAllWarningsForServer(server);
        verify(self, times(0)).renderAndSendWarnDecayLogs(eq(SERVER_ID), any());
    }

    @Test
    public void testDecayAllWarningsWithoutWarningsWithLog() {
        List<Warning> warnings = Collections.emptyList();
        when(server.getId()).thenReturn(SERVER_ID);
        when(warnManagementService.getActiveWarningsInServerOlderThan(eq(server), any(Instant.class))).thenReturn(warnings);
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.WARNING, server, WarningMode.WARN_DECAY_LOG)).thenReturn(true);
        testUnit.decayAllWarningsForServer(server);
        verify(self, times(1)).renderAndSendWarnDecayLogs(eq(SERVER_ID), any());
    }

    @Test
    public void testWarnFullUser() {
        setupWarnContext();
        setupMocksForWarning();
        CompletableFuture<Void> future = testUnit.notifyAndLogFullUserWarning(context);
        future.join();
        Assert.assertFalse(future.isCompletedExceptionally());
    }

    private void setupWarnContext() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(context.getGuild()).thenReturn(guild);
        when(context.getWarnedMember()).thenReturn(warnedMember);
        when(context.getMember()).thenReturn(warningMember);
        when(counterService.getNextCounterValue(server, WARNINGS_COUNTER_KEY)).thenReturn(WARN_ID);
    }


    private void setupMocksForWarning() {
        setupWarnings();
        when(warnedMember.getGuild()).thenReturn(guild);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(warnedMember.getUser()).thenReturn(warnedSimpleUser);
        when(templateService.renderEmbedTemplate(eq(WARN_LOG_TEMPLATE), warnDecayLogModelArgumentCaptor.capture(), eq(SERVER_ID))).thenReturn(messageToSend);
        when(messageService.sendMessageToUser(eq(warnedMember.getUser()), any())).thenReturn(CompletableFuture.completedFuture(null));
        when(postTargetService.sendEmbedInPostTarget(messageToSend, WarningPostTarget.WARN_LOG, SERVER_ID)).thenReturn(CommandTestUtilities.messageFutureList());
        when(templateService.renderTemplate(eq(WarnServiceBean.WARN_NOTIFICATION_TEMPLATE), notificationCaptor.capture(), eq(SERVER_ID))).thenReturn(NOTIFICATION_TEXT);
        when(serverManagementService.loadOrCreate(SERVER_ID)).thenReturn(server);
    }

    private void setupWarnings() {
        when(firstWarning.getWarningUser()).thenReturn(warningUser);
        when(secondWarning.getWarningUser()).thenReturn(warningUser);
        when(warningUser.getServerReference()).thenReturn(server);
        when(warningUser.getUserReference()).thenReturn(thirdAUser);

        when(firstWarning.getWarnedUser()).thenReturn(firstWarnedUser);
        when(firstWarnedUser.getServerReference()).thenReturn(server);
        when(firstWarnedUser.getUserReference()).thenReturn(firstAUser);
        when(secondWarning.getWarnedUser()).thenReturn(secondWarnedUser);
        when(secondWarnedUser.getServerReference()).thenReturn(server);
        when(secondWarnedUser.getUserReference()).thenReturn(secondAUser);
        when(firstWarning.getWarnId()).thenReturn(new ServerSpecificId(SERVER_ID, WARN_ID));
        when(secondWarning.getWarnId()).thenReturn(new ServerSpecificId(SERVER_ID, 9L));
        when(server.getId()).thenReturn(SERVER_ID);
    }

    private void setupWarnDecay() {
        setupWarnings();
        SystemConfigProperty defaultDecayDays = Mockito.mock(SystemConfigProperty.class);
        Long defaultDayCount = 4L;
        when(defaultDecayDays.getLongValue()).thenReturn(defaultDayCount);
        when(defaultConfigManagementService.getDefaultConfig(WarningDecayFeatureConfig.DECAY_DAYS_KEY)).thenReturn(defaultDecayDays);
        when(configService.getLongValue(WarningDecayFeatureConfig.DECAY_DAYS_KEY, SERVER_ID, defaultDayCount)).thenReturn(5L);
        List<Warning> warnings = Arrays.asList(firstWarning, secondWarning);
        when(memberService.getMemberInServerAsync(warningUser)).thenReturn(CompletableFuture.completedFuture(warningMember));
        when(memberService.getMemberInServerAsync(firstWarnedUser)).thenReturn(CompletableFuture.completedFuture(warnedMember));
        when(memberService.getMemberInServerAsync(secondWarnedUser)).thenReturn(CompletableFuture.completedFuture(secondWarnedMember));
        when(warnManagementService.getActiveWarningsInServerOlderThan(eq(server), any(Instant.class))).thenReturn(warnings);
    }

}
