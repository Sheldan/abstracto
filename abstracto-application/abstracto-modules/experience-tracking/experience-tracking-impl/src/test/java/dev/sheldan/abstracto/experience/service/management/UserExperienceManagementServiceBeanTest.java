package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.database.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.repository.UserExperienceRepository;
import dev.sheldan.abstracto.experience.service.LeaderBoardEntryTestImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserExperienceManagementServiceBeanTest extends ExperienceRelatedTest {

    @InjectMocks
    private UserExperienceManagementServiceBean testUnit;

    @Mock
    private UserExperienceRepository repository;

    @Mock
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Test
    public void testFindUserInServer() {
        AUserInAServer user = AUserInAServer.builder().userInServerId(1L).userReference(AUser.builder().id(2L).build()).build();
        AUserExperience experience = AUserExperience.builder().user(user).experience(2L).build();
        when(repository.findById(user.getUserInServerId())).thenReturn(Optional.of(experience));
        AUserExperience userInServer = testUnit.findUserInServer(user);
        Assert.assertEquals(experience.getExperience(), userInServer.getExperience());
    }

    @Test
    public void testNoUserCreateNewWhenSearching() {
        AUserInAServer user = Mockito.mock(AUserInAServer.class);
        AServer server = Mockito.mock(AServer.class);
        when(user.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(2L);
        AUser aUser = Mockito.mock(AUser.class);
        when(aUser.getId()).thenReturn(4L);
        when(user.getUserReference()).thenReturn(aUser);
        when(repository.findById(user.getUserInServerId())).thenReturn(Optional.empty());
        AExperienceLevel startLevel = mockInitialLevel();
        AUserExperience userInServer = testUnit.findUserInServer(user);
        Assert.assertEquals(0L, userInServer.getExperience().longValue());
        Assert.assertEquals(0L, userInServer.getMessageCount().longValue());
        Assert.assertFalse(userInServer.getExperienceGainDisabled());
        Assert.assertEquals(startLevel.getLevel(), userInServer.getCurrentLevel().getLevel());
    }

    @Test
    public void testCreatingUserExperience() {
        AUserInAServer user = Mockito.mock(AUserInAServer.class);
        AServer server = Mockito.mock(AServer.class);
        when(user.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(2L);
        AUser aUser = Mockito.mock(AUser.class);
        when(aUser.getId()).thenReturn(4L);
        when(user.getUserReference()).thenReturn(aUser);
        AExperienceLevel startLevel = mockInitialLevel();
        AUserExperience userInServer = testUnit.createUserInServer(user);
        Assert.assertEquals(0L, userInServer.getExperience().longValue());
        Assert.assertEquals(0L, userInServer.getMessageCount().longValue());
        Assert.assertFalse(userInServer.getExperienceGainDisabled());
        Assert.assertEquals(startLevel.getLevel(), userInServer.getCurrentLevel().getLevel());
    }


    @Test
    public void testLoadAllUsers() {
        AServer server = MockUtils.getServer();
        List<AUserExperience> experiences = getUserExperiences();
        when(repository.findByUser_ServerReference(server)).thenReturn(experiences);
        List<AUserExperience> loadedExperiences = testUnit.loadAllUsers(server);
        Assert.assertEquals(experiences.size(), loadedExperiences.size());
        Assert.assertEquals(experiences.get(0).getExperience(), loadedExperiences.get(0).getExperience());
        Assert.assertEquals(experiences.get(1).getExperience(), loadedExperiences.get(1).getExperience());
    }

    @Test
    public void testLoadPaginated() {
        AServer server = MockUtils.getServer();
        int endIndex = 20;
        int startIndex = 11;
        List<AUserExperience> userExperiences = getUserExperiences();
        when(repository.findTop10ByUser_ServerReferenceOrderByExperienceDesc(server, PageRequest.of(startIndex, endIndex))).thenReturn(userExperiences);
        List<AUserExperience> leaderBoardUsersPaginated = testUnit.findLeaderBoardUsersPaginated(server, startIndex, endIndex);
        Assert.assertEquals(userExperiences.size(), leaderBoardUsersPaginated.size());
        for (int i = 0; i < userExperiences.size(); i++) {
            Assert.assertEquals(userExperiences.get(i).getExperience(), leaderBoardUsersPaginated.get(i).getExperience());
        }
    }

    @Test
    public void testLoadRankOfUser() {
        long experienceValue = 2L;
        AServer server = MockUtils.getServer();
        AUserInAServer user = MockUtils.getUserObject(6L, server);
        AUserExperience experience = AUserExperience.builder().experience(experienceValue).user(user).id(3L).build();
        LeaderBoardEntryTestImpl leaderBoardEntryTest = LeaderBoardEntryTestImpl
                .builder()
                .experience(experienceValue)
                .build();
        when(repository.getRankOfUserInServer(experience.getId(), server.getId())).thenReturn(leaderBoardEntryTest);
        LeaderBoardEntryResult rankOfUserInServer = testUnit.getRankOfUserInServer(experience);
        Assert.assertEquals(experienceValue, rankOfUserInServer.getExperience().longValue());
    }

    @Test
    public void testSaveUser() {
        AUserInAServer user = AUserInAServer.builder().userInServerId(1L).userReference(AUser.builder().id(2L).build()).build();
        AUserExperience experience = AUserExperience.builder().user(user).experience(2L).build();
        when(repository.save(experience)).thenReturn(experience);
        AUserExperience createdInstance = testUnit.saveUser(experience);
        Assert.assertEquals(experience.getExperience(), createdInstance.getExperience());
        Assert.assertEquals(experience.getUser().getUserReference().getId(), createdInstance.getUser().getUserReference().getId());
    }

    private AExperienceLevel mockInitialLevel() {
        AExperienceLevel startLevel = AExperienceLevel.builder().level(0).experienceNeeded(0L).build();
        when(experienceLevelManagementService.getLevel(startLevel.getLevel())).thenReturn(Optional.of(startLevel));
        return startLevel;
    }

    private List<AUserExperience> getUserExperiences() {
        AUserExperience experience = AUserExperience.builder().experience(2L).build();
        AUserExperience experience2 = AUserExperience.builder().experience(2L).build();
        return Arrays.asList(experience, experience2);
    }
}
