package dev.sheldan.abstracto.logging.listener;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.logging.config.LoggingPostTarget;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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

    @Test
    public void testExecute() {
        when(model.getMember()).thenReturn(member);
        when(model.getServerId()).thenReturn(SERVER_ID);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(JoinLogger.USER_JOIN_TEMPLATE), any(), eq(SERVER_ID))).thenReturn(messageToSend);
        testUnit.execute(model);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, LoggingPostTarget.JOIN_LOG, SERVER_ID);
    }

}
