package dev.sheldan.abstracto.utility.commands.repost;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.converter.RepostLeaderBoardConverter;
import dev.sheldan.abstracto.utility.models.RepostLeaderboardEntryModel;
import dev.sheldan.abstracto.utility.models.RepostLeaderboardModel;
import dev.sheldan.abstracto.utility.models.database.result.RepostLeaderboardResult;
import dev.sheldan.abstracto.utility.service.management.RepostManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.utility.commands.repost.RepostLeaderboard.REPOST_LEADERBOARD_RESPONSE_TEMPLATE_KEY;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostLeaderboardTest {

    @InjectMocks
    private RepostLeaderboard testUnit;

    @Mock
    private RepostManagementService repostManagementService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ChannelService channelService;

    @Mock
    private RepostLeaderBoardConverter converter;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private RepostLeaderboardResult result;

    @Mock
    private RepostLeaderboardResult userResult;

    @Mock
    private RepostLeaderboardEntryModel convertedResult;

    @Mock
    private RepostLeaderboardEntryModel convertedUserResult;

    private static final Long SERVER_ID = 1L;

    @Captor
    private ArgumentCaptor<RepostLeaderboardModel> modelCaptor;

    @Test
    public void testExecuteWithoutParameter() {
        executeRepostLeaderboardTest(1, CommandTestUtilities.getNoParameters());
    }

    @Test
    public void testExecuteWithPageParameter() {
        Integer page = 2;
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(page));
        executeRepostLeaderboardTest(page, noParameters);
    }

    public void executeRepostLeaderboardTest(Integer page, CommandContext noParameters) {
        when(noParameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(userInServerManagementService.loadOrCreateUser(noParameters.getAuthor())).thenReturn(aUserInAServer);
        when(repostManagementService.getRepostRankOfUser(aUserInAServer)).thenReturn(userResult);
        List<RepostLeaderboardResult> resultList = Arrays.asList(result);
        when(repostManagementService.findTopRepostingUsersOfServer(SERVER_ID, page, 5)).thenReturn(resultList);
        List<RepostLeaderboardEntryModel> convertedList = Arrays.asList(convertedResult);
        when(converter.fromLeaderBoardResults(resultList)).thenReturn(CompletableFuture.completedFuture(convertedList));
        when(converter.convertSingleUser(userResult)).thenReturn(CompletableFuture.completedFuture(convertedUserResult));
        CompletableFuture<CommandResult> resultFuture = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(resultFuture);
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(REPOST_LEADERBOARD_RESPONSE_TEMPLATE_KEY), modelCaptor.capture(), eq(noParameters.getChannel()));
        RepostLeaderboardModel model = modelCaptor.getValue();
        Assert.assertEquals(noParameters.getGuild(), model.getGuild());
        Assert.assertEquals(convertedList, model.getEntries());
        Assert.assertEquals(convertedUserResult, model.getUserExecuting());
        Assert.assertEquals(noParameters.getAuthor(), model.getMember());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
