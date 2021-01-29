package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.features.mode.ModerationMode;
import dev.sheldan.abstracto.moderation.config.posttargets.ModerationPostTarget;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
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
        Long userId = 8L;
        Long serverId = 9L;
        Member memberToBan = Mockito.mock(Member.class);
        when(memberToBan.getIdLong()).thenReturn(userId);
        ServerContext context = Mockito.mock(ServerContext.class);
        Guild mockedGuild = Mockito.mock(Guild.class);
        when(memberToBan.getGuild()).thenReturn(mockedGuild);
        when(mockedGuild.getIdLong()).thenReturn(serverId);
        AuditableRestAction mockedAction = mock(AuditableRestAction.class);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedGuild.ban(userId.toString(), 0, REASON)).thenReturn(mockedAction);
        MessageToSend mockedMessage = Mockito.mock(MessageToSend.class);
        when(featureModeService.featureModeActive(ModerationFeatures.MODERATION, serverId, ModerationMode.BAN_LOG)).thenReturn(false);
        testUnit.banMember(memberToBan, REASON, context);
        verify(mockedGuild, times(1)).ban(userId.toString(), 0, REASON);
        verify(postTargetService, times(0)).sendEmbedInPostTarget(mockedMessage, ModerationPostTarget.BAN_LOG, serverId);
        verify(templateService, times(0)).renderEmbedTemplate(BanServiceBean.BAN_LOG_TEMPLATE, context);
    }

    @Test
    public void testBanMemberWithLog() {
        Long userId = 8L;
        Long serverId = 9L;
        Member memberToBan = Mockito.mock(Member.class);
        when(memberToBan.getIdLong()).thenReturn(userId);
        ServerContext context = Mockito.mock(ServerContext.class);
        Guild mockedGuild = Mockito.mock(Guild.class);
        when(memberToBan.getGuild()).thenReturn(mockedGuild);
        when(mockedGuild.getIdLong()).thenReturn(serverId);
        AuditableRestAction mockedAction = mock(AuditableRestAction.class);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedGuild.ban(userId.toString(), 0, REASON)).thenReturn(mockedAction);
        MessageToSend mockedMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(BanServiceBean.BAN_LOG_TEMPLATE, context)).thenReturn(mockedMessage);
        when(featureModeService.featureModeActive(ModerationFeatures.MODERATION, serverId, ModerationMode.BAN_LOG)).thenReturn(true);
        testUnit.banMember(memberToBan, REASON, context);
        verify(mockedGuild, times(1)).ban(userId.toString(), 0, REASON);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(mockedMessage, ModerationPostTarget.BAN_LOG, serverId);
    }


    @Test
    public void testBanMemberById() {
        Long userId = 8L;
        Long serverId = 5L;
        ServerContext context = Mockito.mock(ServerContext.class);
        Guild mockedGuild = Mockito.mock(Guild.class);
        AuditableRestAction mockedAction = mock(AuditableRestAction.class);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedGuild.ban(userId.toString(), 0, REASON)).thenReturn(mockedAction);
        MessageToSend mockedMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(BanServiceBean.BAN_ID_LOG_TEMPLATE, context)).thenReturn(mockedMessage);
        when(featureModeService.featureModeActive(ModerationFeatures.MODERATION, serverId, ModerationMode.BAN_LOG)).thenReturn(true);
        when(guildService.getGuildByIdOptional(serverId)).thenReturn(Optional.of(mockedGuild));
        testUnit.banUserViaId(serverId, userId, REASON, context);
        verify(mockedGuild, times(1)).ban(userId.toString(), 0, REASON);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(mockedMessage, ModerationPostTarget.BAN_LOG, serverId);
    }

    @Test(expected = GuildNotFoundException.class)
    public void tryToBanInNonExistentGuild() {
        Long userId = 8L;
        Long serverId = 5L;
        ServerContext context = Mockito.mock(ServerContext.class);
        when(guildService.getGuildByIdOptional(serverId)).thenReturn(Optional.empty());
        testUnit.banUserViaId(serverId, userId, REASON, context);
    }

}
