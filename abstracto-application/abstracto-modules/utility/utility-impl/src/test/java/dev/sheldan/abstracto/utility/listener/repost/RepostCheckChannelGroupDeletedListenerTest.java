package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.utility.service.management.RepostCheckChannelGroupManagement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static dev.sheldan.abstracto.utility.service.RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostCheckChannelGroupDeletedListenerTest {

    @InjectMocks
    private RepostCheckChannelGroupDeletedListener testUnit;

    @Mock
    private  RepostCheckChannelGroupManagement checkChannelGroupManagement;

    @Mock
    private AChannelGroup channelGroup;

    @Mock
    private ChannelGroupType channelGroupType;

    private static final String INCORRECT_TYPE = "incorrectType";

    @Test
    public void testChannelGroupDeleted() {
        when(channelGroup.getChannelGroupType()).thenReturn(channelGroupType);
        when(channelGroupType.getGroupTypeKey()).thenReturn(REPOST_CHECK_CHANNEL_GROUP_TYPE);
        testUnit.channelGroupDeleted(channelGroup);
        verify(checkChannelGroupManagement, times(1)).deleteRepostCheckChannelGroup(channelGroup);
    }

    @Test
    public void testChannelGroupDeletedIncorrectType() {
        when(channelGroup.getChannelGroupType()).thenReturn(channelGroupType);
        when(channelGroupType.getGroupTypeKey()).thenReturn(INCORRECT_TYPE);
        testUnit.channelGroupDeleted(channelGroup);
        verify(checkChannelGroupManagement, times(0)).deleteRepostCheckChannelGroup(channelGroup);
    }
}
