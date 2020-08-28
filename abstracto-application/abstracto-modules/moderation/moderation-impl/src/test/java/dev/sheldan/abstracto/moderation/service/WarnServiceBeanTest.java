package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.features.WarningDecayFeature;
import dev.sheldan.abstracto.moderation.config.posttargets.WarnDecayPostTarget;
import dev.sheldan.abstracto.moderation.config.posttargets.WarningPostTarget;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnLog;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnNotification;
import dev.sheldan.abstracto.moderation.models.template.job.WarnDecayLogModel;
import dev.sheldan.abstracto.moderation.models.template.job.WarnDecayWarning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WarnServiceBeanTest {

    @InjectMocks
    private WarnServiceBean testUnit;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private WarnManagementService warnManagementService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private TemplateService templateService;

    @Mock
    private BotService botService;

    @Mock
    private MessageService messageService;

    @Mock
    private ConfigService configService;

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
    private MessageChannel feedBackChannel;

    @Mock
    private User warnedSimpleUser;

    @Captor
    private ArgumentCaptor<WarnDecayLogModel> warnDecayLogModelArgumentCaptor;

    @Captor
    private ArgumentCaptor<WarnNotification> notificationCaptor;

    private AServer server;
    private AUserInAServer warningUser;
    private AUserInAServer firstWarnedUser;
    private AUserInAServer secondWarnedUser;
    private Warning firstWarning;
    private Warning secondWarning;
    private static final String REASON = "reason";
    private static final String NOTIFICATION_TEXT = "text";
    private static final String GUILD_NAME = "guild";

    @Before
    public void setup() {
        server = MockUtils.getServer();
        warningUser = MockUtils.getUserObject(8L, server);
        firstWarnedUser = MockUtils.getUserObject(5L, server);
        firstWarning = getDefaultWarning();
        firstWarning.setWarningUser(warningUser);
        firstWarning.setWarnedUser(firstWarnedUser);

        secondWarnedUser = MockUtils.getUserObject(7L, server);
        secondWarning = getDefaultWarning();
        secondWarning.setWarnedUser(secondWarnedUser);
        secondWarning.setWarningUser(warningUser);
    }

    @Test
    public void testDecayWarning() {
        Warning warning = getDefaultWarning();
        Instant date = Instant.now();
        testUnit.decayWarning(warning, date);
        Assert.assertTrue(warning.getDecayed());
        Assert.assertEquals(date, warning.getDecayDate());
    }

    @Test
    public void testDecayWarningsForServer() {
        setupWarnDecay();
        testUnit.decayWarningsForServer(server);
        verifyWarnDecayWithLog(true);
    }

    @Test
    public void testDecayAllWarningsForServerWithLog() {
        setupWarnDecay();
        testUnit.decayAllWarningsForServer(server, true);
        verifyWarnDecayWithLog(true);
    }

    @Test
    public void testDecayAllWarningsForServerWithoutLog() {
        setupWarnDecay();
        testUnit.decayAllWarningsForServer(server, false);
        verifyWarnDecayWithLog(false);
    }

    @Test
    public void testDecayAllWarningsWithoutWarnings() {
        List<Warning> warnings = Collections.emptyList();
        when(botService.getGuildByIdNullable(server.getId())).thenReturn(guild);
        when(templateService.renderEmbedTemplate(eq("warn_decay_log"), warnDecayLogModelArgumentCaptor.capture())).thenReturn(messageToSend);
        when(warnManagementService.getActiveWarningsInServerOlderThan(eq(server), any(Instant.class))).thenReturn(warnings);
        testUnit.decayAllWarningsForServer(server, true);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, WarnDecayPostTarget.DECAY_LOG, server.getId());
        WarnDecayLogModel model = warnDecayLogModelArgumentCaptor.getValue();
        List<WarnDecayWarning> usedWarnings = model.getWarnings();
        Assert.assertEquals(0, usedWarnings.size());
    }

    @Test
    public void testWarnFullUser() {
        setupMocksForWarning();
        FullUserInServer warnedFullUser = FullUserInServer.builder().member(warnedMember).aUserInAServer(firstWarnedUser).build();
        FullUserInServer warningFullUser = FullUserInServer.builder().member(warningMember).aUserInAServer(warningUser).build();
        Warning warning = testUnit.warnFullUser(warnedFullUser, warningFullUser, REASON, feedBackChannel);
        verifyWarning(warning);
    }

    @Test
    public void testWarnUser() {
        setupMocksForWarning();
        when(botService.getMemberInServer(firstWarnedUser)).thenReturn(warnedMember);
        when(botService.getMemberInServer(warningUser)).thenReturn(warningMember);
        Warning warning = testUnit.warnUser(firstWarnedUser, warningUser, REASON, feedBackChannel);
        verifyWarning(warning);
    }

    @Test
    public void testWarnMember() {
        setupMocksForWarning();
        when(userInServerManagementService.loadUser(warnedMember)).thenReturn(firstWarnedUser);
        when(userInServerManagementService.loadUser(warningMember)).thenReturn(warningUser);
        Warning warning = testUnit.warnMember(warnedMember, warningMember, REASON, feedBackChannel);
        verifyWarning(warning);
    }

    @Test
    public void testWarnUserWithLog() {
        setupMocksForWarning();
        when(userInServerManagementService.loadUser(warnedMember)).thenReturn(firstWarnedUser);
        when(userInServerManagementService.loadUser(warningMember)).thenReturn(warningUser);
        WarnLog log = WarnLog.builder().server(server).build();
        when(templateService.renderEmbedTemplate(eq(WarnServiceBean.WARN_LOG_TEMPLATE), any(WarnLog.class))).thenReturn(messageToSend);
        Warning warning = testUnit.warnUserWithLog(warnedMember, warningMember, REASON, log, feedBackChannel);
        verifyWarning(warning);
        verify( postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, WarningPostTarget.WARN_LOG, server.getId());
    }

    private void verifyWarning(Warning warning) {
        verify(messageService, times(1)).sendMessageToUser(warnedSimpleUser, NOTIFICATION_TEXT, feedBackChannel);
        WarnNotification notificationValue = notificationCaptor.getValue();
        Assert.assertEquals(firstWarning, notificationValue.getWarning());
        Assert.assertEquals(GUILD_NAME, notificationValue.getServerName());
        Assert.assertEquals(firstWarning, warning);
    }

    private void setupMocksForWarning() {
        when(warnedMember.getGuild()).thenReturn(guild);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(warnedMember.getUser()).thenReturn(warnedSimpleUser);
        when(warnManagementService.createWarning(firstWarnedUser, warningUser, REASON)).thenReturn(firstWarning);
        when(templateService.renderTemplate(eq(WarnServiceBean.WARN_NOTIFICATION_TEMPLATE), notificationCaptor.capture())).thenReturn(NOTIFICATION_TEXT);
    }

    private void verifyWarnDecayWithLog(boolean withLog) {
        int logCount = withLog ? 1 : 0;
        verify(postTargetService, times(logCount)).sendEmbedInPostTarget(messageToSend, WarnDecayPostTarget.DECAY_LOG, server.getId());
        if(withLog) {
            WarnDecayLogModel model = warnDecayLogModelArgumentCaptor.getValue();
            List<WarnDecayWarning> usedWarnings = model.getWarnings();
            Assert.assertEquals(firstWarning, usedWarnings.get(0).getWarning());
            Assert.assertEquals(warnedMember, usedWarnings.get(0).getWarnedMember());
            Assert.assertEquals(warningMember, usedWarnings.get(0).getWarningMember());
            Assert.assertEquals(secondWarning, usedWarnings.get(1).getWarning());
            Assert.assertEquals(secondWarnedMember, usedWarnings.get(1).getWarnedMember());
            Assert.assertEquals(warningMember, usedWarnings.get(1).getWarningMember());
            Assert.assertEquals(2, usedWarnings.size());
        }
    }

    private void setupWarnDecay() {
        when(configService.getLongValue(WarningDecayFeature.DECAY_DAYS_KEY, server.getId())).thenReturn(5L);
        List<Warning> warnings = Arrays.asList(firstWarning, secondWarning);
        when(botService.getMemberInServer(warningUser)).thenReturn(warningMember);
        when(botService.getMemberInServer(firstWarnedUser)).thenReturn(warnedMember);
        when(botService.getMemberInServer(secondWarnedUser)).thenReturn(secondWarnedMember);
        when(botService.getGuildByIdNullable(server.getId())).thenReturn(guild);
        when(templateService.renderEmbedTemplate(eq("warn_decay_log"), warnDecayLogModelArgumentCaptor.capture())).thenReturn(messageToSend);
        when(warnManagementService.getActiveWarningsInServerOlderThan(eq(server), any(Instant.class))).thenReturn(warnings);
    }

    public Warning getDefaultWarning() {
        return Warning.builder().build();
    }

}
