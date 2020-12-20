package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.commands.PingModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
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
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private BotService botService;

    @Before
    public void setup() {
        GuildChannelMember build = GuildChannelMember.builder().build();
        when(botService.getServerChannelUser(eq(SERVER_ID), eq(CHANNEL_ID), eq(AUTHOR_ID))).thenReturn(build);
        AServer server = AServer.builder().id(SERVER_ID).build();
        AUserInAServer aUserInAServer = AUserInAServer.builder().userReference(AUser.builder().id(AUTHOR_ID).build()).serverReference(server).build();
        when(userInServerManagementService.loadUser(eq(SERVER_ID), eq(AUTHOR_ID))).thenReturn(aUserInAServer);
        AChannel channel = AChannel.builder().id(CHANNEL_ID).build();
        when(channelManagementService.loadChannel(eq(CHANNEL_ID))).thenReturn(channel);
    }

    @Test
    public void testFromMessage() {
        PingModel pingModel = (PingModel) classToTest.fromMessage(buildCachedMessage(), PingModel.class);
        assertEquals(AUTHOR_ID, pingModel.getUser().getId());
        assertEquals(AUTHOR_ID, pingModel.getAUserInAServer().getUserReference().getId());
        assertEquals(SERVER_ID, pingModel.getAUserInAServer().getServerReference().getId());
        assertEquals(SERVER_ID, pingModel.getServer().getId());
        assertEquals(CHANNEL_ID, pingModel.getChannel().getId());
    }

    private CachedMessage buildCachedMessage() {
        return CachedMessage.builder().author(CachedAuthor.builder().authorId(AUTHOR_ID).build()).serverId(SERVER_ID).channelId(CHANNEL_ID).build();
    }
}
