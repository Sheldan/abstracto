package dev.sheldan.abstracto.utility.converter;

import dev.sheldan.abstracto.core.models.FullChannel;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.utility.models.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.utility.models.template.commands.RepostCheckChannelGroupDisplayModel;
import dev.sheldan.abstracto.utility.models.template.commands.RepostCheckChannelsModel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RepostCheckChannelModelConverterTest {

    @InjectMocks
    private RepostCheckChannelModelConverter testUnit;

    @Mock
    private BotService botService;

    @Mock
    private Guild guild;

    @Test
    public void testConvertEmptyList() {
        RepostCheckChannelsModel model = testUnit.fromRepostCheckChannelGroups(new ArrayList<>(), guild);
        Assert.assertEquals(0, model.getRepostCheckChannelGroups().size());
    }

    @Test
    public void testConvertChannelGroupNoChannels() {
        RepostCheckChannelGroup element = Mockito.mock(RepostCheckChannelGroup.class);
        AChannelGroup group = Mockito.mock(AChannelGroup.class);
        when(element.getChannelGroup()).thenReturn(group);
        when(group.getChannels()).thenReturn(new ArrayList<>());
        RepostCheckChannelsModel model = testUnit.fromRepostCheckChannelGroups(Arrays.asList(element), guild);
        Assert.assertEquals(1, model.getRepostCheckChannelGroups().size());
        RepostCheckChannelGroupDisplayModel displayModel = model.getRepostCheckChannelGroups().get(0);
        Assert.assertEquals(element, displayModel.getChannelGroup());
        Assert.assertEquals(0, displayModel.getChannels().size());
    }

    @Test
    public void testConvertChannelGroupWithChannelsOneDeleted() {
        RepostCheckChannelGroup element = Mockito.mock(RepostCheckChannelGroup.class);
        AChannelGroup group = Mockito.mock(AChannelGroup.class);
        when(element.getChannelGroup()).thenReturn(group);
        Long channelId1 = 1L;
        AChannel channel1 = Mockito.mock(AChannel.class);
        when(channel1.getId()).thenReturn(channelId1);
        TextChannel textChannel = Mockito.mock(TextChannel.class);
        when(botService.getTextChannelFromServerNullable(guild, channelId1)).thenReturn(textChannel);
        Long channelId2 = 2L;
        AChannel channel2 = Mockito.mock(AChannel.class);
        when(channel2.getId()).thenReturn(channelId2);
        when(botService.getTextChannelFromServerNullable(guild, channelId2)).thenReturn(null);
        when(group.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        RepostCheckChannelsModel model = testUnit.fromRepostCheckChannelGroups(Arrays.asList(element), guild);
        Assert.assertEquals(1, model.getRepostCheckChannelGroups().size());
        RepostCheckChannelGroupDisplayModel displayModel = model.getRepostCheckChannelGroups().get(0);
        Assert.assertEquals(element, displayModel.getChannelGroup());
        Assert.assertEquals(2, displayModel.getChannels().size());
        FullChannel firstFullChannel = displayModel.getChannels().get(0);
        Assert.assertEquals(channel1, firstFullChannel.getChannel());
        Assert.assertEquals(textChannel, firstFullChannel.getServerChannel());
        FullChannel secondFullChannel = displayModel.getChannels().get(1);
        Assert.assertEquals(channel2, secondFullChannel.getChannel());
        Assert.assertNull(secondFullChannel.getServerChannel());
    }
}
