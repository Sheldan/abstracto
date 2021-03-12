package dev.sheldan.abstracto.repostdetection.listener;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.repostdetection.service.RepostServiceBean;
import dev.sheldan.abstracto.repostdetection.service.management.RepostCheckChannelGroupManagement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostCheckChannelGroupCreatedListenerTest {

    @InjectMocks
    private RepostCheckChannelGroupCreatedListener testUnit;

    @Mock
    private RepostCheckChannelGroupManagement checkChannelGroupManagement;

    @Mock
    private AChannelGroup channelGroup;

    @Mock
    private ChannelGroupType channelGroupType;

    private static final String INCORRECT_TYPE = "incorrectType";

    @Test
    public void testChannelGroupCreated() {
        when(channelGroup.getChannelGroupType()).thenReturn(channelGroupType);
        when(channelGroupType.getGroupTypeKey()).thenReturn(RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE);
        testUnit.channelGroupCreated(channelGroup);
        verify(checkChannelGroupManagement, times(1)).createRepostCheckChannelGroup(channelGroup);
    }

    @Test
    public void testChannelGroupCreatedIncorrectType() {
        when(channelGroup.getChannelGroupType()).thenReturn(channelGroupType);
        when(channelGroupType.getGroupTypeKey()).thenReturn(INCORRECT_TYPE);
        testUnit.channelGroupCreated(channelGroup);
        verify(checkChannelGroupManagement, times(0)).createRepostCheckChannelGroup(channelGroup);
    }
}
