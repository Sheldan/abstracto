package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.models.templates.RankModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.internal.JDAImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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
    private JDAImpl jda;

    @Test
    public void testRankExecution() {
        int currentLevelValue = 50;
        long currentExperience = 50L;
        CommandContext context = CommandTestUtilities.getNoParameters(jda);
        AExperienceLevel currentLevel = AExperienceLevel.builder().level(currentLevelValue).build();
        AUserExperience aUserExperience = AUserExperience.builder().experience(currentExperience).currentLevel(currentLevel).build();
        LeaderBoardEntry leaderBoardEntry = LeaderBoardEntry.builder().experience(aUserExperience).build();
        when(userExperienceService.getRankOfUserInServer(context.getUserInitiatedContext().getAUserInAServer())).thenReturn(leaderBoardEntry);
        LeaderBoardEntryModel leaderBoardEntryModel = LeaderBoardEntryModel.builder().build();
        when(converter.fromLeaderBoardEntry(leaderBoardEntry)).thenReturn(leaderBoardEntryModel);
        when(experienceLevelService.calculateExperienceToNextLevel(currentLevelValue, currentExperience)).thenReturn(140L);
        MessageToSend messageToSend = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq(Rank.RANK_POST_EMBED_TEMPLATE), any(RankModel.class))).thenReturn(messageToSend);
        CommandResult result = testUnit.execute(context);
        verify(channelService, Mockito.times(1)).sendMessageToSendToChannel(messageToSend, context.getChannel());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
