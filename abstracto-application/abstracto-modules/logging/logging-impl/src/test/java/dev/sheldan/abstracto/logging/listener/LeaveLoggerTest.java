package dev.sheldan.abstracto.logging.listener;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberLeaveModel;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.logging.config.LoggingPostTarget;
import net.dv8tion.jda.api.entities.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LeaveLoggerTest {

    @InjectMocks
    private LeaveLogger testUnit;

    @Mock
    private TemplateService templateService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private MemberService memberService;

    @Mock
    private LeaveLogger self;

    @Mock
    private ServerUser leavingUser;

    @Mock
    private User user;

    @Mock
    private MemberLeaveModel model;

    private static final Long SERVER_ID = 1L;

     @Test
     public void testExecute() {
         when(model.getServerId()).thenReturn(SERVER_ID);
         when(model.getUser()).thenReturn(user);
         MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
         when(templateService.renderEmbedTemplate(eq(LeaveLogger.USER_LEAVE_TEMPLATE), any(), eq(SERVER_ID))).thenReturn(messageToSend);
         testUnit.execute(model);
         verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, LoggingPostTarget.LEAVE_LOG, SERVER_ID);
     }

}
