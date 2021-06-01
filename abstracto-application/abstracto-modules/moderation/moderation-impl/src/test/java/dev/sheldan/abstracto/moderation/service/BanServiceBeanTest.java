package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
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
public class BanServiceBeanTest {

    public static final String REASON = "reason";
    private static final Long SERVER_ID = 4L;
    private static final Long USER_ID = 5L;
    @InjectMocks
    private BanServiceBean testUnit;

    @Mock
    private TemplateService templateService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private Message message;

    @Test
    public void testBanMemberWithLog() {
        Member memberToBan = Mockito.mock(Member.class);
        User user = Mockito.mock(User.class);
        when(memberToBan.getUser()).thenReturn(user);
        Member banningMember = Mockito.mock(Member.class);
        Guild mockedGuild = Mockito.mock(Guild.class);
        when(memberToBan.getGuild()).thenReturn(mockedGuild);
        when(mockedGuild.getIdLong()).thenReturn(SERVER_ID);
        AuditableRestAction mockedAction = mock(AuditableRestAction.class);
        when(mockedAction.submit()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedGuild.ban(user, 0, REASON)).thenReturn(mockedAction);
        MessageToSend mockedMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(BanServiceBean.BAN_LOG_TEMPLATE), any(), eq(SERVER_ID))).thenReturn(mockedMessage);
        testUnit.banMember(memberToBan, REASON, banningMember, message);
        verify(mockedGuild, times(1)).ban(user, 0, REASON);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(mockedMessage, ModerationPostTarget.BAN_LOG, SERVER_ID);
    }

}
