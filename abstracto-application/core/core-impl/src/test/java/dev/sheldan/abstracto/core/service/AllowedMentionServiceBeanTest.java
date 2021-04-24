package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.AllowedMentionConfig;
import dev.sheldan.abstracto.core.exception.UnknownMentionTypeException;
import dev.sheldan.abstracto.core.models.database.AllowedMention;
import dev.sheldan.abstracto.core.service.management.AllowedMentionManagementService;
import net.dv8tion.jda.api.entities.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllowedMentionServiceBeanTest {

    @InjectMocks
    private AllowedMentionServiceBean testUnit;

    @Mock
    private AllowedMentionConfig allowedMentionConfig;

    @Mock
    private AllowedMentionManagementService allowedMentionManagementService;

    @Mock
    private AllowedMention allowedMention;

    private static final Long SERVER_ID = 4L;

    @Before
    public void setup() {
        testUnit.postConstruct();
    }

    @Test
    public void allMentionsAllowedDefaultAll() {
        allDefaultConfigAllowed();
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.empty());
        Assert.assertTrue(testUnit.allMentionsAllowed(SERVER_ID));
    }

    @Test
    public void allMentionsAllowedDefaultNotAll() {
        when(allowedMentionConfig.getEveryone()).thenReturn(true);
        when(allowedMentionConfig.getRole()).thenReturn(false);
        when(allowedMentionConfig.getUser()).thenReturn(true);
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.empty());
        Assert.assertFalse(testUnit.allMentionsAllowed(SERVER_ID));
    }

    @Test
    public void allMentionsAllowedCustomAll() {
        when(allowedMention.allAllowed()).thenReturn(true);
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.of(allowedMention));
        Assert.assertTrue(testUnit.allMentionsAllowed(SERVER_ID));
    }

    @Test
    public void getAllowedMentionTypesForServerEmpty() {
        allDefaultConfigAllowed();
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.empty());
        Set<Message.MentionType> allowedMentions = testUnit.getAllowedMentionTypesForServer(SERVER_ID);
        Assert.assertNull(allowedMentions);
    }

    @Test
    public void getAllowedMentionTypesAllAllowed() {
        when(allowedMentionConfig.getEveryone()).thenReturn(true);
        when(allowedMentionConfig.getRole()).thenReturn(false);
        when(allowedMentionConfig.getUser()).thenReturn(false);
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.empty());
        Set<Message.MentionType> allowedMentions = testUnit.getAllowedMentionTypesForServer(SERVER_ID);
        Assert.assertEquals(1, allowedMentions.size());
        Assert.assertEquals(Message.MentionType.EVERYONE, allowedMentions.iterator().next());
    }

    @Test
    public void getAllowedMentionTypesAllDisallowed() {
        when(allowedMentionConfig.getEveryone()).thenReturn(false);
        when(allowedMentionConfig.getRole()).thenReturn(false);
        when(allowedMentionConfig.getUser()).thenReturn(false);
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.empty());
        Set<Message.MentionType> allowedMentions = testUnit.getAllowedMentionTypesForServer(SERVER_ID);
        Assert.assertEquals(0, allowedMentions.size());
    }

    @Test
    public void allowMentionForServerWithExistingCustom() {
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.of(allowedMention));
        testUnit.allowMentionForServer(Message.MentionType.EVERYONE, SERVER_ID);
        verify(allowedMention, times(1)).setEveryone(true);
    }

    @Test
    public void allowMentionForServerWithNewCustom() {
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.empty());
        testUnit.allowMentionForServer(Message.MentionType.EVERYONE, SERVER_ID);
        ArgumentCaptor<AllowedMention> argumentCaptor = ArgumentCaptor.forClass(AllowedMention.class);
        verify(allowedMentionManagementService, times(1)).createCustomAllowedMention(eq(SERVER_ID), argumentCaptor.capture());
        AllowedMention creationArgument = argumentCaptor.getValue();
        Assert.assertTrue(creationArgument.getEveryone());
    }

    @Test
    public void disAllowMentionForServer() {
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.empty());
        testUnit.disAllowMentionForServer(Message.MentionType.EVERYONE, SERVER_ID);
        ArgumentCaptor<AllowedMention> argumentCaptor = ArgumentCaptor.forClass(AllowedMention.class);
        verify(allowedMentionManagementService, times(1)).createCustomAllowedMention(eq(SERVER_ID), argumentCaptor.capture());
        AllowedMention creationArgument = argumentCaptor.getValue();
        Assert.assertFalse(creationArgument.getEveryone());
    }

    @Test
    public void getDefaultAllowedMention() {
        when(allowedMentionConfig.getEveryone()).thenReturn(true);
        AllowedMention defaultMention = testUnit.getDefaultAllowedMention();
        Assert.assertEquals(true, defaultMention.getEveryone());
    }

    @Test
    public void getEffectiveAllowedMentionWithCustom() {
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.of(allowedMention));
        AllowedMention effectiveMention = testUnit.getEffectiveAllowedMention(SERVER_ID);
        Assert.assertEquals(allowedMention, effectiveMention);
    }

    @Test
    public void getEffectiveAllowedMentionNoCustom() {
        when(allowedMentionConfig.getEveryone()).thenReturn(true);
        when(allowedMentionManagementService.getCustomAllowedMentionFor(SERVER_ID)).thenReturn(Optional.empty());
        AllowedMention effectiveMention = testUnit.getEffectiveAllowedMention(SERVER_ID);
        Assert.assertNull(effectiveMention.getServer());
        Assert.assertTrue(effectiveMention.getEveryone());
    }

    @Test
    public void getMentionTypeFromString() {
        Message.MentionType result = testUnit.getMentionTypeFromString("everyone");
        Assert.assertEquals(Message.MentionType.EVERYONE, result);
    }

    @Test
    public void getMentionTypeFromStringIgnoreCase() {
        Message.MentionType result = testUnit.getMentionTypeFromString("eVeRyOnE");
        Assert.assertEquals(Message.MentionType.EVERYONE, result);
    }

    @Test(expected = UnknownMentionTypeException.class)
    public void getUnknownMentionType() {
        testUnit.getMentionTypeFromString("test");
    }

    private void allDefaultConfigAllowed() {
        when(allowedMentionConfig.getEveryone()).thenReturn(true);
        when(allowedMentionConfig.getRole()).thenReturn(true);
        when(allowedMentionConfig.getUser()).thenReturn(true);
    }

}