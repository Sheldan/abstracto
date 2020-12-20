package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.moderation.listener.async.LeaveLogger;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

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
    private BotService botService;

    @Mock
    private LeaveLogger self;

    @Mock
    private ServerUser leavingUser;

    @Mock
    private Member member;

    private static final Long SERVER_ID = 1L;
    private static final Long USER_ID = 2L;

     @Test
     public void testExecute() {
         when(leavingUser.getUserId()).thenReturn(USER_ID);
         when(leavingUser.getServerId()).thenReturn(SERVER_ID);
         when(botService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(member));
         testUnit.execute(leavingUser);
         verify(self, times(1)).executeJoinLogging(leavingUser, member);
     }

    @Test
    public void executeListener() {
        User user = Mockito.mock(User.class);
        when(member.getUser()).thenReturn(user);
        String message = "text";
        when(leavingUser.getServerId()).thenReturn(SERVER_ID);
        when(templateService.renderTemplateWithMap(eq(LeaveLogger.USER_LEAVE_TEMPLATE), any())).thenReturn(message);
        testUnit.executeJoinLogging(leavingUser, member);
        verify(postTargetService, times(1)).sendTextInPostTarget(message, LoggingPostTarget.LEAVE_LOG, SERVER_ID);

    }
}
