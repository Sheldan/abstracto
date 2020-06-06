package dev.sheldan.abstracto.utility.repository.converter;

import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.repository.StarStatsUserResult;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarStatsUserConverterTest {

    @InjectMocks
    private StarStatsUserConverter testUnit;

    @Mock
    private BotService botService;


    @Test
    public void testConversionOfMultipleItems() {
        Long serverId = 5L;
        Long firstUserId = 5L;
        Integer firstStarCount = 5;
        Long secondUserId = 9L;
        Integer secondStarCount = 10;
        List<StarStatsUserResult> results = new ArrayList<>();
        StarStatsUserResult firstResult = Mockito.mock(StarStatsUserResult.class);
        Member firstMember = Mockito.mock(Member.class);
        when(botService.getMemberInServer(serverId, firstUserId)).thenReturn(firstMember);
        when(firstResult.getUserId()).thenReturn(firstUserId);
        when(firstResult.getStarCount()).thenReturn(firstStarCount);
        results.add(firstResult);
        StarStatsUserResult secondResult = Mockito.mock(StarStatsUserResult.class);
        Member secondMember = Mockito.mock(Member.class);
        when(botService.getMemberInServer(serverId, secondUserId)).thenReturn(secondMember);

        when(secondResult.getUserId()).thenReturn(secondUserId);
        when(secondResult.getStarCount()).thenReturn(secondStarCount);
        results.add(secondResult);

        List<StarStatsUser> starStatsUsers = testUnit.convertToStarStatsUser(results, serverId);
        StarStatsUser firstConverted = starStatsUsers.get(0);
        Assert.assertEquals(firstStarCount, firstConverted.getStarCount());
        Assert.assertEquals(firstMember, firstConverted.getMember());
        Assert.assertEquals(firstUserId, firstConverted.getUser().getId());
        StarStatsUser secondConverted = starStatsUsers.get(1);
        Assert.assertEquals(secondStarCount, secondConverted.getStarCount());
        Assert.assertEquals(secondMember, secondConverted.getMember());
        Assert.assertEquals(secondUserId, secondConverted.getUser().getId());
        Assert.assertEquals(2, starStatsUsers.size());
    }

    @Test
    public void testConversionOfEmptyList() {
        Long serverId = 5L;
        List<StarStatsUserResult> results = new ArrayList<>();

        List<StarStatsUser> starStatsUsers = testUnit.convertToStarStatsUser(results, serverId);
        verify(botService, times(0)).getMemberInServer(eq(serverId), anyLong());
        Assert.assertEquals(0, starStatsUsers.size());

    }

}
