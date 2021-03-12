package dev.sheldan.abstracto.starboard.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.starboard.model.template.GuildStarStatsModel;
import dev.sheldan.abstracto.starboard.model.template.MemberStarStatsModel;
import dev.sheldan.abstracto.starboard.service.StarboardService;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarStatsTest {

    @InjectMocks
    private StarStats testUnit;

    @Mock
    private StarboardService starboardService;

    @Mock
    private ChannelService channelService;

    @Test
    public void executeCommand() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        GuildStarStatsModel guildStarStatsModel = Mockito.mock(GuildStarStatsModel.class);
        when(starboardService.retrieveStarStats(noParameters.getGuild().getIdLong())).thenReturn(CompletableFuture.completedFuture(guildStarStatsModel));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        verify(channelService, times(1)).sendEmbedTemplateInTextChannelList(StarStats.STARSTATS_RESPONSE_TEMPLATE, guildStarStatsModel, noParameters.getChannel());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void executeCommandForMember() {
        Member member = Mockito.mock(Member.class);
        CommandContext memberParameter = CommandTestUtilities.getWithParameters(Arrays.asList(member));
        MemberStarStatsModel model = Mockito.mock(MemberStarStatsModel.class);
        when(starboardService.retrieveStarStatsForMember(member)).thenReturn(model);
        CompletableFuture<CommandResult> result = testUnit.executeAsync(memberParameter);
        verify(channelService, times(1)).sendEmbedTemplateInTextChannelList(StarStats.STARSTATS_SINGLE_MEMBER_RESPONSE_TEMPLATE, model, memberParameter.getChannel());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }


    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
