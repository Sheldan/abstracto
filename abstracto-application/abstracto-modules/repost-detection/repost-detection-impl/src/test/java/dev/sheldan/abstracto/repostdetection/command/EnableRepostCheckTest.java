package dev.sheldan.abstracto.repostdetection.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.repostdetection.service.RepostCheckChannelService;
import dev.sheldan.abstracto.repostdetection.service.RepostServiceBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnableRepostCheckTest {

    @InjectMocks
    private EnableRepostCheck testUnit;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private ChannelGroupManagementService channelGroupManagementService;

    @Mock
    private RepostCheckChannelService repostCheckChannelService;

    @Test
    public void executeCommandWithChannelGroupParameter() {
        AChannelGroup channelGroup = Mockito.mock(AChannelGroup.class);
        AChannelGroup actualChannelGroup = Mockito.mock(AChannelGroup.class);
        Long serverId = 2L;
        AServer server = Mockito.mock(AServer.class);
        String channelGroupName = "test";
        when(channelGroup.getGroupName()).thenReturn(channelGroupName);
        when(serverManagementService.loadServer(serverId)).thenReturn(server);
        when(channelGroupManagementService.findByNameAndServerAndType(channelGroupName, server, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(actualChannelGroup);
        CommandContext channelGroupParameters = CommandTestUtilities.getWithParameters(Arrays.asList(channelGroup));
        when(channelGroupParameters.getGuild().getIdLong()).thenReturn(serverId);
        CommandResult result = testUnit.execute(channelGroupParameters);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(repostCheckChannelService, times(1)).setRepostCheckEnabledForChannelGroup(actualChannelGroup);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
