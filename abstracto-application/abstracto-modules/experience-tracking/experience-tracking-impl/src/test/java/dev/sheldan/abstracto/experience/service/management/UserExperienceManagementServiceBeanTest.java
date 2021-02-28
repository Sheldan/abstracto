package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.database.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.repository.UserExperienceRepository;
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
public class UserExperienceManagementServiceBeanTest {

    @InjectMocks
    private UserExperienceManagementServiceBean testUnit;

    @Mock
    private UserExperienceRepository repository;

    @Mock
    private ExperienceLevelManagementService experienceLevelManagementService;

    private static final Long SERVER_ID = 2L;
    private static final Long USER_IN_SERVER_ID = 3L;
    private static final Long USER_ID = 4L;
    private static final Integer START_LEVEL = 0;

    @Test
    public void testFindUserInServer() {
        AUserInAServer user = Mockito.mock(AUserInAServer.class);
        when(user.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        when(repository.findById(USER_IN_SERVER_ID)).thenReturn(Optional.of(experience));
        AUserExperience retrievedExperience = testUnit.findUserInServer(user);
        Assert.assertEquals(experience, retrievedExperience);
    }

    @Test
    public void testNoUserCreateNewWhenSearching() {
        AUserInAServer user = Mockito.mock(AUserInAServer.class);
        AServer server = Mockito.mock(AServer.class);
        when(user.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        AUser aUser = Mockito.mock(AUser.class);
        when(user.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        when(aUser.getId()).thenReturn(USER_ID);
        when(user.getUserReference()).thenReturn(aUser);
        when(repository.findById(USER_IN_SERVER_ID)).thenReturn(Optional.empty());
        AExperienceLevel startLevel = Mockito.mock(AExperienceLevel.class);
        when(startLevel.getLevel()).thenReturn(START_LEVEL);
        when(experienceLevelManagementService.getLevel(START_LEVEL)).thenReturn(Optional.of(startLevel));
        AUserExperience userInServer = testUnit.findUserInServer(user);
        Assert.assertEquals(0L, userInServer.getExperience().longValue());
        Assert.assertEquals(0L, userInServer.getMessageCount().longValue());
        Assert.assertFalse(userInServer.getExperienceGainDisabled());
        Assert.assertEquals(START_LEVEL, userInServer.getCurrentLevel().getLevel());
    }

    @Test
    public void testCreatingUserExperience() {
        AUserInAServer user = Mockito.mock(AUserInAServer.class);
        AServer server = Mockito.mock(AServer.class);
        when(user.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        AUser aUser = Mockito.mock(AUser.class);
        when(aUser.getId()).thenReturn(USER_ID);
        when(user.getUserReference()).thenReturn(aUser);
        AExperienceLevel startLevel = Mockito.mock(AExperienceLevel.class);
        when(startLevel.getLevel()).thenReturn(START_LEVEL);
        when(experienceLevelManagementService.getLevel(START_LEVEL)).thenReturn(Optional.of(startLevel));
        AUserExperience userInServer = testUnit.createUserInServer(user);
        Assert.assertEquals(0L, userInServer.getExperience().longValue());
        Assert.assertEquals(0L, userInServer.getMessageCount().longValue());
        Assert.assertFalse(userInServer.getExperienceGainDisabled());
        Assert.assertEquals(START_LEVEL, userInServer.getCurrentLevel().getLevel());
    }


    @Test
    public void testLoadAllUsers() {
        AServer server = Mockito.mock(AServer.class);
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        AUserExperience experience2 = Mockito.mock(AUserExperience.class);
        List<AUserExperience> experiences = Arrays.asList(experience, experience2);
        when(repository.findByUser_ServerReference(server)).thenReturn(experiences);
        List<AUserExperience> loadedExperiences = testUnit.loadAllUsers(server);
        Assert.assertEquals(experiences.size(), loadedExperiences.size());
        Assert.assertEquals(experience, loadedExperiences.get(0));
        Assert.assertEquals(experience2, loadedExperiences.get(1));
    }

    @Test
    public void testLoadPaginated() {
        AServer server = Mockito.mock(AServer.class);
        int endIndex = 20;
        int startIndex = 11;
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        AUserExperience experience2 = Mockito.mock(AUserExperience.class);
        List<AUserExperience> experiences = Arrays.asList(experience, experience2);
        when(repository.findTop10ByUser_ServerReferenceOrderByExperienceDesc(server, PageRequest.of(startIndex, endIndex))).thenReturn(experiences);
        List<AUserExperience> leaderBoardUsersPaginated = testUnit.findLeaderBoardUsersPaginated(server, startIndex, endIndex);
        Assert.assertEquals(experiences.size(), leaderBoardUsersPaginated.size());
        Assert.assertEquals(experience, leaderBoardUsersPaginated.get(0));
        Assert.assertEquals(experience2, leaderBoardUsersPaginated.get(1));
    }

    @Test
    public void testLoadRankOfUser() {
        long experienceValue = 2L;
        AServer server = Mockito.mock(AServer.class);
        when(server.getId()).thenReturn(SERVER_ID);
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        when(experience.getServer()).thenReturn(server);
        when(experience.getId()).thenReturn(USER_IN_SERVER_ID);
        LeaderBoardEntryResult leaderBoardEntryTest = Mockito.mock(LeaderBoardEntryResult.class);
        when(leaderBoardEntryTest.getExperience()).thenReturn(experienceValue);
        when(repository.getRankOfUserInServer(USER_IN_SERVER_ID, SERVER_ID)).thenReturn(leaderBoardEntryTest);
        LeaderBoardEntryResult rankOfUserInServer = testUnit.getRankOfUserInServer(experience);
        Assert.assertEquals(experienceValue, rankOfUserInServer.getExperience().longValue());
    }

    @Test
    public void testSaveUser() {
        AUserExperience experience =  Mockito.mock(AUserExperience.class);
        when(repository.save(experience)).thenReturn(experience);
        AUserExperience createdInstance = testUnit.saveUser(experience);
        Assert.assertEquals(experience, createdInstance);
    }

}
