package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.mode.ModerationMode;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.template.command.KickLogModel;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KickServiceBeanTest {

    @InjectMocks
    private KickServiceBean testUnit;

    @Mock
    private TemplateService templateService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private FeatureModeService featureModeService;

    private static final Long SERVER_ID = 1L;

    @Test
    public void testKickMemberWithoutLog() {
        User user = Mockito.mock(User.class);
        Member member = Mockito.mock(Member.class);
        when(member.getUser()).thenReturn(user);
        when(user.getIdLong()).thenReturn(6L);
        Guild mockedGuild = Mockito.mock(Guild.class);
        when(mockedGuild.getIdLong()).thenReturn(SERVER_ID);
        when(member.getGuild()).thenReturn(mockedGuild);
        String reason = "reason";
        AuditableRestAction<Void> mockedAction = Mockito.mock(AuditableRestAction.class);
        when(mockedGuild.kick(member, reason)).thenReturn(mockedAction);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        KickLogModel model = Mockito.mock(KickLogModel.class);
        when(model.getGuild()).thenReturn(mockedGuild);
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.MODERATION, SERVER_ID, ModerationMode.KICK_LOG)).thenReturn(false);
        testUnit.kickMember(member, reason, model);
        verify(postTargetService, times(0)).sendEmbedInPostTarget(any(MessageToSend.class), eq(ModerationPostTarget.KICK_LOG), eq(SERVER_ID));
        verify(templateService, times(0)).renderEmbedTemplate(KickServiceBean.KICK_LOG_TEMPLATE, model, SERVER_ID);
    }

    @Test
    public void testKickMemberWithLog() {
        User user = Mockito.mock(User.class);
        Member member = Mockito.mock(Member.class);
        when(member.getUser()).thenReturn(user);
        when(user.getIdLong()).thenReturn(6L);
        Guild mockedGuild = Mockito.mock(Guild.class);
        when(mockedGuild.getIdLong()).thenReturn(SERVER_ID);
        when(member.getGuild()).thenReturn(mockedGuild);
        String reason = "reason";
        AuditableRestAction<Void> mockedAction = Mockito.mock(AuditableRestAction.class);
        when(mockedGuild.kick(member, reason)).thenReturn(mockedAction);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        KickLogModel model = Mockito.mock(KickLogModel.class);
        when(model.getGuild()).thenReturn(mockedGuild);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(KickServiceBean.KICK_LOG_TEMPLATE, model, SERVER_ID)).thenReturn(messageToSend);
        when(featureModeService.featureModeActive(ModerationFeatureDefinition.MODERATION, SERVER_ID, ModerationMode.KICK_LOG)).thenReturn(true);
        testUnit.kickMember(member, reason, model);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, ModerationPostTarget.KICK_LOG, SERVER_ID);
    }
}