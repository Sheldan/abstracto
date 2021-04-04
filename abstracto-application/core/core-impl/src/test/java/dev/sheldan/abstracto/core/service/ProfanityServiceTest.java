package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;
import dev.sheldan.abstracto.core.service.management.ProfanityGroupManagementService;
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
public class ProfanityServiceTest {
    @InjectMocks
    private ProfanityServiceBean testUnit;

    @Mock
    private ProfanityGroupManagementService profanityGroupManagementService;

    @Mock
    private ProfanityGroup group;

    @Mock
    private ProfanityRegex regex;

    @Mock
    private AServer server;

    private final static String INPUT = "input";
    private final static String REPLACEMENT = "repl";

    private static final Long SERVER_ID = 4L;

    @Test
    public void testEmptyConfig() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(new ArrayList<>());
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanitiesWithDefault(INPUT, SERVER_ID, REPLACEMENT);
        Assert.assertEquals(INPUT, result);
    }

    @Test
    public void testOneGroupNoRegex() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanitiesWithDefault(INPUT, SERVER_ID, REPLACEMENT);
        Assert.assertEquals(INPUT, result);
    }

    @Test
    public void testOneGroupWithOneRegexNotMatching() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        when(group.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(group.getProfanities()).thenReturn(Arrays.asList(regex));
        when(regex.getRegex()).thenReturn("a");
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanitiesWithDefault(INPUT, SERVER_ID, REPLACEMENT);
        Assert.assertEquals(INPUT, result);
    }

    @Test
    public void testOneGroupWithOneRegexMatching() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        when(group.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(group.getProfanities()).thenReturn(Arrays.asList(regex));
        when(regex.getRegex()).thenReturn("input");
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanitiesWithDefault(INPUT, SERVER_ID, REPLACEMENT);
        Assert.assertEquals(REPLACEMENT, result);
    }

    @Test
    public void testOneGroupWithOneRegexMatchingDefinedReplacement() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        when(group.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(group.getProfanities()).thenReturn(Arrays.asList(regex));
        when(regex.getRegex()).thenReturn("input");
        when(regex.getReplacement()).thenReturn(REPLACEMENT);
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanitiesWithDefault(INPUT, SERVER_ID, "");
        Assert.assertEquals(REPLACEMENT, result);
    }

    @Test
    public void testOneGroupWithTwoRegexOneMatching() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        when(group.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        ProfanityRegex regex2 = Mockito.mock(ProfanityRegex.class);
        when(regex2.getRegex()).thenReturn("asdf");
        when(group.getProfanities()).thenReturn(Arrays.asList(regex, regex2));
        when(regex.getRegex()).thenReturn("input");
        when(regex.getReplacement()).thenReturn(REPLACEMENT);
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanitiesWithDefault(INPUT, SERVER_ID, "");
        Assert.assertEquals(REPLACEMENT, result);
    }

    /**
     * This scenario is not desired, generally, the outputs should be independent, because the user cannot define the order in which
     * the regexes are applied
     */
    @Test
    public void testOneGroupWithTwoRegexBothMatching() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        when(group.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        ProfanityRegex regex2 = Mockito.mock(ProfanityRegex.class);
        when(regex2.getRegex()).thenReturn(REPLACEMENT);
        String finalString = "FINAL";
        when(regex2.getReplacement()).thenReturn(finalString);
        when(group.getProfanities()).thenReturn(Arrays.asList(regex, regex2));
        when(regex.getRegex()).thenReturn("input");
        when(regex.getReplacement()).thenReturn(REPLACEMENT);
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanitiesWithDefault(INPUT, SERVER_ID, "");
        Assert.assertEquals(finalString, result);
    }

    @Test
    public void testOneGroupWithOneRegexMatchingAndFixReplacement() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        when(group.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(group.getProfanities()).thenReturn(Arrays.asList(regex));
        when(regex.getRegex()).thenReturn("input");
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanities(INPUT, SERVER_ID, REPLACEMENT);
        Assert.assertEquals(REPLACEMENT, result);
    }

    @Test
    public void testOneGroupWithOneRegexMatchingNoProvidedReplacement() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        when(group.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(group.getProfanities()).thenReturn(Arrays.asList(regex));
        when(regex.getRegex()).thenReturn("input");
        when(regex.getReplacement()).thenReturn(REPLACEMENT);
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanities(INPUT, SERVER_ID);
        Assert.assertEquals(REPLACEMENT, result);
    }

    @Test
    public void testOneGroupWithOneRegexMatchingUsingProvidedReplacement() {
        when(profanityGroupManagementService.getAllGroups()).thenReturn(Arrays.asList(group));
        when(group.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(group.getProfanities()).thenReturn(Arrays.asList(regex));
        when(regex.getRegex()).thenReturn("input");
        testUnit.reloadRegex();
        String result = testUnit.replaceProfanities(INPUT, SERVER_ID);
        Assert.assertEquals("", result);
    }
}
