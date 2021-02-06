package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AllowedMention;
import dev.sheldan.abstracto.core.repository.AllowedMentionRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllowedMentionManagementServiceBeanTest {

    @InjectMocks
    private AllowedMentionManagementServiceBean testUnit;

    @Mock
    private AllowedMentionRepository allowedMentionRepository;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private AllowedMention allowedMention;

    @Mock
    private AServer server;

    private static final Long SERVER_ID = 4L;

    @Test
    public void getCustomAllowedMentionForIdExisting() {
        when(allowedMentionRepository.findById(SERVER_ID)).thenReturn(Optional.of(allowedMention));
        Optional<AllowedMention> foundMentionOptional = testUnit.getCustomAllowedMentionFor(SERVER_ID);
        Assert.assertTrue(foundMentionOptional.isPresent());
        foundMentionOptional.ifPresent(foundMention -> Assert.assertEquals(allowedMention, foundMention));
    }

    @Test
    public void getCustomAllowedMentionForIdNotExisting() {
        when(allowedMentionRepository.findById(SERVER_ID)).thenReturn(Optional.empty());
        Optional<AllowedMention> foundMentionOptional = testUnit.getCustomAllowedMentionFor(SERVER_ID);
        Assert.assertFalse(foundMentionOptional.isPresent());
    }

    @Test
    public void testGetCustomAllowedMentionForServer() {
        when(allowedMentionRepository.findByServer(server)).thenReturn(Optional.of(allowedMention));
        Optional<AllowedMention> foundMentionOptional = testUnit.getCustomAllowedMentionFor(server);
        Assert.assertTrue(foundMentionOptional.isPresent());
        foundMentionOptional.ifPresent(foundMention -> Assert.assertEquals(allowedMention, foundMention));
    }

    @Test
    public void hasCustomAllowedMention() {
        when(allowedMentionRepository.findById(SERVER_ID)).thenReturn(Optional.of(allowedMention));
        Assert.assertTrue(testUnit.hasCustomAllowedMention(SERVER_ID));
    }

    @Test
    public void createCustomAllowedMention() {
        when(serverManagementService.loadOrCreate(SERVER_ID)).thenReturn(server);
        AllowedMention createdMention = testUnit.createCustomAllowedMention(SERVER_ID, allowedMention);
        ArgumentCaptor<AllowedMention> mentionCaptor = ArgumentCaptor.forClass(AllowedMention.class);
        verify(allowedMentionRepository, times(1)).save(mentionCaptor.capture());
        Assert.assertEquals(createdMention, mentionCaptor.getValue());
        Assert.assertEquals(server, createdMention.getServer());
    }

    @Test
    public void deleteCustomAllowedMention() {
        testUnit.deleteCustomAllowedMention(SERVER_ID);
        verify(allowedMentionRepository, times(1)).deleteById(SERVER_ID);
    }
}