package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.mode.ModerationMode;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BanServiceBeanTest {

    public static final String REASON = "reason";
    private static final Long SERVER_ID = 4L;
    private static final Long USER_ID = 5L;
    @InjectMocks
    private BanServiceBean testUnit;

    @Mock
    private GuildService guildService;

    @Mock
    private TemplateService templateService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private FeatureModeService featureModeService;

    @Test
    public void testBanMemberByMemberWithoutLog() {
        Member memberToBan = Mockito.mock(Member.class);
        when(memberToBan.getIdLong()).thenReturn(USER_ID);
        ServerContext context = Mockito.mock(ServerContext.class);
        Guild mockedGuild = Mockito.mock(Guild.class);
        when(memberToBan.getGuild()).thenReturn(mockedGuild);
        when(mockedGuild.getIdLong()).thenReturn(SERVER_ID);
        AuditableRestAction mockedAction = mock(AuditableRestAction.class);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedGuild.ban(USER_ID.toString(), 0, REASON)).thenReturn(mockedAction);
        MessageToSend mockedMessage = Mockito.mock(MessageToSend.class);
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.MODERATION, SERVER_ID, ModerationMode.BAN_LOG)).thenReturn(false);
        testUnit.banMember(memberToBan, REASON, context);
        verify(mockedGuild, times(1)).ban(USER_ID.toString(), 0, REASON);
        verify(postTargetService, times(0)).sendEmbedInPostTarget(mockedMessage, ModerationPostTarget.BAN_LOG, SERVER_ID);
        verify(templateService, times(0)).renderEmbedTemplate(BanServiceBean.BAN_LOG_TEMPLATE, context, SERVER_ID);
    }

    @Test
    public void testBanMemberWithLog() {
        Member memberToBan = Mockito.mock(Member.class);
        when(memberToBan.getIdLong()).thenReturn(USER_ID);
        ServerContext context = Mockito.mock(ServerContext.class);
        Guild mockedGuild = Mockito.mock(Guild.class);
        when(memberToBan.getGuild()).thenReturn(mockedGuild);
        when(mockedGuild.getIdLong()).thenReturn(SERVER_ID);
        AuditableRestAction mockedAction = mock(AuditableRestAction.class);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedGuild.ban(USER_ID.toString(), 0, REASON)).thenReturn(mockedAction);
        MessageToSend mockedMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(BanServiceBean.BAN_LOG_TEMPLATE, context, SERVER_ID)).thenReturn(mockedMessage);
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.MODERATION, SERVER_ID, ModerationMode.BAN_LOG)).thenReturn(true);
        testUnit.banMember(memberToBan, REASON, context);
        verify(mockedGuild, times(1)).ban(USER_ID.toString(), 0, REASON);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(mockedMessage, ModerationPostTarget.BAN_LOG, SERVER_ID);
    }


    @Test
    public void testBanMemberById() {
        ServerContext context = Mockito.mock(ServerContext.class);
        Guild mockedGuild = Mockito.mock(Guild.class);
        AuditableRestAction mockedAction = mock(AuditableRestAction.class);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedGuild.ban(USER_ID.toString(), 0, REASON)).thenReturn(mockedAction);
        MessageToSend mockedMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(BanServiceBean.BAN_ID_LOG_TEMPLATE, context, SERVER_ID)).thenReturn(mockedMessage);
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.MODERATION, SERVER_ID, ModerationMode.BAN_LOG)).thenReturn(true);
        when(guildService.getGuildByIdOptional(SERVER_ID)).thenReturn(Optional.of(mockedGuild));
        testUnit.banUserViaId(SERVER_ID, USER_ID, REASON, context);
        verify(mockedGuild, times(1)).ban(USER_ID.toString(), 0, REASON);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(mockedMessage, ModerationPostTarget.BAN_LOG, SERVER_ID);
    }

    @Test(expected = GuildNotFoundException.class)
    public void tryToBanInNonExistentGuild() {
        ServerContext context = Mockito.mock(ServerContext.class);
        when(guildService.getGuildByIdOptional(SERVER_ID)).thenReturn(Optional.empty());
        testUnit.banUserViaId(SERVER_ID, USER_ID, REASON, context);
    }

}
