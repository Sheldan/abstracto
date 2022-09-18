package dev.sheldan.abstracto.entertainment.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureConfig;
import dev.sheldan.abstracto.entertainment.exception.ReactDuplicateCharacterException;
import dev.sheldan.abstracto.entertainment.model.ReactMapping;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EntertainmentServiceBeanTest {
    @InjectMocks
    private EntertainmentServiceBean testUnit;

    @Mock
    private SecureRandom secureRandom;

    @Mock
    private ConfigService configService;

    // requires the org.mockito.plugins.MockMaker file
    @Mock
    private Gson gson;

    @Mock
    private Resource resource;

    @Mock
    private Member member;

    @Mock
    private Member secondMember;

    private static final String INPUT_TEXT = "input";
    private static final int RANDOM_VALUE = 0;

    @Test
    public void testEightBallChoice() {
        when(secureRandom.nextInt(EntertainmentServiceBean.EIGHT_BALL_ANSWER_KEYS.size())).thenReturn(RANDOM_VALUE);
        String chosenKey = testUnit.getEightBallValue(INPUT_TEXT);
        Assert.assertEquals(EntertainmentServiceBean.EIGHT_BALL_ANSWER_KEYS.get(RANDOM_VALUE), chosenKey);
    }

    @Test
    public void testLoveCalc() {
        when(secureRandom.nextInt(100)).thenReturn(RANDOM_VALUE);
        Integer loveCalcValue = testUnit.getLoveCalcValue(INPUT_TEXT, INPUT_TEXT);
        Assert.assertEquals(RANDOM_VALUE, loveCalcValue.intValue());
    }

    @Test
    public void testRoll() {
        executeRollTest(20, 10);
    }

    @Test
    public void testRollOutOfOrderParams() {
        executeRollTest(10, 20);
    }

    private void executeRollTest(int high, int low) {
        when(secureRandom.nextInt(10)).thenReturn(RANDOM_VALUE);
        Integer loveCalcValue = testUnit.calculateRollResult(low, high);
        Assert.assertEquals(Math.min(low, high) + RANDOM_VALUE, loveCalcValue.intValue());
    }

    @Test
    public void testRouletteNoShot() {
        executeRouletteTest(1);
    }

    @Test
    public void testRouletteShot() {
        executeRouletteTest(0);
    }

    private void executeRouletteTest(int randomValue) {
        Long serverId = 3L;
        Member member = Mockito.mock(Member.class);
        Guild guild = Mockito.mock(Guild.class);
        when(guild.getIdLong()).thenReturn(serverId);
        when(member.getGuild()).thenReturn(guild);
        Long sides = 6L;
        when(configService.getLongValueOrConfigDefault(EntertainmentFeatureConfig.ROULETTE_BULLETS_CONFIG_KEY, serverId)).thenReturn(sides);
        when(secureRandom.nextInt(sides.intValue())).thenReturn(randomValue);
        boolean shot = testUnit.executeRoulette(member);
        Assert.assertEquals(randomValue == 0, shot);
    }

    @Test
    public void testTakeChoice(){
        Member member = Mockito.mock(Member.class);
        List<String> choices = Arrays.asList(INPUT_TEXT, INPUT_TEXT + "test");
        when(secureRandom.nextInt(choices.size())).thenReturn(RANDOM_VALUE);
        String choiceTaken = testUnit.takeChoice(choices, member);
        Assert.assertEquals(choices.get(0), choiceTaken);
    }

    @Test
    public void testMocking() {
        Assert.assertEquals("AsDf", testUnit.createMockText("asdf", member, secondMember));
    }

    @Test
    public void testMockingUpperCase() {
        Assert.assertEquals("AsDf", testUnit.createMockText("ASDF", member, secondMember));
    }

    @Test
    public void testMockingNull() {
        Assert.assertEquals("", testUnit.createMockText(null, member, secondMember));
    }

    @Test
    public void testConvertTextToEmojis() throws IOException {
        setupMappings();
        Assert.assertEquals("bceg", testUnit.convertTextToEmojisAsString("asdf", false));
    }

    @Test(expected = ReactDuplicateCharacterException.class)
    public void testConvertTextToEmojisDuplicateReplacement() throws IOException {
        setupMappings();
        testUnit.convertTextToEmojisAsString("aa", false);
    }

    @Test
    public void testConvertTextToEmojisNoReplacementFound() throws IOException {
        setupMappings();
        Assert.assertEquals("", testUnit.convertTextToEmojisAsString("e", false));
    }

    @Test
    public void testConvertTextToEmojisNullInput() throws IOException {
        setupMappings();
        Assert.assertEquals("", testUnit.convertTextToEmojisAsString(null, false));
    }

    @Test
    public void testConvertTextToEmojisDoubleUnicodePassThrough() throws IOException {
        setupMappings();
        Assert.assertEquals("\uD83C\uDD98", testUnit.convertTextToEmojisAsString("\uD83C\uDD98", false));
    }

    @Test
    public void testConvertTextToEmojisDuplicate() throws IOException {
        setupMappings();
        Assert.assertEquals("bb", testUnit.convertTextToEmojisAsString("aa", true));
    }

    @Test
    public void testConvertTextToEmojisCombinations() throws IOException {
        setupMappings();
        Assert.assertEquals("\uD83C\uDD98", testUnit.convertTextToEmojisAsString("kk", true));
    }

    @Test
    public void testConvertTextToEmojisCombinationWithNormalText() throws IOException {
        setupMappings();
        Assert.assertEquals("\uD83C\uDD98l", testUnit.convertTextToEmojisAsString("kkk", false));
    }

    @Test
    public void testConvertTextToEmojisCombinationWithNormalTextMixed() throws IOException {
        setupMappings();
        Assert.assertEquals("\uD83C\uDD98lm", testUnit.convertTextToEmojisAsString("kkkk", false));
    }

    @Test
    public void testConvertTextToEmojisCombinationDuplicates() throws IOException {
        setupMappings();
        Assert.assertEquals("\uD83C\uDD98\uD83C\uDD98", testUnit.convertTextToEmojisAsString("kkkk", true));
    }

    private void setupMappings() throws IOException {
        ReactMapping mapping = Mockito.mock(ReactMapping.class);
        HashMap<String, List<String>> singleMappings = new HashMap<>();
        singleMappings.put("a", Arrays.asList("b"));
        singleMappings.put("s", Arrays.asList("c"));
        singleMappings.put("d", Arrays.asList("e"));
        singleMappings.put("f", Arrays.asList("g"));
        singleMappings.put("k", Arrays.asList("l", "m"));
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
        when(mapping.getSingle()).thenReturn(singleMappings);
        HashMap<String, String> combinations = new HashMap<>();
        combinations.put("kk", "\uD83C\uDD98");
        when(mapping.getCombination()).thenReturn(combinations);
        TreeSet<String> combinationKeys = new TreeSet<>();
        combinationKeys.add("kk");
        when(mapping.getCombinationKeys()).thenReturn(combinationKeys);
        when(gson.fromJson(any(JsonReader.class), eq(ReactMapping.class))).thenReturn(mapping);
        testUnit.postConstruct();
    }

}
