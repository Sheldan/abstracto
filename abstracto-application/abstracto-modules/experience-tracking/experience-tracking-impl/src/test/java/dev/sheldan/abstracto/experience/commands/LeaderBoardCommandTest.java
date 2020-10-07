package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
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
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

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

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTestAsync(testUnit);
    }

    @Test
    public void testLeaderBoardWithNoParameter() {
        testLeaderBoardCommand(CommandTestUtilities.getNoParameters(), 1);
    }

    @Test
    public void testLeaderBoardWithPageParameter() {
        testLeaderBoardCommand(CommandTestUtilities.getWithParameters(Arrays.asList(5)), 5);
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
        when(templateService.renderEmbedTemplate(eq(LeaderBoardCommand.LEADER_BOARD_POST_EMBED_TEMPLATE), any(LeaderBoardModel.class))).thenReturn(messageToSend);
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        verify(channelService, times(1)).sendMessageToSendToChannel(messageToSend, context.getChannel());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
