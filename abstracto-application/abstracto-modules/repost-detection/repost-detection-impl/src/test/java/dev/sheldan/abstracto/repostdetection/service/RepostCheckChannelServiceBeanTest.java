package dev.sheldan.abstracto.repostdetection.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.repostdetection.exception.RepostCheckChannelGroupNotFoundException;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.repostdetection.service.management.RepostCheckChannelGroupManagement;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostCheckChannelServiceBeanTest {

    public static final long SERVER_ID = 1L;
    @InjectMocks
    private RepostCheckChannelServiceBean testUnit;

    @Mock
    private RepostCheckChannelGroupManagement repostCheckChannelManagement;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private ChannelGroupService channelGroupService;

    @Mock
    private ChannelGroupManagementService channelGroupManagementService;

    @Test
    public void testSetCheckEnabledForCheckGroup() {
        RepostCheckChannelGroup group = Mockito.mock(RepostCheckChannelGroup.class);
        testUnit.setRepostCheckEnabledForChannelGroup(group);
        verify(group, times(1)).setCheckEnabled(true);
    }

    @Test
    public void testSetCheckEnabledForChannelGroup() {
        AChannelGroup channelGroup = Mockito.mock(AChannelGroup.class);
        RepostCheckChannelGroup group = Mockito.mock(RepostCheckChannelGroup.class);
        when(repostCheckChannelManagement.loadRepostChannelGroupByChannelGroup(channelGroup)).thenReturn(group);
        testUnit.setRepostCheckEnabledForChannelGroup(channelGroup);
        verify(group, times(1)).setCheckEnabled(true);
    }

    @Test
    public void testSetCheckDisabledForCheckGroup() {
        RepostCheckChannelGroup group = Mockito.mock(RepostCheckChannelGroup.class);
        testUnit.setRepostCheckDisabledForChannelGroup(group);
        verify(group, times(1)).setCheckEnabled(false);
    }

    @Test
    public void testSetCheckDisabledForChannelGroup() {
        AChannelGroup channelGroup = Mockito.mock(AChannelGroup.class);
        RepostCheckChannelGroup group = Mockito.mock(RepostCheckChannelGroup.class);
        when(repostCheckChannelManagement.loadRepostChannelGroupByChannelGroup(channelGroup)).thenReturn(group);
        testUnit.setRepostCheckDisabledForChannelGroup(channelGroup);
        verify(group, times(1)).setCheckEnabled(false);
    }

    @Test
    public void testCheckDuplicateCheckEnabledAChannel() {
        AChannel channel = mock(AChannel.class);
        setupEnabledCheck(channel);
        boolean enabled = testUnit.duplicateCheckEnabledForChannel(channel);
        Assert.assertTrue(enabled);
    }

    @Test
    public void testCheckDuplicateCheckDisabledAChannel() {
        AChannel channel = mock(AChannel.class);
        setupDisabledCheck(channel);
        boolean enabled = testUnit.duplicateCheckEnabledForChannel(channel);
        Assert.assertFalse(enabled);
    }

    @Test
    public void testCheckDuplicateCheckEnabledTextChannel() {
        TextChannel textChannel = Mockito.mock(TextChannel.class);
        AChannel channel = Mockito.mock(AChannel.class);
        Long channelID = 3L;
        when(textChannel.getIdLong()).thenReturn(channelID);
        when(channelManagementService.loadChannel(channelID)).thenReturn(channel);
        setupEnabledCheck(channel);
        boolean enabled = testUnit.duplicateCheckEnabledForChannel(textChannel);
        Assert.assertTrue(enabled);
    }

    @Test
    public void testCheckDuplicateCheckDisabledTextChannel() {
        TextChannel textChannel = Mockito.mock(TextChannel.class);
        AChannel channel = Mockito.mock(AChannel.class);
        Long channelID = 3L;
        when(textChannel.getIdLong()).thenReturn(channelID);
        when(channelManagementService.loadChannel(channelID)).thenReturn(channel);
        setupDisabledCheck(channel);
        boolean enabled = testUnit.duplicateCheckEnabledForChannel(textChannel);
        Assert.assertFalse(enabled);
    }

    @Test
    public void testCheckDuplicateCheckNoChannelGroupsChannel() {
        AChannel channel = Mockito.mock(AChannel.class);
        when(channelGroupService.getChannelGroupsOfChannelWithType(channel, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(new ArrayList<>());
        boolean enabled = testUnit.duplicateCheckEnabledForChannel(channel);
        Assert.assertFalse(enabled);
    }

    @Test
    public void testCheckDuplicateCheckNoChannelGroupsTextChannel() {
        TextChannel textChannel = Mockito.mock(TextChannel.class);
        AChannel channel = Mockito.mock(AChannel.class);
        Long channelID = 3L;
        when(textChannel.getIdLong()).thenReturn(channelID);
        when(channelManagementService.loadChannel(channelID)).thenReturn(channel);
        when(channelGroupService.getChannelGroupsOfChannelWithType(channel, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(new ArrayList<>());
        boolean enabled = testUnit.duplicateCheckEnabledForChannel(textChannel);
        Assert.assertFalse(enabled);
    }

    public void setupEnabledCheck(AChannel channel) {
        setupRepostEnabledTest(channel, true, false);
    }

    public void setupDisabledCheck(AChannel channel) {
        setupRepostEnabledTest(channel, false, false);
    }

    public void setupRepostEnabledTest(AChannel channel, boolean firstGroupState, boolean secondGroupSate) {
        AChannelGroup firstGroup = Mockito.mock(AChannelGroup.class);
        Long firstChannelGroupId = 1L;
        Long secondChannelGroupId = 2L;
        when(firstGroup.getId()).thenReturn(firstChannelGroupId);
        AChannelGroup secondGroup = Mockito.mock(AChannelGroup.class);
        when(secondGroup.getId()).thenReturn(secondChannelGroupId);
        RepostCheckChannelGroup firstRepostCheckChannelGroup = Mockito.mock(RepostCheckChannelGroup.class);
        when(firstRepostCheckChannelGroup.getCheckEnabled()).thenReturn(secondGroupSate);
        RepostCheckChannelGroup secondRepostCheckChannelGroup = Mockito.mock(RepostCheckChannelGroup.class);
        when(secondRepostCheckChannelGroup.getCheckEnabled()).thenReturn(firstGroupState);
        when(repostCheckChannelManagement.loadRepostChannelGroupById(firstChannelGroupId)).thenReturn(firstRepostCheckChannelGroup);
        when(repostCheckChannelManagement.loadRepostChannelGroupById(secondChannelGroupId)).thenReturn(secondRepostCheckChannelGroup);
        when(channelGroupService.getChannelGroupsOfChannelWithType(channel, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(Arrays.asList(firstGroup, secondGroup));
    }

    @Test
    public void testGetRepostCheckChannelGroupsForServerNoChannelGroups() {
        when(channelGroupManagementService.findAllInServerWithType(SERVER_ID, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(new ArrayList<>());
        List<RepostCheckChannelGroup> groups = testUnit.getRepostCheckChannelGroupsForServer(SERVER_ID);
        Assert.assertEquals(0, groups.size());
    }

    @Test
    public void testGetRepostCheckChannelGroupsForServerTwoChannelGroups() {
        List<RepostCheckChannelGroup> mockedGroups = setupGetRepostCheckChannelGroupsTest();
        List<RepostCheckChannelGroup> groups = testUnit.getRepostCheckChannelGroupsForServer(SERVER_ID);
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(mockedGroups.get(0), groups.get(0));
        Assert.assertEquals(mockedGroups.get(1), groups.get(1));
    }

    @Test
    public void testGetRepostCheckChannelGroupsForServerTwoChannelGroupsAServer() {
        AServer server = Mockito.mock(AServer.class);
        when(server.getId()).thenReturn(SERVER_ID);
        List<RepostCheckChannelGroup> mockedGroups = setupGetRepostCheckChannelGroupsTest();
        List<RepostCheckChannelGroup> groups = testUnit.getRepostCheckChannelGroupsForServer(server);
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(mockedGroups.get(0), groups.get(0));
        Assert.assertEquals(mockedGroups.get(1), groups.get(1));
    }

    public List<RepostCheckChannelGroup> setupGetRepostCheckChannelGroupsTest() {
        Long firstChannelGroupId = 1L;
        Long secondChannelGroupId = 2L;
        AChannelGroup firstGroup = Mockito.mock(AChannelGroup.class);
        when(firstGroup.getId()).thenReturn(firstChannelGroupId);
        AChannelGroup secondGroup = Mockito.mock(AChannelGroup.class);
        when(secondGroup.getId()).thenReturn(secondChannelGroupId);
        when(channelGroupManagementService.findAllInServerWithType(SERVER_ID, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(Arrays.asList(firstGroup, secondGroup));
        RepostCheckChannelGroup firstRepostCheckChannelGroup = Mockito.mock(RepostCheckChannelGroup.class);
        RepostCheckChannelGroup secondRepostCheckChannelGroup = Mockito.mock(RepostCheckChannelGroup.class);
        when(repostCheckChannelManagement.loadRepostChannelGroupById(firstChannelGroupId)).thenReturn(firstRepostCheckChannelGroup);
        when(repostCheckChannelManagement.loadRepostChannelGroupById(secondChannelGroupId)).thenReturn(secondRepostCheckChannelGroup);
        return Arrays.asList(firstRepostCheckChannelGroup, secondRepostCheckChannelGroup);
    }

    @Test(expected = RepostCheckChannelGroupNotFoundException.class)
    public void testGetRepostCheckChannelGroupsNotExisting() {
        Long channelGroupId = 1L;
        AChannelGroup firstGroup = Mockito.mock(AChannelGroup.class);
        when(firstGroup.getId()).thenReturn(channelGroupId);
        when(channelGroupManagementService.findAllInServerWithType(SERVER_ID, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(Arrays.asList(firstGroup));
        when(repostCheckChannelManagement.loadRepostChannelGroupById(channelGroupId)).thenThrow(new RepostCheckChannelGroupNotFoundException(channelGroupId));
        testUnit.getRepostCheckChannelGroupsForServer(SERVER_ID);
    }

    @Test
    public void testGetChannelGroupsWithEnabledCheckNoGroups() {
        when(channelGroupManagementService.findAllInServerWithType(SERVER_ID, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(new ArrayList<>());
        List<RepostCheckChannelGroup> groups = testUnit.getChannelGroupsWithEnabledCheck(SERVER_ID);
        Assert.assertEquals(0, groups.size());
    }

    @Test
    public void testGetChannelGroupsWithEnabledCheckAServer() {
        AServer server = Mockito.mock(AServer.class);
        when(server.getId()).thenReturn(SERVER_ID);
        List<RepostCheckChannelGroup> checkGroups = setupGetChannelGroupsTest();
        List<RepostCheckChannelGroup> groups = testUnit.getChannelGroupsWithEnabledCheck(server);
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(checkGroups.get(0), groups.get(0));
    }

    @Test
    public void testGetChannelGroupsWithEnabledCheck() {
        List<RepostCheckChannelGroup> checkGroups = setupGetChannelGroupsTest();
        List<RepostCheckChannelGroup> groups = testUnit.getChannelGroupsWithEnabledCheck(SERVER_ID);
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(checkGroups.get(0), groups.get(0));
    }

    public List<RepostCheckChannelGroup> setupGetChannelGroupsTest() {
        Long firstChannelGroupId = 1L;
        Long secondChannelGroupId = 2L;
        AChannelGroup firstGroup = Mockito.mock(AChannelGroup.class);
        when(firstGroup.getId()).thenReturn(firstChannelGroupId);
        AChannelGroup secondGroup = Mockito.mock(AChannelGroup.class);
        when(secondGroup.getId()).thenReturn(secondChannelGroupId);
        when(channelGroupManagementService.findAllInServerWithType(SERVER_ID, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)).thenReturn(Arrays.asList(firstGroup, secondGroup));
        RepostCheckChannelGroup firstRepostCheckChannelGroup = Mockito.mock(RepostCheckChannelGroup.class);
        when(firstRepostCheckChannelGroup.getCheckEnabled()).thenReturn(true);
        RepostCheckChannelGroup secondRepostCheckChannelGroup = Mockito.mock(RepostCheckChannelGroup.class);
        when(secondRepostCheckChannelGroup.getCheckEnabled()).thenReturn(false);
        when(repostCheckChannelManagement.loadRepostChannelGroupById(firstChannelGroupId)).thenReturn(firstRepostCheckChannelGroup);
        when(repostCheckChannelManagement.loadRepostChannelGroupById(secondChannelGroupId)).thenReturn(secondRepostCheckChannelGroup);
        return Arrays.asList(firstRepostCheckChannelGroup, secondRepostCheckChannelGroup);
    }

}
