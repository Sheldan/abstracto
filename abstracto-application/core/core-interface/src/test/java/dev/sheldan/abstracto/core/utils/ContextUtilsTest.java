package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.commands.PingModel;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContextUtilsTest {

    public static final Long CHANNEL_ID = 1L;
    public static final Long SERVER_ID = 1L;
    public static final Long AUTHOR_ID = 1L;
    @InjectMocks
    private ContextUtils classToTest;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private Bot bot;

    @Before
    public void setup() {
        GuildChannelMember build = GuildChannelMember.builder().build();
        when(bot.getServerChannelUser(eq(SERVER_ID), eq(CHANNEL_ID), eq(AUTHOR_ID))).thenReturn(build);
        AServer server = AServer.builder().id(SERVER_ID).build();
        AUserInAServer aUserInAServer = AUserInAServer.builder().userReference(AUser.builder().id(AUTHOR_ID).build()).serverReference(server).build();
        when(userManagementService.loadUser(eq(SERVER_ID), eq(AUTHOR_ID))).thenReturn(aUserInAServer);
        AChannel channel = AChannel.builder().id(CHANNEL_ID).build();
        when(channelManagementService.loadChannel(eq(CHANNEL_ID))).thenReturn(channel);
    }

    @Test
    public void testFromMessage() {
        PingModel pingModel = (PingModel) classToTest.fromMessage(buildCachedMessage(), PingModel.class);
        assertEquals(pingModel.getUser().getId(), AUTHOR_ID);
        assertEquals(pingModel.getAUserInAServer().getUserReference().getId(), AUTHOR_ID);
        assertEquals(pingModel.getAUserInAServer().getServerReference().getId(), SERVER_ID);
        assertEquals(pingModel.getServer().getId(), SERVER_ID);
        assertEquals(pingModel.getChannel().getId(), CHANNEL_ID);
    }

    private CachedMessage buildCachedMessage() {
        return CachedMessage.builder().authorId(AUTHOR_ID).serverId(SERVER_ID).channelId(CHANNEL_ID).build();
    }
}
