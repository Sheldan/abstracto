package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeatureConfig;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.RoleCalculationResult;
import dev.sheldan.abstracto.experience.models.ServerExperience;
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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    private RunTimeExperienceService runTimeExperienceService;

    @Captor
    private ArgumentCaptor<AUserExperience> aUserExperienceArgumentCaptor;

    @Mock
    private AUserExperienceServiceBean self;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private AUserExperience userExperience;

    @Mock
    private AUserExperience userExperience2;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private AUserInAServer aUserInAServer2;

    @Mock
    private AUser user;

    @Mock
    private AUser user2;

    @Mock
    private ServerExperience serverExperience;

    @Mock
    private ServerExperience serverExperience2;

    @Mock
    private AServer server;

    @Mock
    private Member firstMember;

    @Mock
    private Member secondMember;

    private static final Long USER_IN_SERVER_ID = 4L;
    private static final Long USER_ID = 8L;
    private static final Long SERVER_ID = 9L;

    @Test
    public void testCalculateLevelTooLow() {
        AUserExperience experienceToCalculate = AUserExperience.builder().experience(50L).build();
        List<AExperienceLevel> levels = getLevelConfiguration();
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(levels, experienceToCalculate.getExperience());
        Assert.assertEquals(0, calculatedLevel.getLevel().intValue());
    }

    @Test
    public void testCalculateLevelBetweenLevels() {
        AUserExperience experienceToCalculate = AUserExperience.builder().experience(250L).build();
        List<AExperienceLevel> levels = getLevelConfiguration();
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(levels, experienceToCalculate.getExperience());
        Assert.assertEquals(2, calculatedLevel.getLevel().intValue());
    }

    @Test
    public void testCalculateLevelTooHigh() {
        AUserExperience experienceToCalculate = AUserExperience.builder().experience(500L).build();
        List<AExperienceLevel> levels = getLevelConfiguration();
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(levels, experienceToCalculate.getExperience());
        Assert.assertEquals(3, calculatedLevel.getLevel().intValue());
    }

    @Test
    public void testUpdateUserExperienceLevelChanged() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        AUserInAServer userObject = MockUtils.getUserObject(2L, MockUtils.getServer());
        AUserExperience experienceToCalculate = AUserExperience.builder().user(userObject).currentLevel(levels.get(1)).experience(250L).build();
        Assert.assertTrue(testUnit.updateUserLevel(experienceToCalculate, levels, experienceToCalculate.getExperience()));
    }

    @Test
    public void testUpdateUserExperienceLevelNotChanged() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        AUserInAServer userObject = MockUtils.getUserObject(2L, MockUtils.getServer());
        AUserExperience experienceToCalculate = AUserExperience.builder().user(userObject).currentLevel(levels.get(2)).experience(250L).build();
        Assert.assertFalse(testUnit.updateUserLevel(experienceToCalculate, levels, experienceToCalculate.getExperience()));
    }

    @Test
    public void testGainExpSingleUserLvlUpOneServerWithoutRole() {
        /**
         * In this scenario, the user has a role before, but the config changed, and now there are no experience roles.
         * Hence the user should lose the experience role.
         */
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        AExperienceRole previousExperienceRole = experienceRoles.get(1);
        setupSimpleSingleUserTest(levels, experienceRoles);
        when(userExperience.getCurrentExperienceRole()).thenReturn(previousExperienceRole);
        when(roleService.removeRoleFromUserFuture(userExperience.getUser(), userExperience.getCurrentExperienceRole().getRole())).thenReturn(CompletableFuture.completedFuture(null));
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        testUnit.handleExperienceGain(servers).join();
        verify(roleService, times(1)).removeRoleFromUserFuture(aUserInAServer, previousExperienceRole.getRole());
        verify(roleService, times(0)).addRoleToUserFuture(eq(aUserInAServer), any());
    }

    @Test
    public void testLevelUpGainingNewRoleButUserAlreadyHasRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        AExperienceRole previousExperienceRole = experienceRoles.get(1);
        setupSimpleSingleUserTest(levels, experienceRoles);
        when(userExperience.getExperience()).thenReturn(199L);
        AExperienceRole newRole = experienceRoles.get(2);
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(newRole);
        AExperienceRole newAwardedRole = experienceRoles.get(3);
        when(roleService.memberHasRole(firstMember, newRole.getRole().getId())).thenReturn(true);
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        testUnit.handleExperienceGain(servers).join();
        verify(roleService, times(0)).addRoleToUser(aUserInAServer, newAwardedRole.getRole());
        verify(roleService, times(0)).removeRoleFromUser(aUserInAServer, previousExperienceRole.getRole());
    }

    @Test
    public void testLevelUpNotGainingNewRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        AExperienceRole previousExperienceRole = experienceRoles.get(1);
        setupSimpleSingleUserTest(levels, experienceRoles);
        AExperienceRole newRole = experienceRoles.get(1);
        when(userExperience.getCurrentExperienceRole()).thenReturn(newRole);
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(newRole);

        when(roleService.memberHasRole(firstMember, newRole.getRole().getId())).thenReturn(true);
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        testUnit.handleExperienceGain(servers).join();
        verify(roleService, times(0)).addRoleToUser(aUserInAServer, newRole.getRole());
        verify(roleService, times(0)).removeRoleFromUser(aUserInAServer, previousExperienceRole.getRole());
    }

    @Test
    public void testHandleExperienceForUserNotLevelingUpWithoutExistingRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        AExperienceRole previousExperienceRole = null;
        when(userExperience.getCurrentExperienceRole()).thenReturn(previousExperienceRole);
        setupSimpleSingleUserTest(levels, experienceRoles);
        when(botService.getMemberInServer(aUserInAServer)).thenReturn(firstMember);
        AExperienceRole newRole = experienceRoles.get(1);
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(newRole);

        when(roleService.memberHasRole(firstMember, newRole.getRole().getId())).thenReturn(false);
        when(roleService.addRoleToMemberFuture(firstMember, newRole.getRole().getId())).thenReturn(CompletableFuture.completedFuture(null));
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        when(roleService.addRoleToMemberFuture(firstMember, newRole.getRole().getId())).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.handleExperienceGain(servers).join();
    }

    @Test
    public void handleExpGainWithTooLittleForRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        AExperienceRole previousExperienceRole = null;
        setupSimpleSingleUserTest(levels, experienceRoles);
        when(userExperience.getExperience()).thenReturn(50L);
        when(serverExperience.getUserInServerIds()).thenReturn(Arrays.asList(USER_IN_SERVER_ID));
        when(userExperience.getCurrentExperienceRole()).thenReturn(previousExperienceRole);
        when(botService.getMemberInServer(aUserInAServer)).thenReturn(firstMember);
        AExperienceRole newRole = null;
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(newRole);

        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        testUnit.handleExperienceGain(servers).join();
        verify(roleService, times(0)).removeRoleFromUserFuture(eq(aUserInAServer), any());
        verify(roleService, times(0)).addRoleToUserFuture(eq(aUserInAServer), any());
    }

    @Test
    public void testUserHasExperienceRoleButNotAnymore() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        setupSimpleSingleUserTest(levels, experienceRoles);
        when(userExperience.getExperience()).thenReturn(50L);
        when(serverExperience.getUserInServerIds()).thenReturn(Arrays.asList(USER_IN_SERVER_ID));
        AExperienceRole previousExperienceRole = experienceRoles.get(0);
        when(userExperience.getCurrentExperienceRole()).thenReturn(previousExperienceRole);
        when(botService.getMemberInServer(aUserInAServer)).thenReturn(firstMember);
        AExperienceRole newRole = null;
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(newRole);

        when(roleService.removeRoleFromUserFuture(eq(aUserInAServer), any())).thenReturn(CompletableFuture.completedFuture(null));
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        testUnit.handleExperienceGain(servers).join();
        verify(roleService, times(0)).addRoleToUserFuture(eq(aUserInAServer), any());
        verify(roleService, times(1)).removeRoleFromUserFuture(eq(aUserInAServer), any());
    }

    @Test
    public void testHandleExperienceGainForGainDisabledForUser() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        setupServerConfig();
        when(serverManagementService.loadOrCreate(serverExperience.getServerId())).thenReturn(server);
        when(experienceLevelManagementService.getLevelConfig()).thenReturn(levels);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(new ArrayList<>());
        when(userInServerManagementService.loadUser(USER_IN_SERVER_ID)).thenReturn(aUserInAServer);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(serverExperience.getUserInServerIds()).thenReturn(Arrays.asList(USER_IN_SERVER_ID));
        when(botService.getMemberInServer(aUserInAServer)).thenReturn(firstMember);
        when(userExperience.getExperienceGainDisabled()).thenReturn(true);

        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        testUnit.handleExperienceGain(servers).join();
        verify(roleService, times(0)).removeRoleFromUserFuture(eq(aUserInAServer), any());
        verify(roleService, times(0)).addRoleToUserFuture(eq(aUserInAServer), any());
    }

    @Test
    public void testHandleExperienceGainForGainDisabledForRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        ServerExperience serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        mockSimpleServer(levels, experienceRoles, serverToUse);
        testUnit.handleExperienceGain(servers).join();
        verify(roleService, times(0)).removeRoleFromUser(aUserInAServer, experienceRoles.get(0).getRole());
        verify(roleService, times(0)).addRoleToUser(eq(aUserInAServer), any(ARole.class));
    }

    @Test
    public void testHandleExperienceForUserNotLevelingUpWithExistingRole() {
        List<AExperienceLevel> levels = getLevelConfiguration();
        List<ServerExperience> servers = Arrays.asList(serverExperience);
        when(serverExperience.getUserInServerIds()).thenReturn(Arrays.asList(USER_IN_SERVER_ID));
        ServerExperience serverToUse = servers.get(0);
        List<AExperienceRole> experienceRoles = getExperienceRoles(levels);
        mockSimpleServer(levels, experienceRoles, serverExperience);
        Long userToUse = serverToUse.getUserInServerIds().get(0);
        when(userExperienceManagementService.findByUserInServerIdOptional(userToUse)).thenReturn(Optional.of(userExperience));
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(userInServerManagementService.loadUser(userToUse)).thenReturn(aUserInAServer);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(8L);
        when(botService.getMemberInServer(aUserInAServer)).thenReturn(firstMember);
        when(roleService.hasAnyOfTheRoles(eq(firstMember), anyList())).thenReturn(false);
        when(user.getId()).thenReturn(7L);
        CompletableFuture<Void> future = testUnit.handleExperienceGain(servers);
        future.join();
        verify(roleService, times(0)).removeRoleFromUser(aUserInAServer, experienceRoles.get(0).getRole());
        verify(roleService, times(0)).addRoleToUser(eq(aUserInAServer), any(ARole.class));
    }

    @Test
    public void testSyncNoRoleUserGettingRole2() {
        AExperienceLevel level0 = Mockito.mock(AExperienceLevel.class);
        AExperienceLevel level1 = Mockito.mock(AExperienceLevel.class);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(Arrays.asList(level0, level1));
        userExperience.setCurrentExperienceRole(null);
        AExperienceRole afterRole = usedExperienceRoles.get(0);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);

        when(botService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(usedExperienceRoles);
        when(experienceRoleService.calculateRole(usedExperienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(botService.getMemberInServerAsync(userExperience.getUser())).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(roleService.addRoleToMemberFuture(firstMember, afterRole.getRole().getId())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<RoleCalculationResult> calculationFuture = testUnit.syncForSingleUser(userExperience);
        RoleCalculationResult result = calculationFuture.join();
        Assert.assertEquals(afterRole.getRole().getId(), result.getExperienceRoleId());
    }

    @Test
    public void testSyncUserLosingRole() {
        AExperienceLevel level0 = Mockito.mock(AExperienceLevel.class);
        AExperienceLevel level1 = Mockito.mock(AExperienceLevel.class);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(Arrays.asList(level0, level1));
        AExperienceRole beforeRole = usedExperienceRoles.get(0);
        when(userExperience.getCurrentExperienceRole()).thenReturn(beforeRole);
        AExperienceRole afterRole = null;
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(usedExperienceRoles);
        when(experienceRoleService.calculateRole(usedExperienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(roleService.removeRoleFromUserFuture(aUserInAServer, beforeRole.getRole())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<RoleCalculationResult> calculationFuture = testUnit.syncForSingleUser(userExperience);
        RoleCalculationResult result = calculationFuture.join();
        Assert.assertNull(result.getExperienceRoleId());
    }

    @Test
    public void testSyncUserKeepingRole() {
        AExperienceLevel level0 = Mockito.mock(AExperienceLevel.class);
        AExperienceLevel level1 = Mockito.mock(AExperienceLevel.class);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(Arrays.asList(level0, level1));
        AExperienceRole beforeRole = usedExperienceRoles.get(0);
        when(userExperience.getCurrentExperienceRole()).thenReturn(beforeRole);
        AExperienceRole afterRole = usedExperienceRoles.get(0);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);

        when(botService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(usedExperienceRoles);
        when(experienceRoleService.calculateRole(usedExperienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(botService.getMemberInServerAsync(userExperience.getUser())).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(roleService.memberHasRole(firstMember, afterRole.getRole().getId())).thenReturn(true);
        CompletableFuture<RoleCalculationResult> calculationFuture = testUnit.syncForSingleUser(userExperience);
        RoleCalculationResult result = calculationFuture.join();
        Assert.assertEquals(afterRole.getRole().getId(), result.getExperienceRoleId());
    }

    @Test
    public void testSyncUserChangingRole() {
        AExperienceLevel level0 = Mockito.mock(AExperienceLevel.class);
        AExperienceLevel level1 = Mockito.mock(AExperienceLevel.class);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(Arrays.asList(level0, level1));
        AExperienceRole beforeRole = usedExperienceRoles.get(0);
        when(userExperience.getCurrentExperienceRole()).thenReturn(beforeRole);
        AExperienceRole afterRole = usedExperienceRoles.get(1);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(roleService.memberHasRole(firstMember, beforeRole.getRole().getId())).thenReturn(true);

        when(botService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(usedExperienceRoles);
        when(experienceRoleService.calculateRole(usedExperienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(botService.getMemberInServerAsync(userExperience.getUser())).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(roleService.memberHasRole(firstMember, afterRole.getRole().getId())).thenReturn(false);
        when(roleService.removeRoleFromMemberAsync(firstMember, beforeRole.getRole().getId())).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToMemberFuture(firstMember, afterRole.getRole().getId())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<RoleCalculationResult> calculationFuture = testUnit.syncForSingleUser(userExperience);
        RoleCalculationResult result = calculationFuture.join();
        Assert.assertEquals(afterRole.getRole().getId(), result.getExperienceRoleId());
    }

    @Test
    public void testDisablingExperienceForUser() {
        AUserInAServer userObject = MockUtils.getUserObject(2L, server);
        AUserExperience experience = AUserExperience.builder().user(userObject).experienceGainDisabled(false).build();
        when(userExperienceManagementService.findUserInServer(userObject)).thenReturn(experience);
        testUnit.disableExperienceForUser(userObject);
        Assert.assertTrue(experience.getExperienceGainDisabled());
    }

    @Test
    public void testDisablingExpForUserWhichHasItDisabled() {
        AUserInAServer userObject = MockUtils.getUserObject(2L, server);
        AUserExperience experience = AUserExperience.builder().user(userObject).experienceGainDisabled(true).build();
        when(userExperienceManagementService.findUserInServer(userObject)).thenReturn(experience);
        testUnit.disableExperienceForUser(userObject);
        Assert.assertTrue(experience.getExperienceGainDisabled());
    }

    @Test
    public void testEnablingExperienceForEnabledUser() {
        AUserInAServer userObject = MockUtils.getUserObject(2L, server);
        AUserExperience experience = AUserExperience.builder().user(userObject).experienceGainDisabled(false).build();
        when(userExperienceManagementService.findUserInServer(userObject)).thenReturn(experience);
        testUnit.enableExperienceForUser(userObject);
        Assert.assertFalse(experience.getExperienceGainDisabled());
    }

    @Test
    public void testEnablingExpForUserWhichHasItDisabled() {
        AUserInAServer userObject = MockUtils.getUserObject(2L, server);
        AUserExperience experience = AUserExperience.builder().user(userObject).experienceGainDisabled(true).build();
        when(userExperienceManagementService.findUserInServer(userObject)).thenReturn(experience);
        testUnit.enableExperienceForUser(userObject);
        Assert.assertFalse(experience.getExperienceGainDisabled());
    }

    @Test
    public void testFindLeaderBoardData() {
        executeLeaderBoardTest(server, 1);
    }

    @Test
    public void testFindLeaderBoardDataSecondPage() {
        executeLeaderBoardTest(server, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalLeaderBoardPage() {
        testUnit.findLeaderBoardData(MockUtils.getServer(1L), -1);
    }

    @Test
    public void testSyncAllUsers() {
        AExperienceLevel level0 = Mockito.mock(AExperienceLevel.class);
        AExperienceLevel level1 = Mockito.mock(AExperienceLevel.class);
        List<AExperienceRole> usedExperienceRoles = getExperienceRoles(Arrays.asList(level0, level1));
        AExperienceRole beforeRole = usedExperienceRoles.get(0);

        when(userExperience.getCurrentExperienceRole()).thenReturn(beforeRole);
        AExperienceRole afterRole = usedExperienceRoles.get(1);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(user.getId()).thenReturn(8L);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(userExperience.getCurrentLevel()).thenReturn(level0);

        when(aUserInAServer2.getUserReference()).thenReturn(user2);
        when(user2.getId()).thenReturn(9L);
        when(aUserInAServer2.getServerReference()).thenReturn(server);
        when(userExperience2.getUser()).thenReturn(aUserInAServer2);
        when(userExperience2.getCurrentLevel()).thenReturn(level0);

        when(userExperience2.getCurrentExperienceRole()).thenReturn(beforeRole);

        when(botService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(botService.getMemberInServerAsync(aUserInAServer2)).thenReturn(CompletableFuture.completedFuture(secondMember));

        when(server.getId()).thenReturn(15L);

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(usedExperienceRoles);
        when(experienceRoleService.calculateRole(usedExperienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(experienceRoleService.calculateRole(usedExperienceRoles, userExperience2.getLevelOrDefault())).thenReturn(afterRole);
        when(botService.getMemberInServerAsync(userExperience.getUser())).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(botService.getMemberInServerAsync(userExperience2.getUser())).thenReturn(CompletableFuture.completedFuture(secondMember));
        when(roleService.memberHasRole(firstMember, afterRole.getRole().getId())).thenReturn(false);
        when(roleService.memberHasRole(firstMember, beforeRole.getRole().getId())).thenReturn(true);
        when(roleService.memberHasRole(secondMember, afterRole.getRole().getId())).thenReturn(true);
        when(roleService.removeRoleFromMemberAsync(firstMember, beforeRole.getRole().getId())).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToMemberFuture(firstMember, afterRole.getRole().getId())).thenReturn(CompletableFuture.completedFuture(null));
        List<AUserExperience> experiences = Arrays.asList(userExperience, userExperience2);
        when(userExperienceManagementService.loadAllUsers(server)).thenReturn(experiences);
        List<CompletableFuture<RoleCalculationResult>> calculationFutures = testUnit.syncUserRoles(server);
        verify(roleService, times(0)).removeRoleFromMemberAsync(secondMember, beforeRole.getRole().getId());
        verify(roleService, times(0)).addRoleToMemberFuture(secondMember, afterRole.getRole().getId());
        RoleCalculationResult firstResult = calculationFutures.get(0).join();
        Assert.assertEquals(afterRole.getRole().getId(), firstResult.getExperienceRoleId());
        RoleCalculationResult secondResult = calculationFutures.get(1).join();
        Assert.assertEquals(afterRole.getRole().getId(), secondResult.getExperienceRoleId());
    }

    @Test
    public void testGetRankForUser() {
        long experience = 1L;
        int level = 1;
        long messageCount = 1L;
        int rank = 1;
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
        AChannel channel = AChannel.builder().id(2L).build();
        List<AUserExperience> experiences = getUserExperiences(25, server);

        checkStatusMessages(server, channel, experiences, 13);
    }

    @Test
    public void testSyncRolesWithNoUsers() {
        AChannel channel = AChannel.builder().id(2L).build();
        List<AUserExperience> experiences = new ArrayList<>();

        checkStatusMessages(server, channel, experiences, 1);
    }

    private void setupServerConfig() {
        when(configService.getLongValue(ExperienceFeatureConfig.MIN_EXP_KEY, serverExperience.getServerId())).thenReturn(20L);
        when(configService.getLongValue(ExperienceFeatureConfig.MAX_EXP_KEY, serverExperience.getServerId())).thenReturn(50L);
        when(configService.getDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, serverExperience.getServerId())).thenReturn(1.2);
    }

    private void setupSimpleSingleUserTest(List<AExperienceLevel> levels, List<AExperienceRole> experienceRoles) {
        setupServerConfig();
        when(serverManagementService.loadOrCreate(serverExperience.getServerId())).thenReturn(server);
        when(serverExperience.getUserInServerIds()).thenReturn(Arrays.asList(USER_IN_SERVER_ID));
        when(experienceLevelManagementService.getLevelConfig()).thenReturn(levels);
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(new ArrayList<>());
        when(userInServerManagementService.loadUser(USER_IN_SERVER_ID)).thenReturn(aUserInAServer);
        when(user.getId()).thenReturn(USER_ID);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(botService.getMemberInServer(aUserInAServer)).thenReturn(firstMember);
        when(botService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(userExperience.getExperience()).thenReturn(500L);
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(user.getId()).thenReturn(USER_ID);
    }

    private void checkStatusMessages(AServer server, AChannel channel, List<AUserExperience> experiences, int messageCount) {
        when(userExperienceManagementService.loadAllUsers(server)).thenReturn(experiences);
        MessageToSend statusMessage = MessageToSend.builder().message("text").build();
        when(templateService.renderEmbedTemplate(eq("user_sync_status_message"), any(UserSyncStatusModel.class))).thenReturn(statusMessage);
        long messageId = 5L;
        Message statusMessageJDA = Mockito.mock(Message.class);
        when(statusMessageJDA.getIdLong()).thenReturn(messageId);
        when(messageService.createStatusMessage(statusMessage, channel)).thenReturn(CompletableFuture.completedFuture(statusMessageJDA));
        testUnit.syncUserRolesWithFeedback(server, channel);
        verify(messageService, times(messageCount)).updateStatusMessage(channel, messageId, statusMessage);
    }

    private void executeLeaderBoardTest(AServer server, Integer page) {
        int pageSize = 10;
        List<AUserExperience> experiences = getUserExperiences(pageSize, server);
        when(userExperienceManagementService.findLeaderBoardUsersPaginated(server, (page - 1) * pageSize, page * pageSize)).thenReturn(experiences);
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

    private void mockSimpleServer(List<AExperienceLevel> levels, List<AExperienceRole> experienceRoles, ServerExperience serverExperience) {
        when(configService.getLongValue(ExperienceFeatureConfig.MIN_EXP_KEY, serverExperience.getServerId())).thenReturn(20L);
        when(configService.getLongValue(ExperienceFeatureConfig.MAX_EXP_KEY, serverExperience.getServerId())).thenReturn(50L);
        when(configService.getDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, serverExperience.getServerId())).thenReturn(1.2);
        when(serverManagementService.loadOrCreate(serverExperience.getServerId())).thenReturn(server);
        when(experienceLevelManagementService.getLevelConfig()).thenReturn(levels);
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(new ArrayList<>());
    }


}
