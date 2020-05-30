package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.internal.JDAImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LeaderBoardCommandTest {

    @InjectMocks
    private LeaderBoardCommand testUnit;

    @Mock
    private AUserExperienceService userExperienceService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ChannelService channelService;

    @Mock
    private LeaderBoardModelConverter converter;

    @Mock
    private JDAImpl jda;

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit, jda);
    }

    @Test
    public void testLeaderBoardWithNoParameter() {
        testLeaderBoardCommand(CommandTestUtilities.getNoParameters(jda), 1);
    }

    @Test
    public void testLeaderBoardWithPageParameter() {
        testLeaderBoardCommand(CommandTestUtilities.getWithParameters(jda, Arrays.asList(5)), 5);
    }

    private void testLeaderBoardCommand(CommandContext context, int expectedPage) {
        LeaderBoard leaderBoard = LeaderBoard.builder().build();
        when(userExperienceService.findLeaderBoardData(context.getUserInitiatedContext().getServer(), expectedPage)).thenReturn(leaderBoard);
        when(converter.fromLeaderBoard(leaderBoard)).thenReturn(new ArrayList<>());
        LeaderBoardEntry executingUserRank = LeaderBoardEntry.builder().build();
        when(userExperienceService.getRankOfUserInServer(context.getUserInitiatedContext().getAUserInAServer())).thenReturn(executingUserRank);
        LeaderBoardEntryModel leaderBoardEntryModel = LeaderBoardEntryModel.builder().build();
        when(converter.fromLeaderBoardEntry(executingUserRank)).thenReturn(leaderBoardEntryModel);
        MessageToSend messageToSend = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq(LeaderBoardCommand.LEADERBOARD_POST_EMBED_TEMPLATE), any(LeaderBoardModel.class))).thenReturn(messageToSend);
        CommandResult result = testUnit.execute(context);
        verify(channelService, times(1)).sendMessageToSendToChannel(messageToSend, context.getChannel());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
