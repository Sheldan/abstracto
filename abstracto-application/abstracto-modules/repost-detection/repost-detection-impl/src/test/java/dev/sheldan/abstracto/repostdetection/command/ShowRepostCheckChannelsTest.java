package dev.sheldan.abstracto.repostdetection.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.repostdetection.converter.RepostCheckChannelModelConverter;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.repostdetection.model.template.RepostCheckChannelsModel;
import dev.sheldan.abstracto.repostdetection.service.RepostCheckChannelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShowRepostCheckChannelsTest {

    @InjectMocks
    private ShowRepostCheckChannels testUnit;

    @Mock
    private RepostCheckChannelModelConverter converter;

    @Mock
    private RepostCheckChannelService checkChannelService;

    @Mock
    private ChannelService channelService;

    @Test
    public void testExecuteCommand() {
        Long serverId = 1L;
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        when(noParameters.getGuild().getIdLong()).thenReturn(serverId);
        RepostCheckChannelGroup group = Mockito.mock(RepostCheckChannelGroup.class);
        List<RepostCheckChannelGroup> groupList = Arrays.asList(group);
        when(checkChannelService.getChannelGroupsWithEnabledCheck(serverId)).thenReturn(groupList);
        RepostCheckChannelsModel model = Mockito.mock(RepostCheckChannelsModel.class);
        when(converter.fromRepostCheckChannelGroups(groupList, noParameters.getGuild())).thenReturn(model);
        CompletableFuture<CommandResult> futureResult = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(futureResult);
        verify(channelService, times(1)).sendEmbedTemplateInMessageChannel(ShowRepostCheckChannels.SHOW_REPOST_CHECK_CHANNELS_RESPONSE_TEMPLATE_KEY, model, noParameters.getChannel());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }


}
