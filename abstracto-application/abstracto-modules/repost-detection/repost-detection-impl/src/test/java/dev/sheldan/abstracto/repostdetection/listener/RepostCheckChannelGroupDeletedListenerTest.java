package dev.sheldan.abstracto.repostdetection.listener;

import dev.sheldan.abstracto.core.command.exception.ChannelGroupNotFoundByIdException;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupDeletedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.repostdetection.service.RepostServiceBean;
import dev.sheldan.abstracto.repostdetection.service.management.RepostCheckChannelGroupManagement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostCheckChannelGroupDeletedListenerTest {

    @InjectMocks
    private RepostCheckChannelGroupDeletedListener testUnit;

    @Mock
    private RepostCheckChannelGroupManagement checkChannelGroupManagement;

    @Mock
    private ChannelGroupManagementService channelGroupManagementService;

    @Mock
    private AChannelGroup channelGroup;

    @Mock
    private ChannelGroupType channelGroupType;

    @Mock
    private ChannelGroupDeletedListenerModel model;

    private static final String INCORRECT_TYPE = "incorrectType";
    private static final Long CHANNEL_GROUP_ID = 1L;

    @Test
    public void testChannelGroupDeleted() {
        when(channelGroup.getChannelGroupType()).thenReturn(channelGroupType);
        when(channelGroupType.getGroupTypeKey()).thenReturn(RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE);
        when(model.getChannelGroupId()).thenReturn(CHANNEL_GROUP_ID);
        when(channelGroupManagementService.findChannelGroupById(CHANNEL_GROUP_ID)).thenReturn(channelGroup);
        testUnit.execute(model);
        verify(checkChannelGroupManagement, times(1)).deleteRepostCheckChannelGroup(channelGroup);
    }

    @Test(expected = ChannelGroupNotFoundByIdException.class)
    public void testChannelGroupNotExisting() {
        when(model.getChannelGroupId()).thenReturn(CHANNEL_GROUP_ID);
        when(channelGroupManagementService.findChannelGroupById(CHANNEL_GROUP_ID)).thenThrow(new ChannelGroupNotFoundByIdException());
        testUnit.execute(model);
    }

    @Test
    public void testChannelGroupDeletedIncorrectType() {
        when(channelGroup.getChannelGroupType()).thenReturn(channelGroupType);
        when(channelGroupType.getGroupTypeKey()).thenReturn(INCORRECT_TYPE);
        when(model.getChannelGroupId()).thenReturn(CHANNEL_GROUP_ID);
        when(channelGroupManagementService.findChannelGroupById(CHANNEL_GROUP_ID)).thenReturn(channelGroup);
        testUnit.execute(model);
        verify(checkChannelGroupManagement, times(0)).deleteRepostCheckChannelGroup(channelGroup);
    }
}
