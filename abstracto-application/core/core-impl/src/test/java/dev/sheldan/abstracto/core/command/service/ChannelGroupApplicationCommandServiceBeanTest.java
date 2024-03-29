package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static dev.sheldan.abstracto.core.command.CommandConstants.COMMAND_CHANNEL_GROUP_KEY;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelGroupApplicationCommandServiceBeanTest {

    @InjectMocks
    private ChannelGroupCommandServiceBean testUnit;

    @Mock
    private ChannelGroupCommandManagementService channelGroupCommandService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private ACommand command;

    @Mock
    private AChannel channel;

    @Mock
    private AChannel secondChannel;

    @Mock
    private AChannelGroupCommand channelGroupCommand;

    @Mock
    private AChannelGroup channelGroup;

    private static final Long CHANNEL_ID = 4L;
    private static final Long CHANNEL_ID_2 = 2L;

    @Test
    public void testNoChannelGroup() {
        when(channelGroupCommandService.getAllGroupCommandsForCommandWithType(command, COMMAND_CHANNEL_GROUP_KEY)).thenReturn(new ArrayList<>());
        Assert.assertTrue(testUnit.isCommandEnabled(command, channel));
    }

    @Test
    public void testOneDisabledChannelGroup() {
        when(channelGroupCommandService.getAllGroupCommandsForCommandWithType(command, COMMAND_CHANNEL_GROUP_KEY)).thenReturn(Arrays.asList(channelGroupCommand));
        when(channelGroupCommand.getGroup()).thenReturn(channelGroup);
        when(channelGroup.getEnabled()).thenReturn(true);
        when(channelGroup.getChannels()).thenReturn(Arrays.asList(channel));
        when(channelGroupCommand.getEnabled()).thenReturn(false);
        Assert.assertFalse(testUnit.isCommandEnabled(command, channel));
    }

    @Test
    public void testOneEnabledChannelGroup() {
        when(channelGroupCommandService.getAllGroupCommandsForCommandWithType(command, COMMAND_CHANNEL_GROUP_KEY)).thenReturn(Arrays.asList(channelGroupCommand));
        when(channelGroupCommand.getGroup()).thenReturn(channelGroup);
        when(channelGroup.getEnabled()).thenReturn(true);
        when(channelGroup.getChannels()).thenReturn(Arrays.asList(channel));
        when(channelGroupCommand.getEnabled()).thenReturn(true);
        Assert.assertTrue(testUnit.isCommandEnabled(command, channel));
    }

    @Test
    public void testDisabledInOneGroupChannelIsNotPartOf() {
        when(channelGroupCommandService.getAllGroupCommandsForCommandWithType(command, COMMAND_CHANNEL_GROUP_KEY)).thenReturn(Arrays.asList(channelGroupCommand));
        when(channelGroupCommand.getGroup()).thenReturn(channelGroup);
        when(channelGroup.getEnabled()).thenReturn(true);
        when(channelGroup.getChannels()).thenReturn(Arrays.asList(secondChannel));
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(secondChannel.getId()).thenReturn(CHANNEL_ID_2);
        Assert.assertTrue(testUnit.isCommandEnabled(command, channel));
    }
}
