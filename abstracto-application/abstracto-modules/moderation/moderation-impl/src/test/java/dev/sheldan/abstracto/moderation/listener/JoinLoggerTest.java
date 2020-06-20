package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.api.entities.Guild;
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

    @Test
    public void executeListener() {
        Member joiningMember = Mockito.mock(Member.class);
        Guild guild = Mockito.mock(Guild.class);
        Long guildId = 6L;
        when(guild.getIdLong()).thenReturn(guildId);
        AServer server = MockUtils.getServer();
        AUserInAServer aUserInAServer = MockUtils.getUserObject(5L, server);
        String message = "text";
        when(templateService.renderTemplateWithMap(eq(JoinLogger.USER_JOIN_TEMPLATE), any())).thenReturn(message);
        testUnit.execute(joiningMember, guild, aUserInAServer);
        verify(postTargetService, times(1)).sendTextInPostTarget(message, LoggingPostTarget.JOIN_LOG, guildId);
    }
}
