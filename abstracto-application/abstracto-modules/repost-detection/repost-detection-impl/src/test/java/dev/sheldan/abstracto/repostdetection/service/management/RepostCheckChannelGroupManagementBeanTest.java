package dev.sheldan.abstracto.repostdetection.service.management;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.repostdetection.exception.RepostCheckChannelGroupNotFoundException;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.repostdetection.repository.RepostCheckChannelRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostCheckChannelGroupManagementBeanTest {

    @InjectMocks
    private RepostCheckChannelGroupManagementBean testUnit;

    @Mock
    private RepostCheckChannelRepository repository;

    @Mock
    private RepostCheckChannelGroup checkChannelGroup;

    @Mock
    private AChannelGroup aChannelGroup;

    @Captor
    private ArgumentCaptor<RepostCheckChannelGroup> checkChannelGroupArgumentCaptor;

    private static final Long CHANNEL_GROUP_ID = 1L;

    @Test(expected = RepostCheckChannelGroupNotFoundException.class)
    public void testLoadRepostChannelGroupByIdNotFound() {
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.empty());
        testUnit.loadRepostChannelGroupById(CHANNEL_GROUP_ID);
    }

    @Test
    public void testLoadRepostChannelGroupById() {
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.of(checkChannelGroup));
        RepostCheckChannelGroup resultChannelGroup = testUnit.loadRepostChannelGroupById(CHANNEL_GROUP_ID);
        Assert.assertEquals(checkChannelGroup, resultChannelGroup);
    }

    @Test
    public void testLoadRepostChannelGroupByIdOptional() {
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.of(checkChannelGroup));
        Optional<RepostCheckChannelGroup> resultChannelGroupOptional = testUnit.loadRepostChanelGroupByIdOptional(CHANNEL_GROUP_ID);
        Assert.assertTrue(resultChannelGroupOptional.isPresent());
        resultChannelGroupOptional.ifPresent(repostCheckChannelGroup -> Assert.assertEquals(checkChannelGroup, repostCheckChannelGroup));
    }

    @Test
    public void testRepostCheckChannelGroupExistsNot() {
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.empty());
        Assert.assertFalse(testUnit.repostCheckChannelGroupExists(CHANNEL_GROUP_ID));
    }

    @Test
    public void testRepostCheckChannelGroupExists() {
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.of(checkChannelGroup));
        Assert.assertTrue(testUnit.repostCheckChannelGroupExists(CHANNEL_GROUP_ID));
    }

    @Test
    public void testLoadRepostChannelGroupByChannelGroupOptionalPresent() {
        when(aChannelGroup.getId()).thenReturn(CHANNEL_GROUP_ID);
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.of(checkChannelGroup));
        Optional<RepostCheckChannelGroup> resultChannelGroupOptional = testUnit.loadRepostChannelGroupByChannelGroupOptional(aChannelGroup);
        Assert.assertTrue(resultChannelGroupOptional.isPresent());
        resultChannelGroupOptional.ifPresent(repostCheckChannelGroup -> Assert.assertEquals(checkChannelGroup, repostCheckChannelGroup));
    }

    @Test
    public void testLoadRepostChannelGroupByChannelGroupOptionalNotPresent() {
        when(aChannelGroup.getId()).thenReturn(CHANNEL_GROUP_ID);
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.empty());
        Optional<RepostCheckChannelGroup> resultChannelGroupOptional = testUnit.loadRepostChannelGroupByChannelGroupOptional(aChannelGroup);
        Assert.assertFalse(resultChannelGroupOptional.isPresent());
    }

    @Test
    public void testLoadRepostChannelGroupByChannelGroup() {
        when(aChannelGroup.getId()).thenReturn(CHANNEL_GROUP_ID);
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.of(checkChannelGroup));
        RepostCheckChannelGroup repostCheckChannelGroup = testUnit.loadRepostChannelGroupByChannelGroup(aChannelGroup);
        Assert.assertEquals(checkChannelGroup, repostCheckChannelGroup);
    }

    @Test(expected = RepostCheckChannelGroupNotFoundException.class)
    public void testLoadRepostChannelGroupByChannelGroupNotFound() {
        when(aChannelGroup.getId()).thenReturn(CHANNEL_GROUP_ID);
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.empty());
        testUnit.loadRepostChannelGroupByChannelGroup(aChannelGroup);
    }

    @Test
    public void testCreateRepostCheckChannelGroup() {
        when(aChannelGroup.getId()).thenReturn(CHANNEL_GROUP_ID);
        RepostCheckChannelGroup savedGroup = Mockito.mock(RepostCheckChannelGroup.class);
        when(repository.save(checkChannelGroupArgumentCaptor.capture())).thenReturn(savedGroup);
        RepostCheckChannelGroup createdCheckChannelGroup = testUnit.createRepostCheckChannelGroup(aChannelGroup);
        Assert.assertEquals(savedGroup, createdCheckChannelGroup);
        RepostCheckChannelGroup capturedGroup = checkChannelGroupArgumentCaptor.getValue();
        Assert.assertTrue(capturedGroup.getCheckEnabled());
        Assert.assertEquals(CHANNEL_GROUP_ID, capturedGroup.getId());
        Assert.assertEquals(aChannelGroup, capturedGroup.getChannelGroup());
    }

    @Test(expected = RepostCheckChannelGroupNotFoundException.class)
    public void testDeleteRepostCheckChannelGroupNotExisting(){
        when(aChannelGroup.getId()).thenReturn(CHANNEL_GROUP_ID);
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.empty());
        testUnit.deleteRepostCheckChannelGroup(aChannelGroup);
    }

    @Test
    public void testDeleteRepostCheckChannelGroup(){
        when(aChannelGroup.getId()).thenReturn(CHANNEL_GROUP_ID);
        when(repository.findById(CHANNEL_GROUP_ID)).thenReturn(Optional.of(checkChannelGroup));
        testUnit.deleteRepostCheckChannelGroup(aChannelGroup);
        verify(repository, times(1)).delete(checkChannelGroup);
    }
}
