package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.models.templates.RankModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.experience.commands.Rank.RANK_POST_EMBED_TEMPLATE;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RankTest {

    @InjectMocks
    private Rank testUnit;

    @Mock
    private LeaderBoardModelConverter converter;

    @Mock
    private TemplateService templateService;

    @Mock
    private AUserExperienceService userExperienceService;

    @Mock
    private ExperienceLevelService experienceLevelService;

    @Mock
    private ChannelService channelService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private Rank self;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private UserExperienceManagementService userExperienceManagementService;

    @Test
    public void testRankExecution() {
        CommandContext context = CommandTestUtilities.getNoParameters();
        LeaderBoardEntry leaderBoardEntry = Mockito.mock(LeaderBoardEntry.class);
        when(userInServerManagementService.loadUser(context.getAuthor())).thenReturn(aUserInAServer);
        when(userExperienceService.getRankOfUserInServer(aUserInAServer)).thenReturn(leaderBoardEntry);
        LeaderBoardEntryModel leaderBoardEntryModel = Mockito.mock(LeaderBoardEntryModel.class);
        when(converter.fromLeaderBoardEntry(leaderBoardEntry)).thenReturn(CompletableFuture.completedFuture(leaderBoardEntryModel));
        when(self.renderAndSendRank(eq(context), any(RankModel.class), eq(leaderBoardEntryModel))).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void testRenderAndSend() {
        int currentLevelValue = 50;
        long currentExperience = 50L;
        AExperienceLevel currentLevel = Mockito.mock(AExperienceLevel.class);
        when(currentLevel.getLevel()).thenReturn(currentLevelValue);
        AUserExperience aUserExperience = Mockito.mock(AUserExperience.class);
        when(aUserExperience.getCurrentLevel()).thenReturn(currentLevel);
        when(aUserExperience.getExperience()).thenReturn(currentExperience);
        CommandContext context = CommandTestUtilities.getNoParameters();
        RankModel rankModel = Mockito.mock(RankModel.class);
        LeaderBoardEntryModel leaderBoardEntryModel = Mockito.mock(LeaderBoardEntryModel.class);
        when(userInServerManagementService.loadUser(context.getAuthor())).thenReturn(aUserInAServer);
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(aUserExperience);
        when(experienceLevelService.calculateExperienceToNextLevel(currentLevelValue, currentExperience)).thenReturn(140L);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(RANK_POST_EMBED_TEMPLATE, rankModel)).thenReturn(messageToSend);
        when(channelService.sendMessageToSendToChannel(messageToSend, context.getChannel())).thenReturn(Arrays.asList(CompletableFuture.completedFuture(null)));
        testUnit.renderAndSendRank(context, rankModel, leaderBoardEntryModel).join();
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
