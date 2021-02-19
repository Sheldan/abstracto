package dev.sheldan.abstracto.moderation.listener.async;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JoinLoggerTest {

    @InjectMocks
    private JoinLogger testUnit;

    @Mock
    private TemplateService templateService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private MemberService memberService;

    @Mock
    private JoinLogger self;

    @Mock
    private ServerUser serverUser;

    @Mock
    private Member member;


    private static final Long SERVER_ID = 1L;
    private static final Long USER_ID = 2L;

    @Test
    public void testExecute() {
        when(serverUser.getUserId()).thenReturn(USER_ID);
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(member));
        testUnit.execute(serverUser);
        verify(self, times(1)).sendJoinLog(serverUser, member);
    }

    @Test
    public void testJoinLog() {
        String message = "text";
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        when(templateService.renderTemplateWithMap(eq(JoinLogger.USER_JOIN_TEMPLATE), any(), eq(SERVER_ID))).thenReturn(message);
        testUnit.sendJoinLog(serverUser, member);
        verify(postTargetService, times(1)).sendTextInPostTarget(message, LoggingPostTarget.JOIN_LOG, SERVER_ID);
    }
}
