package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeatureConfig;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.templates.UserSyncStatusModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class AUserExperienceServiceBeanTest extends ExperienceRelatedTest {

    @InjectMocks
    private AUserExperienceServiceBean testUnit;

    @Mock
    private UserExperienceManagementService userExperienceManagementService;

    @Mock
    private ExperienceRoleService experienceRoleService;

    @Mock
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Mock
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Mock
    private ConfigService configService;

    @Mock
    private RoleService roleService;

    @Mock
    private MessageService messageService;

    @Mock
    private TemplateService templateService;

    @Mock
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Mock
    private BotService botService;

    @Mock
    private JDAImpl jda;

    @Mock
    private RunTimeExperienceService runTimeExperienceService;

    @Captor
    private ArgumentCaptor<AUserExperience> aUserExperienceArgumentCaptor;

    @Test
    public void testCalculateLevelTooLow() {
        AUserExperience experienceToCalculate = AUserExperience.builder().experience(50L).build();
        List<AExperienceLevel> levels = getLevelConfiguration();
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(experienceToCalculate, levels);
        Assert.assertEquals(0, calculatedLevel.getLevel().intValue());
    }

    @Test
    public void testCalculateLevelBetweenLevels() {
        AUserExperience experienceToCalculate = AUserExperience.builder().experience(250L).build();
        List<AExperienceLevel> levels = getLevelConfiguration();
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(experienceToCalculate, levels);
        Assert.assertEquals(2, calculatedLevel.getLevel().intValue());
    }

    @Test
    public void testCalculateLevelTooHigh() {
        AUserExperience experienceToCalculate = AUserExperience.builder().experience(500L).build();
        List<AExperienceLevel> levels = getLevelConfiguration();
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(experienceToCalculate, levels);
        Assert.assertEquals(3, calculatedLevel.getLevel().intValue());
    }

    @Test
    public void testUpdateUserExperienceLevelChanged() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        AUserInAServer userObject = MockUtils.getUserObject(2L, MockUtils.getServer());
        AUserExperience experienceToCalculate = AUserExperience.builder().user(userObject).currentLevel(levels.get(1)).experience(250L).build();
        Assert.assertTrue(testUnit.updateUserLevel(experienceToCalculate, levels));
    }

    @Test
    public void testUpdateUserExperienceLevelNotChanged() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        AUserInAServer userObject = MockUtils.getUserObject(2L, MockUtils.getServer());
        AUserExperience experienceToCalculate = AUserExperience.builder().user(userObject).currentLevel(levels.get(2)).experience(250L).build();
        Assert.assertFalse(testUnit.updateUserLevel(experienceToCalculate, levels));
    }

    @Test
    public void testGainExpSingleUserLvlUpOneServerWithoutRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        AExperienceRole previousExperienceRole = experienceRoles.get(1);
        when(botService.isUserInGuild(userToUse)).thenReturn(true);
        AExperienceRole newAwardedRole = testRoleRelatedScenario(false, levels, servers, serverToUse, experienceRoles, userToUse, previousExperienceRole);
        verify(roleService, times(1)).addRoleToUser(userToUse, newAwardedRole.getRole());
        verify(roleService, times(1)).removeRoleFromUser(userToUse, previousExperienceRole.getRole());
    }

    @Test
    public void testLevelUpGainingNewRoleButUserAlreadyHasRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        AExperienceRole previousExperienceRole = experienceRoles.get(1);
        AExperienceRole newAwardedRole = testRoleRelatedScenario(true, levels, servers, serverToUse, experienceRoles, userToUse, previousExperienceRole);
        verify(roleService, times(0)).addRoleToUser(userToUse, newAwardedRole.getRole());
        verify(roleService, times(0)).removeRoleFromUser(userToUse, previousExperienceRole.getRole());
    }

    @Test
    public void testLevelUpNotGainingNewRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        levels.add(AExperienceLevel.builder().level(4).experienceNeeded(400L).build());
        mockSimpleServer(levels, experienceRoles, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        MemberImpl jdaMember = MockUtils.getMockedMember(serverToUse, userToUse, jda);
        when(botService.getMemberInServer(serverToUse, userToUse.getUserReference())).thenReturn(jdaMember);
        AExperienceRole previousExperienceRole = experienceRoles.get(3);
        AUserExperience newUserExperience = mockServerWithSingleUser(levels, serverToUse, 401L, 3, previousExperienceRole, false);

        AExperienceRole newAwardedRole = experienceRoles.get(3);
        when(experienceRoleService.calculateRole(newUserExperience, experienceRoles)).thenReturn(newAwardedRole);
        when(roleService.memberHasRole(jdaMember, newAwardedRole.getRole())).thenReturn(true);
        testUnit.handleExperienceGain(servers);
        verify(userExperienceManagementService, times(1)).saveUser(eq(newUserExperience));
        verify(roleService, times(0)).removeRoleFromUser(userToUse, previousExperienceRole.getRole());
        verify(roleService, times(0)).addRoleToUser(userToUse, newAwardedRole.getRole());
        Assert.assertEquals(4, newUserExperience.getCurrentLevel().getLevel().intValue());
        Assert.assertEquals(3L, newUserExperience.getCurrentExperienceRole().getRole().getId().longValue());
    }

    @Test
    public void testHandleExperienceForUserNotLevelingUpWithoutExistingRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        mockSimpleServer(levels, experienceRoles, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        MemberImpl jdaMember = MockUtils.getMockedMember(serverToUse, userToUse, jda);
        when(botService.getMemberInServer(serverToUse, userToUse.getUserReference())).thenReturn(jdaMember);
        AUserExperience newUserExperience = mockServerWithSingleUser(levels, serverToUse, 101L, 1, null, false);

        when(experienceRoleService.calculateRole(newUserExperience, experienceRoles)).thenReturn(experienceRoles.get(1));
        testUnit.handleExperienceGain(servers);
        verify(userExperienceManagementService, times(1)).saveUser(eq(newUserExperience));
        Assert.assertEquals(1, newUserExperience.getCurrentLevel().getLevel().intValue());
        Assert.assertEquals(1L, newUserExperience.getCurrentExperienceRole().getRole().getId().longValue());
    }

    @Test
    public void handleExpGainWithTooLittleForRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        mockSimpleServer(levels, experienceRoles, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        MemberImpl jdaMember = MockUtils.getMockedMember(serverToUse, userToUse, jda);
        when(botService.getMemberInServer(serverToUse, userToUse.getUserReference())).thenReturn(jdaMember);
        AUserExperience newUserExperience = mockServerWithSingleUser(levels, serverToUse, 50L, 0, null, false);

        when(experienceRoleService.calculateRole(newUserExperience, experienceRoles)).thenReturn(null);
        testUnit.handleExperienceGain(servers);
        verify(userExperienceManagementService, times(1)).saveUser(eq(newUserExperience));
        Assert.assertEquals(0, newUserExperience.getCurrentLevel().getLevel().intValue());
        Assert.assertNull(newUserExperience.getCurrentExperienceRole());
    }

    @Test
    public void testUserHasExperienceRoleButNotAnymore() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        mockSimpleServer(levels, experienceRoles, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        MemberImpl jdaMember = MockUtils.getMockedMember(serverToUse, userToUse, jda);
        when(botService.getMemberInServer(serverToUse, userToUse.getUserReference())).thenReturn(jdaMember);
        AUserExperience newUserExperience = mockServerWithSingleUser(levels, serverToUse, 50L, 0, experienceRoles.get(0), false);

        when(experienceRoleService.calculateRole(newUserExperience, experienceRoles)).thenReturn(null);
        when(botService.isUserInGuild(userToUse)).thenReturn(true);
        testUnit.handleExperienceGain(servers);
        verify(userExperienceManagementService, times(1)).saveUser(eq(newUserExperience));
        verify(roleService, times(1)).removeRoleFromUser(userToUse, experienceRoles.get(0).getRole());
        verify(roleService, times(0)).addRoleToUser(eq(userToUse), any(ARole.class));
        Assert.assertEquals(0, newUserExperience.getCurrentLevel().getLevel().intValue());
        Assert.assertNull(newUserExperience.getCurrentExperienceRole());
    }

    @Test
    public void testHandleExperienceGainForGainDisabledForUser() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        mockSimpleServer(levels, experienceRoles, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        MockUtils.getMockedMember(serverToUse, userToUse, jda);
        AUserExperience newUserExperience = mockServerWithSingleUser(levels, serverToUse, 50L, 0, experienceRoles.get(0), true);

        testUnit.handleExperienceGain(servers);
        verify(userExperienceManagementService, times(0)).saveUser(eq(newUserExperience));
        verify(roleService, times(0)).removeRoleFromUser(userToUse, experienceRoles.get(0).getRole());
        verify(roleService, times(0)).addRoleToUser(eq(userToUse), any(ARole.class));
        Assert.assertEquals(0, newUserExperience.getCurrentLevel().getLevel().intValue());
        Assert.assertEquals(experienceRoles.get(0).getRole().getId(), newUserExperience.getCurrentExperienceRole().getRole().getId());
    }

    @Test
    public void testHandleExperienceGainForGainDisabledForRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        mockSimpleServer(levels, experienceRoles, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        MemberImpl jdaMember = MockUtils.getMockedMember(serverToUse, userToUse, jda);
        AUserExperience newUserExperience = mockServerWithSingleUser(levels, serverToUse, 50L, 0, experienceRoles.get(0), false);
        when(botService.getMemberInServer(userToUse)).thenReturn(jdaMember);
        when(roleService.hasAnyOfTheRoles(eq(jdaMember), anyList())).thenReturn(true);
        testUnit.handleExperienceGain(servers);
        verify(userExperienceManagementService, times(0)).saveUser(eq(newUserExperience));
        verify(roleService, times(0)).removeRoleFromUser(userToUse, experienceRoles.get(0).getRole());
        verify(roleService, times(0)).addRoleToUser(eq(userToUse), any(ARole.class));
        Assert.assertEquals(0, newUserExperience.getCurrentLevel().getLevel().intValue());
        Assert.assertEquals(experienceRoles.get(0).getRole().getId(), newUserExperience.getCurrentExperienceRole().getRole().getId());
    }

    @Test
    public void testHandleExperienceForUserNotLevelingUpWithExistingRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getOneServerWithOneUser();
        AServer serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels, serverToUse);
        mockSimpleServer(levels, experienceRoles, serverToUse);
        AUserInAServer userToUse = serverToUse.getUsers().get(0);
        MemberImpl jdaMember = MockUtils.getMockedMember(serverToUse, userToUse, jda);
        when(botService.getMemberInServer(serverToUse, userToUse.getUserReference())).thenReturn(jdaMember);
        AUserExperience newUserExperience = mockServerWithSingleUser(levels, serverToUse, 101L, 1, experienceRoles.get(1), false);
        when(experienceRoleService.calculateRole(newUserExperience, experienceRoles)).thenReturn(experienceRoles.get(1));
        testUnit.handleExperienceGain(servers);
        verify(userExperienceManagementService, times(1)).saveUser(eq(newUserExperience));
        verify(roleService, times(0)).removeRoleFromUser(userToUse, experienceRoles.get(0).getRole());
        verify(roleService, times(0)).addRoleToUser(eq(userToUse), any(ARole.class));
        Assert.assertEquals(1, newUserExperience.getCurrentLevel().getLevel().intValue());
        Assert.assertEquals(experienceRoles.get(1).getRole().getId(), newUserExperience.getCurrentExperienceRole().getRole().getId());
    }

    @Test
    public void testSingleUserInMultipleServers() {
        // The user levels in one server, and does not level in another one
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AServer> servers = getServersWithUserExperience();
        List<AUserExperience> userExperiences = new ArrayList<>();
        List<List<AExperienceRole>> allExperienceRoles = new ArrayList<>();
        List<Integer> userLevels = Arrays.asList(1,2);
        List<Long> userExperienceValues = Arrays.asList(101L,301L);
        List<Integer> experienceRoleIndices = Arrays.asList(1, 2);
        for (int i = 0; i < servers.size(); i++) {
            AServer aServer = servers.get(i);
            Integer level = userLevels.get(i);
            Long experienceValues = userExperienceValues.get(i);
            List<AExperienceRole> experienceRoles = getExperienceRoles(levels, aServer);
            mockSimpleServer(levels, experienceRoles, aServer);
            AUserInAServer userToUse = aServer.getUsers().get(0);
            MemberImpl jdaMember = MockUtils.getMockedMember(aServer, userToUse, jda);
            when(botService.getMemberInServer(aServer, userToUse.getUserReference())).thenReturn(jdaMember);
            AExperienceRole role = experienceRoles.get(experienceRoleIndices.get(i));
            AUserExperience newUserExperience = mockServerWithSingleUser(levels, aServer, experienceValues, level, role, false);
            when(experienceRoleService.calculateRole(newUserExperience, experienceRoles)).thenReturn(role);
            userExperiences.add(newUserExperience);
            allExperienceRoles.add(experienceRoles);
        }
        testUnit.handleExperienceGain(servers);
        List<Integer> newLevels = Arrays.asList(1,3);
        List<Integer> newExperienceRoleIndices = Arrays.asList(1, 2);
        for (int i = 0; i < servers.size(); i++) {
            AServer server = servers.get(i);
            AUserExperience newUserExperience = userExperiences.get(i);
            AUserInAServer userToUse = server.getUsers().get(0);
            List<AExperienceRole> experienceRoles = allExperienceRoles.get(i);
            verify(userExperienceManagementService, times(2)).saveUser(aUserExperienceArgumentCaptor.capture());
            verify(roleService, times(0)).removeRoleFromUser(userToUse, experienceRoles.get(0).getRole());
            verify(roleService, times(0)).addRoleToUser(eq(userToUse), any(ARole.class));
            Assert.assertEquals(newLevels.get(i).intValue(), newUserExperience.getCurrentLevel().getLevel().intValue());
            Assert.assertEquals(experienceRoles.get(newExperienceRoleIndices.get(i)).getRole().getId(), newUserExperience.getCurrentExperienceRole().getRole().getId());
        }
    }

    @Test
    public void testSyncNoRoleUserGettingRole() {
        AServer server = MockUtils.getServer(2L);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(getLevelConfiguration(), server);
        AExperienceRole previousRole = null;
        AExperienceRole afterRole = usedExperienceRoles.get(0);
        Integer removals = 0;
        Integer adds = 1;
        executeSyncSingleUserTest(server, usedExperienceRoles, previousRole, afterRole, removals, adds);
    }

    @Test
    public void testSyncUserLosingRole() {
        AServer server = MockUtils.getServer(2L);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(getLevelConfiguration(), server);
        AExperienceRole previousRole = usedExperienceRoles.get(0);
        AExperienceRole afterRole = null;
        Integer removals = 1;
        Integer adds = 0;
        executeSyncSingleUserTest(server, usedExperienceRoles, previousRole, afterRole, removals, adds);
    }

    @Test
    public void testSyncUserKeepRole() {
        AServer server = MockUtils.getServer(2L);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(getLevelConfiguration(), server);
        AExperienceRole previousRole = usedExperienceRoles.get(0);
        AExperienceRole afterRole = usedExperienceRoles.get(0);
        Integer removals = 0;
        Integer adds = 0;
        executeSyncSingleUserTest(server, usedExperienceRoles, previousRole, afterRole, removals, adds);
    }

    @Test
    public void testSyncUserChangingRole() {
        AServer server = MockUtils.getServer(2L);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(getLevelConfiguration(), server);
        AExperienceRole previousRole = usedExperienceRoles.get(0);
        AExperienceRole afterRole = usedExperienceRoles.get(1);
        Integer removals = 1;
        Integer adds = 1;
        executeSyncSingleUserTest(server, usedExperienceRoles, previousRole, afterRole, removals, adds);
    }

    @Test
    public void testDisablingExperienceForUser() {
        AServer server = MockUtils.getServer(1L);
        AUserInAServer userObject = MockUtils.getUserObject(2L, server);
        AUserExperience experience = AUserExperience.builder().user(userObject).experienceGainDisabled(false).build();
        when(userExperienceManagementService.findUserInServer(userObject)).thenReturn(experience);
        testUnit.disableExperienceForUser(userObject);
        Assert.assertTrue(experience.getExperienceGainDisabled());
    }

    @Test
    public void testDisablingExpForUserWhichHasItDisabled() {
        AServer server = MockUtils.getServer(1L);
        AUserInAServer userObject = MockUtils.getUserObject(2L, server);
        AUserExperience experience = AUserExperience.builder().user(userObject).experienceGainDisabled(true).build();
        when(userExperienceManagementService.findUserInServer(userObject)).thenReturn(experience);
        testUnit.disableExperienceForUser(userObject);
        Assert.assertTrue(experience.getExperienceGainDisabled());
    }

    @Test
    public void testEnablingExperienceForEnabledUser() {
        AServer server = MockUtils.getServer(1L);
        AUserInAServer userObject = MockUtils.getUserObject(2L, server);
        AUserExperience experience = AUserExperience.builder().user(userObject).experienceGainDisabled(false).build();
        when(userExperienceManagementService.findUserInServer(userObject)).thenReturn(experience);
        testUnit.enableExperienceForUser(userObject);
        Assert.assertFalse(experience.getExperienceGainDisabled());
    }

    @Test
    public void testEnablingExpForUserWhichHasItDisabled() {
        AServer server = MockUtils.getServer(1L);
        AUserInAServer userObject = MockUtils.getUserObject(2L, server);
        AUserExperience experience = AUserExperience.builder().user(userObject).experienceGainDisabled(true).build();
        when(userExperienceManagementService.findUserInServer(userObject)).thenReturn(experience);
        testUnit.enableExperienceForUser(userObject);
        Assert.assertFalse(experience.getExperienceGainDisabled());
    }

    @Test
    public void testFindLeaderBoardData() {
        AServer server = MockUtils.getServer(1L);
        executeLeaderBoardTest(server, 1);
    }

    @Test
    public void testFindLeaderBoardDataSecondPage() {
        AServer server = MockUtils.getServer(1L);
        executeLeaderBoardTest(server, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalLeaderBoardPage() {
        testUnit.findLeaderBoardData(MockUtils.getServer(1L), -1);
    }

    @Test
    public void testSyncAllUsers() {
        AServer server = MockUtils.getServer(2L);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(getLevelConfiguration(), server);
        AExperienceRole firstPreviousRole = null;
        AExperienceRole firstAfterRole = usedExperienceRoles.get(0);
        AExperienceRole secondPreviousRole = null;
        AExperienceRole secondAfterRole = null;
        AExperienceLevel level0 = AExperienceLevel.builder().level(0).build();
        AUserExperience experience = AUserExperience.builder().experience(40L).user(MockUtils.getUserObject(3L, server)).currentLevel(level0).build();
        AUserExperience experience2 = AUserExperience.builder().experience(201L).user(MockUtils.getUserObject(4L, server)).currentLevel(level0).build();
        List<AUserExperience> experiences = Arrays.asList(experience, experience2);

        List<AUserInAServer> users = experiences.stream().map(AUserExperience::getUser).collect(Collectors.toList());
        MemberImpl firstMember = MockUtils.getMockedMember(server, users.get(0), jda);
        when(botService.getMemberInServer(server, users.get(0).getUserReference())).thenReturn(firstMember);
        MemberImpl secondMember = MockUtils.getMockedMember(server, users.get(1), jda);
        when(botService.getMemberInServer(server, users.get(1).getUserReference())).thenReturn(secondMember);
        experience.setCurrentExperienceRole(firstPreviousRole);
        experience2.setCurrentExperienceRole(secondPreviousRole);
        when(userExperienceManagementService.loadAllUsers(server)).thenReturn(experiences);
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(usedExperienceRoles);
        when(experienceRoleService.calculateRole(experience, usedExperienceRoles)).thenReturn(firstAfterRole);
        when(experienceRoleService.calculateRole(experience2, usedExperienceRoles)).thenReturn(secondAfterRole);
        when(botService.getMemberInServer(server, experience.getUser().getUserReference())).thenReturn(firstMember);
        when(botService.getMemberInServer(server, experience2.getUser().getUserReference())).thenReturn(secondMember);
        when(roleService.memberHasRole(firstMember, firstAfterRole.getRole())).thenReturn(false);
        testUnit.syncUserRoles(server);
        Assert.assertNull(experience2.getCurrentExperienceRole());
        Assert.assertEquals(usedExperienceRoles.get(0).getRole().getId(), experience.getCurrentExperienceRole().getRole().getId());
        verify(roleService, times(1)).addRoleToUser(users.get(0), usedExperienceRoles.get(0).getRole());
    }

    @Test
    public void testGetRankForUser() {
        long experience = 1L;
        int level = 1;
        long messageCount = 1L;
        int rank = 1;
        AServer server = MockUtils.getServer(2L);
        AUserInAServer userInAServer = MockUtils.getUserObject(1L, server);
        AExperienceLevel level0 = AExperienceLevel.builder().experienceNeeded(200L).level(level).build();
        AUserExperience experienceObj = AUserExperience.builder().experience(experience).user(MockUtils.getUserObject(3L, server)).currentLevel(level0).build();
        when(userExperienceManagementService.findUserInServer(userInAServer)).thenReturn(experienceObj);
        LeaderBoardEntryTestImpl leaderBoardEntryTest = LeaderBoardEntryTestImpl
                .builder()
                .experience(experience)
                .id(1L)
                .level(level)
                .messageCount(messageCount)
                .rank(rank)
                .userInServerId(1L)
                .build();
        when(userExperienceManagementService.getRankOfUserInServer(experienceObj)).thenReturn(leaderBoardEntryTest);
        LeaderBoardEntry rankOfUserInServer = testUnit.getRankOfUserInServer(userInAServer);
        Assert.assertEquals(experience, rankOfUserInServer.getExperience().getExperience().longValue());
        Assert.assertEquals(level, rankOfUserInServer.getExperience().getCurrentLevel().getLevel().intValue());
        Assert.assertEquals(rank, rankOfUserInServer.getRank().intValue());
    }

    @Test
    public void testGetRankForUserNotExisting() {
        AServer server = MockUtils.getServer(2L);
        AUserInAServer userInAServer = MockUtils.getUserObject(1L, server);
        when(userExperienceManagementService.findUserInServer(userInAServer)).thenReturn(null);
        LeaderBoardEntry rankOfUserInServer = testUnit.getRankOfUserInServer(userInAServer);
        Assert.assertNull(rankOfUserInServer.getExperience());
        Assert.assertEquals(0, rankOfUserInServer.getRank().intValue());
    }

    @Test
    public void testGetRankWhenRankReturnsNull() {
        long experience = 1L;
        int level = 1;
        long messageCount = 1L;
        AServer server = MockUtils.getServer(2L);
        AUserInAServer userInAServer = MockUtils.getUserObject(1L, server);
        AExperienceLevel level0 = AExperienceLevel.builder().experienceNeeded(200L).level(level).build();
        AUserExperience experienceObj = AUserExperience.builder().experience(experience).user(MockUtils.getUserObject(3L, server)).messageCount(messageCount).currentLevel(level0).build();
        when(userExperienceManagementService.findUserInServer(userInAServer)).thenReturn(experienceObj);
        when(userExperienceManagementService.getRankOfUserInServer(experienceObj)).thenReturn(null);
        LeaderBoardEntry rankOfUserInServer = testUnit.getRankOfUserInServer(userInAServer);
        Assert.assertEquals(experience, rankOfUserInServer.getExperience().getExperience().longValue());
        Assert.assertEquals(level, rankOfUserInServer.getExperience().getCurrentLevel().getLevel().intValue());
        Assert.assertEquals(messageCount, rankOfUserInServer.getExperience().getMessageCount().longValue());
        Assert.assertEquals(0, rankOfUserInServer.getRank().intValue());
    }

    @Test
    public void testSyncRolesWithFeedBack() {
        AServer server = MockUtils.getServer(1L);
        AChannel channel = AChannel.builder().id(2L).build();
        List<AUserExperience> experiences = getUserExperiences(25, server);

        checkStatusMessages(server, channel, experiences, 13);
    }

    @Test
    public void testSyncRolesWithNoUsers() {
        AServer server = MockUtils.getServer(1L);
        AChannel channel = AChannel.builder().id(2L).build();
        List<AUserExperience> experiences = new ArrayList<>();

        checkStatusMessages(server, channel, experiences, 1);
    }

    private void checkStatusMessages(AServer server, AChannel channel, List<AUserExperience> experiences, int messageCount) {
        when(userExperienceManagementService.loadAllUsers(server)).thenReturn(experiences);
        MessageToSend statusMessage = MessageToSend.builder().message("text").build();
        when(templateService.renderEmbedTemplate(eq("user_sync_status_message"), any(UserSyncStatusModel.class))).thenReturn(statusMessage);
        long messageId = 5L;
        ReceivedMessage statusMessageJDA = MockUtils.buildMockedMessage(messageId, "text", null);
        when(messageService.createStatusMessage(statusMessage, channel)).thenReturn(CompletableFuture.completedFuture(statusMessageJDA));
        testUnit.syncUserRolesWithFeedback(server, channel);
        verify(messageService, times(messageCount)).updateStatusMessage(channel, messageId, statusMessage);
    }

    private void executeSyncSingleUserTest(AServer server, List<AExperienceRole> usedExperienceRoles, AExperienceRole previousRole, AExperienceRole afterRole, Integer removals, Integer adds) {
        AExperienceLevel level0 = AExperienceLevel.builder().level(0).build();
        AUserExperience experience = AUserExperience.builder().experience(40L).user(MockUtils.getUserObject(3L, server)).currentLevel(level0).build();
        List<AUserExperience> experiences = Arrays.asList(experience);

        List<AUserInAServer> users = experiences.stream().map(AUserExperience::getUser).collect(Collectors.toList());
        MemberImpl firstMember = MockUtils.getMockedMember(server, users.get(0), jda);
        when(botService.getMemberInServer(server, users.get(0).getUserReference())).thenReturn(firstMember);
        experience.setCurrentExperienceRole(previousRole);
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(usedExperienceRoles);
        when(experienceRoleService.calculateRole(experience, usedExperienceRoles)).thenReturn(afterRole);
        when(botService.getMemberInServer(server, experience.getUser().getUserReference())).thenReturn(firstMember);
        if(removals > 0) {
            users.forEach(aUserInAServer -> when(botService.isUserInGuild(aUserInAServer)).thenReturn(true));
        }
        if(afterRole != null && previousRole != null) {
            boolean sameRole = previousRole.getRole().getId().equals(afterRole.getRole().getId());
            when(roleService.memberHasRole(firstMember, afterRole.getRole())).thenReturn(sameRole);
        }
        testUnit.syncForSingleUser(experience);
        if(afterRole != null) {
            Assert.assertEquals(afterRole.getRole().getId(), experience.getCurrentExperienceRole().getRole().getId());
        } else {
            Assert.assertNull(experience.getCurrentExperienceRole());
        }
        if(previousRole != null) {
            verify(roleService, times(removals)).removeRoleFromUser(experience.getUser(), previousRole.getRole());
        }
        if(afterRole != null) {
            verify(roleService, times(adds)).addRoleToUser(experience.getUser(), afterRole.getRole());
        }
    }

    private void executeLeaderBoardTest(AServer server, Integer page) {
        int pageSize = 10;
        List<AUserExperience> experiences = getUserExperiences(pageSize, server);
        when(userExperienceManagementService.findLeaderboardUsersPaginated(server, (page - 1) * pageSize, page * pageSize)).thenReturn(experiences);
        LeaderBoard leaderBoardData = testUnit.findLeaderBoardData(server, page);
        page--;
        List<LeaderBoardEntry> entries = leaderBoardData.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            LeaderBoardEntry entry = entries.get(i);
            Assert.assertEquals(i, entry.getExperience().getExperience().longValue());
            Assert.assertEquals(i, entry.getExperience().getCurrentLevel().getLevel().intValue());
            Assert.assertEquals(i, entry.getExperience().getUser().getUserReference().getId().longValue());
            Assert.assertEquals((page * pageSize) + i + 1, entry.getRank().intValue());
        }
        Assert.assertEquals(pageSize, entries.size());
    }

    private AExperienceRole testRoleRelatedScenario(boolean shouldHaveRole, List<AExperienceLevel> levels, List<AServer> servers, AServer serverToUse, List<AExperienceRole> experienceRoles, AUserInAServer userToUse, AExperienceRole previousExperienceRole) {
        mockSimpleServer(levels, experienceRoles, serverToUse);
        MemberImpl jdaMember = MockUtils.getMockedMember(serverToUse, userToUse, jda);
        when(botService.getMemberInServer(serverToUse, userToUse.getUserReference())).thenReturn(jdaMember);
        AUserExperience newUserExperience = mockServerWithSingleUser(levels, serverToUse, 301L, 1, previousExperienceRole, false);

        AExperienceRole newAwardedRole = experienceRoles.get(3);
        when(experienceRoleService.calculateRole(newUserExperience, experienceRoles)).thenReturn(newAwardedRole);
        when(roleService.memberHasRole(jdaMember, newAwardedRole.getRole())).thenReturn(shouldHaveRole);
        testUnit.handleExperienceGain(servers);
        verify(userExperienceManagementService, times(1)).saveUser(eq(newUserExperience));
        Assert.assertEquals(3, newUserExperience.getCurrentLevel().getLevel().intValue());
        Assert.assertEquals(3L, newUserExperience.getCurrentExperienceRole().getRole().getId().longValue());
        return newAwardedRole;
    }

    private AUserExperience mockServerWithSingleUser(List<AExperienceLevel> levels, AServer serverToUse, Long experience, Integer currentLevelValue, AExperienceRole role, boolean hasExpDisabled) {
        Optional<AExperienceLevel> first = levels.stream().filter(level -> level.getLevel().equals(currentLevelValue)).findFirst();
        if(first.isPresent()) {
            AExperienceLevel currentLevel = first.get();
            AUserInAServer firstUser = serverToUse.getUsers().get(0);
            return mockUser(experience, currentLevel, firstUser, hasExpDisabled, role);
        }
        throw new AbstractoRunTimeException("No level found");
    }

    private AUserExperience mockUser(Long experience, AExperienceLevel currentLevel, AUserInAServer firstUser, boolean hasExpDisabled, AExperienceRole currentRole) {
        AUserExperience newUserExperience = AUserExperience.builder().currentLevel(currentLevel).experience(experience).user(firstUser).experienceGainDisabled(hasExpDisabled).currentExperienceRole(currentRole).build();
        when(userExperienceManagementService.incrementExpForUser(eq(firstUser), anyLong(), anyLong())).thenReturn(newUserExperience);
        return newUserExperience;
    }

    private void mockSimpleServer(List<AExperienceLevel> levels, List<AExperienceRole> experienceRoles, AServer server) {
        when(configService.getLongValue(ExperienceFeatureConfig.MIN_EXP_KEY, server.getId())).thenReturn(20L);
        when(configService.getLongValue(ExperienceFeatureConfig.MAX_EXP_KEY, server.getId())).thenReturn(50L);
        when(configService.getDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, server.getId())).thenReturn(1.2);
        when(experienceLevelManagementService.getLevelConfig()).thenReturn(levels);
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(new ArrayList<>());
    }

    private List<AServer> getOneServerWithOneUser() {
        AServer server = MockUtils.getServer(3L);
        MockUtils.getUserObject(3L, server);
        return Arrays.asList(server);
    }

    private List<AServer> getServersWithUserExperience() {
        AServer server = MockUtils.getServer(3L);
        AServer otherServer = MockUtils.getServer(3L);
        MockUtils.getUserObject(3L, server);
        MockUtils.getUserObject(4L, otherServer);

        return Arrays.asList(server, otherServer);
    }

}
