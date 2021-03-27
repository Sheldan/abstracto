package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttarget.LoggingPostTarget;
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

    @Mock
    private MemberJoinModel model;

    private static final Long SERVER_ID = 1L;
    private static final Long USER_ID = 2L;

    @Test
    public void testExecute() {
        when(model.getMember()).thenReturn(member);
        when(model.getServerId()).thenReturn(SERVER_ID);
        String message = "text";
        when(templateService.renderTemplateWithMap(eq(JoinLogger.USER_JOIN_TEMPLATE), any(), eq(SERVER_ID))).thenReturn(message);
        testUnit.execute(model);
        verify(postTargetService, times(1)).sendTextInPostTarget(message, LoggingPostTarget.JOIN_LOG, SERVER_ID);
    }

}
