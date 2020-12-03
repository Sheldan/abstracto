package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.utility.service.management.PostedImageManagement;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PostedImageServiceBeanTest {

    @InjectMocks
    private PostedImageServiceBean testUnit;

    @Mock
    private PostedImageManagement postedImageManagement;

    @Mock
    private ServerManagementService serverManagementService;

    @Test
    public void testPurgePostedImagesOfAUserInAServer() {
        AUserInAServer aUserInAServer = Mockito.mock(AUserInAServer.class);
        testUnit.purgePostedImages(aUserInAServer);
        verify(postedImageManagement, times(1)).removePostedImagesOf(aUserInAServer);
    }

    @Test
    public void testPurgePostedImagesInGuild() {
        Guild guild = Mockito.mock(Guild.class);
        Long serverId = 5L;
        when(guild.getIdLong()).thenReturn(serverId);
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(serverId)).thenReturn(server);
        testUnit.purgePostedImages(guild);
        verify(postedImageManagement, times(1)).removedPostedImagesIn(server);
    }

    @Test(expected = GuildNotFoundException.class)
    public void testPurgePostedImagesNotExistingServer() {
        Guild guild = Mockito.mock(Guild.class);
        Long serverId = 5L;
        when(guild.getIdLong()).thenReturn(serverId);
        when(serverManagementService.loadServer(serverId)).thenThrow(new GuildNotFoundException(serverId));
        testUnit.purgePostedImages(guild);
    }

}
