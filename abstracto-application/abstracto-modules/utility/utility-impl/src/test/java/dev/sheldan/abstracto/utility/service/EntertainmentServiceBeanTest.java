package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.service.ConfigService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import static dev.sheldan.abstracto.utility.config.features.EntertainmentFeature.ROULETTE_BULLETS_CONFIG_KEY;
import static dev.sheldan.abstracto.utility.service.EntertainmentServiceBean.EIGHT_BALL_ANSWER_KEYS;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EntertainmentServiceBeanTest {
    @InjectMocks
    private EntertainmentServiceBean testUnit;

    @Mock
    private SecureRandom secureRandom;

    @Mock
    private ConfigService configService;

    private static final String INPUT_TEXT = "input";
    private static final int RANDOM_VALUE = 0;

    @Test
    public void testEightBallChoice() {
        when(secureRandom.nextInt(EIGHT_BALL_ANSWER_KEYS.size())).thenReturn(RANDOM_VALUE);
        String chosenKey = testUnit.getEightBallValue(INPUT_TEXT);
        Assert.assertEquals(EIGHT_BALL_ANSWER_KEYS.get(RANDOM_VALUE), chosenKey);
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
        when(configService.getLongValue(ROULETTE_BULLETS_CONFIG_KEY, serverId)).thenReturn(sides);
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

}
