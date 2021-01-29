package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.MockUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SlowModeServiceBeanTest {

    @InjectMocks
    private SlowModeServiceBean testUnit;

    @Mock
    private ChannelService channelService;

    @Mock
    private ChannelManager returnedManager;

    @Mock
    private Guild guild;

    @Mock
    private TextChannel channel;

    @Test
    public void setSlowModeInTextChannel() {
        when(channel.getGuild()).thenReturn(guild);
        Duration duration = Duration.ofMinutes(5);
        testUnit.setSlowMode(channel, duration);
        verify(channelService, times(1)).setSlowModeInChannel(channel,(int) duration.getSeconds());
    }

    @Test
    public void testSlowModeInAChannel() {
        AServer server = MockUtils.getServer();
        AChannel aChannel = MockUtils.getTextChannel(server, 5L);
        Duration duration = Duration.ofMinutes(5);
        when(channel.getGuild()).thenReturn(guild);
        when(channelService.getTextChannelFromServerOptional(server.getId(), aChannel.getId())).thenReturn(Optional.of(channel));
        testUnit.setSlowMode(aChannel, duration);
        verify(channelService, times(1)).setSlowModeInChannel(channel,(int) duration.getSeconds());
    }

    @Test
    public void testDisableSlowMode() {
        when(channel.getGuild()).thenReturn(guild);
        testUnit.disableSlowMode(channel);
        verify(channelService, times(1)).setSlowModeInChannel(channel,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTooLongSlowModeInChannel() {
        when(channel.getGuild()).thenReturn(guild);
        Duration duration = Duration.ofHours(24);
        testUnit.setSlowMode(channel, duration);
    }

    @Test(expected = ChannelNotInGuildException.class)
    public void testSlowModeInAChannelNotFound() {
        AServer server = MockUtils.getServer();
        AChannel aChannel = MockUtils.getTextChannel(server, 5L);
        Duration duration = Duration.ofMinutes(5);
        when(channelService.getTextChannelFromServerOptional(server.getId(), aChannel.getId())).thenReturn(Optional.empty());
        testUnit.setSlowMode(aChannel, duration);
    }
}
