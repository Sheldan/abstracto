package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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

    @Test
    public void executeListener() {
        Member leavingMember = Mockito.mock(Member.class);
        Guild guild = Mockito.mock(Guild.class);
        User user = Mockito.mock(User.class);
        when(leavingMember.getUser()).thenReturn(user);
        Long guildId = 6L;
        when(guild.getIdLong()).thenReturn(guildId);
        String message = "text";
        when(templateService.renderTemplateWithMap(eq(LeaveLogger.USER_LEAVE_TEMPLATE), any())).thenReturn(message);
        testUnit.execute(leavingMember, guild);
        verify(postTargetService, times(1)).sendTextInPostTarget(message, LoggingPostTarget.LEAVE_LOG, guildId);

    }
}
