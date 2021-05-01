package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureConfig;
import dev.sheldan.abstracto.experience.model.*;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.model.template.UserSyncStatusModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AUserExperienceServiceBeanTest {

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
    private ChannelManagementService channelManagementService;

    @Mock
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Mock
    private MemberService memberService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private AUserExperienceServiceBean self;

    @Mock
    private DefaultConfigManagementService defaultConfigManagementService;

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
    private AServer server;

    @Mock
    private Member firstMember;

    @Mock
    private Member secondMember;

    @Mock
    private AExperienceLevel level0;

    @Mock
    private AExperienceLevel level1;

    @Mock
    private AExperienceLevel level2;

    @Mock
    private AExperienceLevel level3;

    @Mock
    private AExperienceRole experienceRole1;

    @Mock
    private ARole aRole1;

    @Mock
    private AExperienceRole experienceRole2;

    @Mock
    private ARole aRole2;

    private List<AExperienceLevel> levels = new ArrayList<>();
    private List<AExperienceRole> experienceRoles = new ArrayList<>();

    private static final Long USER_IN_SERVER_ID = 4L;
    private static final Long USER_ID = 8L;
    private static final Long SERVER_ID = 9L;
    private static final Long CHANNEL_ID = 7L;
    private static final Long DEFAULT_MIN_EXP = 10L;
    private static final Long DEFAULT_MAX_EXP = 25L;
    private static final Double DEFAULT_EXP_MULTIPLIER = 1D;
    private static final Long LOW_EXP = 50L;
    private static final Long MID_EXP = 250L;
    private static final Long HIGH_EXP = 500L;
    private static final Long LVL_0_EXP = 0L;
    private static final Long LVL_1_EXP = 100L;
    private static final Long LVL_2_EXP = 200L;
    private static final Long LVL_3_EXP = 300L;
    private static final Integer ZERO_LVL = 0;
    private static final Integer SECOND_LVL = 2;

    private static final Long ROLE_ID = 4L;
    private static final Long SECOND_ROLE_ID = 7L;
    private static final Long MESSAGE_COUNT = 10L;

    @Before
    public void setup() {
        this.levels = Arrays.asList(level0, level1, level2, level3);
        this.experienceRoles = Arrays.asList(experienceRole1, experienceRole2);
    }

    @Test
    public void testCalculateLevelTooLow() {
        this.levels = Arrays.asList(level0, level1);
        setupLevels(1);
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(levels, LOW_EXP);
        Assert.assertEquals(level0, calculatedLevel);
    }

    @Test
    public void testCalculateLevelBetweenLevels() {
        this.levels = Arrays.asList(level0, level1, level2);
        setupLevels(3);
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(levels, MID_EXP);
        Assert.assertEquals(level2, calculatedLevel);
    }

    @Test
    public void testCalculateLevelTooHigh() {
        this.levels = Arrays.asList(level0, level1, level2, level3);
        setupLevels(3);
        AExperienceLevel calculatedLevel = testUnit.calculateLevel(levels, HIGH_EXP);
        Assert.assertEquals(level3, calculatedLevel);
    }

    @Test
    public void testUpdateUserExperienceLevelChanged() {
        AUserExperience experienceToCalculate = Mockito.mock(AUserExperience.class);
        when(experienceToCalculate.getUser()).thenReturn(aUserInAServer);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(experienceToCalculate.getCurrentLevel()).thenReturn(level0);
        when(level0.getLevel()).thenReturn(ZERO_LVL);
        when(level2.getLevel()).thenReturn(SECOND_LVL);
        setupLevels(3);
        Assert.assertTrue(testUnit.updateUserLevel(experienceToCalculate, levels, MID_EXP));
        verify(experienceToCalculate, times(1)).setCurrentLevel(level2);
    }

    @Test
    public void testUpdateUserExperienceLevelNotChanged() {
        AUserExperience experienceToCalculate = Mockito.mock(AUserExperience.class);
        when(experienceToCalculate.getCurrentLevel()).thenReturn(level2);
        when(experienceToCalculate.getExperience()).thenReturn(MID_EXP);
        setupLevels(3);
        Assert.assertFalse(testUnit.updateUserLevel(experienceToCalculate, levels, experienceToCalculate.getExperience()));
    }

    @Test
    public void testHandleExperienceGainSingleUser() {
        when(serverExperience.getUserInServerIds()).thenReturn(Arrays.asList(USER_IN_SERVER_ID));
        when(userInServerManagementService.loadOrCreateUser(USER_IN_SERVER_ID)).thenReturn(aUserInAServer);
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(serverExperience.getServerId()).thenReturn(SERVER_ID);
        testUnit.handleExperienceGain(Arrays.asList(serverExperience)).join();
        ArgumentCaptor<List<CompletableFuture<Member>>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(self, times(1)).updateFoundMembers(listArgumentCaptor.capture(), eq(SERVER_ID), anyList(), anyList());
        Assert.assertEquals(firstMember, listArgumentCaptor.getValue().get(0).join());
    }

    @Test
    public void testHandleExperienceMemberFailed() {
        when(serverExperience.getUserInServerIds()).thenReturn(Arrays.asList(USER_IN_SERVER_ID));
        when(userInServerManagementService.loadOrCreateUser(USER_IN_SERVER_ID)).thenReturn(aUserInAServer);
        CompletableFuture<Member> future = new CompletableFuture<>();
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(future);
        future.completeExceptionally(new RuntimeException());
        when(serverExperience.getServerId()).thenReturn(SERVER_ID);
        testUnit.handleExperienceGain(Arrays.asList(serverExperience)).join();
        ArgumentCaptor<List<CompletableFuture<Member>>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(self, times(1)).updateFoundMembers(listArgumentCaptor.capture(), eq(SERVER_ID), anyList(), anyList());
        Assert.assertTrue(listArgumentCaptor.getValue().get(0).isCompletedExceptionally());
    }

    @Test
    public void testGainExpSingleUserLvlUpOneServerWithoutRole() {
        /*
         * In this scenario, the user has a role before, but the config changed, and now there are no experience roles.
         * Hence the user should lose the experience role.
         */
        setupServerId();
        setupLevels(3);
        when(experienceRole1.getRole()).thenReturn(aRole1);
        setExperienceRoleLevels();
        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();
        setupUserInServer();
        when(userExperience.getMessageCount()).thenReturn(MESSAGE_COUNT);
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(null);
        when(userExperience.getExperience()).thenReturn(500L);
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        when(userExperience.getCurrentExperienceRole()).thenReturn(experienceRole1);
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(roleService.removeRoleFromUserFuture(aUserInAServer, aRole1)).thenReturn(CompletableFuture.completedFuture(null));
        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        Assert.assertEquals(1, experienceResults.size());
        ExperienceGainResult result = experienceResults.get(0);
        Assert.assertEquals(MESSAGE_COUNT + 1, result.getNewMessageCount().longValue());
        Assert.assertEquals(1, roleCalculationResults.size());
        RoleCalculationResult roleCalcResult = roleCalculationResults.get(0).join();
        Assert.assertNull(roleCalcResult.getExperienceRoleId());
        Assert.assertEquals(USER_IN_SERVER_ID, roleCalcResult.getUserInServerId());
        verify(roleService, times(1)).removeRoleFromUserFuture(aUserInAServer, aRole1);
        verify(roleService, times(0)).addRoleToUserFuture(any(AUserInAServer.class), any());
    }

    @Test
    public void testLevelUpGainingNewRoleButUserAlreadyHasRole() {
        setupServerId();
        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        setupUserInServer();
        when(userExperience.getExperience()).thenReturn(199L);
        when(experienceRole1.getLevel()).thenReturn(level0);
        when(experienceRole2.getRole()).thenReturn(aRole2);
        when(experienceRole2.getLevel()).thenReturn(level1);
        AExperienceRole newRole = experienceRole2;
        when(aRole2.getId()).thenReturn(ROLE_ID);
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(newRole);
        when(roleService.memberHasRole(firstMember, ROLE_ID)).thenReturn(true);
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        verify(roleService, times(0)).addRoleToUser(any(AUserInAServer.class), any(ARole.class));
        verify(roleService, times(0)).removeRoleFromUser(any(AUserInAServer.class), any(ARole.class));
    }

    @Test
    public void testLevelUpNotGainingNewRole() {
        setupServerId();
        when(experienceRole1.getRole()).thenReturn(aRole1);
        setExperienceRoleLevels();
        when(aRole1.getId()).thenReturn(ROLE_ID);
        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        setupUserInServer();
        when(userExperience.getExperience()).thenReturn(500L);
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        AExperienceRole newRole = experienceRole1;
        when(userExperience.getCurrentExperienceRole()).thenReturn(newRole);
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(newRole);

        when(roleService.memberHasRole(firstMember, ROLE_ID)).thenReturn(true);
        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        verify(roleService, times(0)).addRoleToUser(any(AUserInAServer.class), any(ARole.class));
        verify(roleService, times(0)).removeRoleFromUser(any(AUserInAServer.class), any(ARole.class));
    }

    @Test
    public void testHandleExperienceForUserNotLevelingUpWithoutExistingRole() {
        setupServerId();
        when(userExperience.getCurrentExperienceRole()).thenReturn(null);
        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        setupUserInServer();
        when(userExperience.getExperience()).thenReturn(500L);
        AExperienceRole newRole = experienceRole1;
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(newRole);
        when(aRole1.getId()).thenReturn(ROLE_ID);
        setupTwoExperienceRoles();
        when(roleService.memberHasRole(firstMember, ROLE_ID)).thenReturn(false);
        when(roleService.addRoleToMemberFuture(firstMember, ROLE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        when(roleService.addRoleToMemberFuture(firstMember, ROLE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        verify(roleService, times(0)).addRoleToUserFuture(any(AUserInAServer.class), any(ARole.class));
        verify(roleService, times(0)).removeRoleFromUserFuture(any(AUserInAServer.class), any());
    }

    @Test
    public void handleExpGainWithTooLittleForRole() {
        setupServerId();
        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();
        setupUserInServer();
        when(userExperience.getExperience()).thenReturn(50L);
        setExperienceRoleLevels();
        when(userExperience.getCurrentExperienceRole()).thenReturn(null);
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(null);

        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        verify(roleService, times(0)).removeRoleFromUserFuture(any(AUserInAServer.class), any());
        verify(roleService, times(0)).addRoleToUserFuture(any(AUserInAServer.class), any());
    }

    @Test
    public void testUserHasExperienceRoleButNotAnymore() {
        setupServerId();
        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();
        setupUserInServer();
        when(userExperience.getExperience()).thenReturn(50L);
        AExperienceRole previousExperienceRole = experienceRole1;
        when(userExperience.getCurrentExperienceRole()).thenReturn(previousExperienceRole);
        when(experienceRole1.getRole()).thenReturn(aRole1);
        setExperienceRoleLevels();
        when(experienceRoleService.calculateRole(eq(experienceRoles), any())).thenReturn(null);

        when(roleService.removeRoleFromUserFuture(eq(aUserInAServer), any())).thenReturn(CompletableFuture.completedFuture(null));
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        verify(roleService, times(0)).addRoleToUserFuture(eq(aUserInAServer), any());
        verify(roleService, times(1)).removeRoleFromUserFuture(eq(aUserInAServer), any());
    }

    @Test
    public void testHandleExperienceGainForGainDisabledForUser() {
        setupServerId();
        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();
        setExperienceRoleLevels();
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(userExperience.getExperienceGainDisabled()).thenReturn(true);

        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        when(aUserInAServer.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        when(userInServerManagementService.loadOrCreateUser(firstMember)).thenReturn(aUserInAServer);
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));

        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        verify(roleService, times(0)).removeRoleFromUserFuture(eq(aUserInAServer), any());
        verify(roleService, times(0)).addRoleToUserFuture(eq(aUserInAServer), any());
    }

    @Test
    public void testHandleExperienceGainForGainDisabledForRole() {
        setupServerId();
        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();
        setExperienceRoleLevels();
        when(experienceRole1.getLevel()).thenReturn(level0);
        when(experienceRole2.getLevel()).thenReturn(level1);
        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        when(roleService.hasAnyOfTheRoles(eq(firstMember), anyList())).thenReturn(true);
        when(aUserInAServer.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        when(userInServerManagementService.loadOrCreateUser(firstMember)).thenReturn(aUserInAServer);
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        verify(roleService, times(0)).removeRoleFromUser(aUserInAServer, aRole1);
        verify(roleService, times(0)).addRoleToUser(eq(aUserInAServer), any(ARole.class));
    }

    @Test
    public void testHandleExperienceForUserNotLevelingUpWithExistingRole() {
        setupServerId();
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(roleService.hasAnyOfTheRoles(eq(firstMember), anyList())).thenReturn(false);
        when(user.getId()).thenReturn(USER_ID);
        setExperienceRoleLevels();

        setupServerConfig();
        setupDefaultConfig();
        setupLevelsAndRolesAndNoDisallowed();

        when(aUserInAServer.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        when(userInServerManagementService.loadOrCreateUser(firstMember)).thenReturn(aUserInAServer);

        ArrayList<ExperienceGainResult> experienceResults = new ArrayList<>();
        ArrayList<CompletableFuture<RoleCalculationResult>> roleCalculationResults = new ArrayList<>();
        List<CompletableFuture<Member>> memberFutures = Arrays.asList(CompletableFuture.completedFuture(firstMember));
        testUnit.updateFoundMembers(memberFutures, SERVER_ID, experienceResults, roleCalculationResults);
        verify(roleService, times(0)).removeRoleFromUser(aUserInAServer, aRole1);
        verify(roleService, times(0)).addRoleToUser(eq(aUserInAServer), any(ARole.class));
    }

    @Test
    public void testSyncNoRoleUserGettingRole2() {
        userExperience.setCurrentExperienceRole(null);
        AExperienceRole afterRole = experienceRole1;
        when(experienceRole1.getRole()).thenReturn(aRole1);
        when(experienceRole1.getId()).thenReturn(ROLE_ID);
        when(aRole1.getId()).thenReturn(ROLE_ID);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);

        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(experienceRole1.getLevel()).thenReturn(level0);
        when(experienceRole2.getLevel()).thenReturn(level1);
        when(experienceRoleService.calculateRole(experienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(memberService.getMemberInServerAsync(userExperience.getUser())).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(roleService.addRoleToMemberFuture(firstMember, ROLE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<RoleCalculationResult> calculationFuture = testUnit.syncForSingleUser(userExperience);
        RoleCalculationResult result = calculationFuture.join();
        Assert.assertEquals(ROLE_ID, result.getExperienceRoleId());
    }

    @Test
    public void testSyncUserLosingRole() {
        AExperienceRole beforeRole = experienceRole1;
        when(userExperience.getCurrentExperienceRole()).thenReturn(beforeRole);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(experienceRole1.getRole()).thenReturn(aRole1);

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(experienceRoleService.calculateRole(experienceRoles, userExperience.getLevelOrDefault())).thenReturn(null);
        when(roleService.removeRoleFromUserFuture(aUserInAServer, aRole1)).thenReturn(CompletableFuture.completedFuture(null));
        when(experienceRole1.getLevel()).thenReturn(level0);
        when(experienceRole2.getLevel()).thenReturn(level1);
        CompletableFuture<RoleCalculationResult> calculationFuture = testUnit.syncForSingleUser(userExperience);
        RoleCalculationResult result = calculationFuture.join();
        Assert.assertNull(result.getExperienceRoleId());
    }

    @Test
    public void testSyncUserKeepingRole() {
        AExperienceRole beforeRole = experienceRole1;
        when(userExperience.getCurrentExperienceRole()).thenReturn(beforeRole);
        AExperienceRole afterRole = experienceRole1;
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(aRole1.getId()).thenReturn(ROLE_ID);
        when(experienceRole1.getId()).thenReturn(ROLE_ID);
        when(experienceRole1.getRole()).thenReturn(aRole1);
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(experienceRoleService.calculateRole(experienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(memberService.getMemberInServerAsync(userExperience.getUser())).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(roleService.memberHasRole(firstMember, ROLE_ID)).thenReturn(true);
        when(experienceRole1.getLevel()).thenReturn(level0);
        when(experienceRole2.getLevel()).thenReturn(level1);
        CompletableFuture<RoleCalculationResult> calculationFuture = testUnit.syncForSingleUser(userExperience);
        RoleCalculationResult result = calculationFuture.join();
        Assert.assertEquals(ROLE_ID, result.getExperienceRoleId());
    }

    @Test
    public void testSyncUserChangingRole() {
        AExperienceRole beforeRole = experienceRole1;
        when(userExperience.getCurrentExperienceRole()).thenReturn(beforeRole);
        AExperienceRole afterRole = experienceRole2;
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperience.getUser()).thenReturn(aUserInAServer);

        when(aRole1.getId()).thenReturn(ROLE_ID);
        when(aRole2.getId()).thenReturn(SECOND_ROLE_ID);
        when(experienceRole1.getRole()).thenReturn(aRole1);
        when(experienceRole2.getRole()).thenReturn(aRole2);
        when(experienceRole2.getId()).thenReturn(SECOND_ROLE_ID);

        when(roleService.memberHasRole(firstMember, ROLE_ID)).thenReturn(true);

        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(experienceRoleService.calculateRole(experienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(experienceRole1.getLevel()).thenReturn(level0);
        when(experienceRole2.getLevel()).thenReturn(level1);
        when(roleService.memberHasRole(firstMember, SECOND_ROLE_ID)).thenReturn(false);
        when(roleService.removeRoleFromMemberAsync(firstMember, ROLE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToMemberFuture(firstMember, SECOND_ROLE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<RoleCalculationResult> calculationFuture = testUnit.syncForSingleUser(userExperience);
        RoleCalculationResult result = calculationFuture.join();
        Assert.assertEquals(SECOND_ROLE_ID, result.getExperienceRoleId());
    }

    @Test
    public void testDisablingExperienceForUser() {
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(experience);
        testUnit.disableExperienceForUser(aUserInAServer);
        verify(experience, times(1)).setExperienceGainDisabled(true);
    }

    @Test
    public void testEnablingExpForUser() {
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(experience);
        testUnit.enableExperienceForUser(aUserInAServer);
        verify(experience, times(1)).setExperienceGainDisabled(false);
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
        testUnit.findLeaderBoardData(server, -1);
    }

    @Test
    public void testSyncAllUsers() {
        AExperienceRole beforeRole = experienceRole1;

        when(userExperience.getCurrentExperienceRole()).thenReturn(beforeRole);
        AExperienceRole afterRole = experienceRole2;
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

        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(memberService.getMemberInServerAsync(aUserInAServer2)).thenReturn(CompletableFuture.completedFuture(secondMember));


        when(aRole1.getId()).thenReturn(ROLE_ID);
        when(aRole2.getId()).thenReturn(SECOND_ROLE_ID);
        when(experienceRole1.getRole()).thenReturn(aRole1);
        when(experienceRole2.getRole()).thenReturn(aRole2);
        when(experienceRole2.getId()).thenReturn(SECOND_ROLE_ID);

        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(experienceRoleService.calculateRole(experienceRoles, userExperience.getLevelOrDefault())).thenReturn(afterRole);
        when(experienceRoleService.calculateRole(experienceRoles, userExperience2.getLevelOrDefault())).thenReturn(afterRole);
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(memberService.getMemberInServerAsync(aUserInAServer2)).thenReturn(CompletableFuture.completedFuture(secondMember));
        when(roleService.memberHasRole(firstMember, SECOND_ROLE_ID)).thenReturn(false);
        when(roleService.memberHasRole(firstMember, ROLE_ID)).thenReturn(true);
        when(roleService.memberHasRole(secondMember, SECOND_ROLE_ID)).thenReturn(true);
        when(roleService.removeRoleFromMemberAsync(firstMember, ROLE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToMemberFuture(firstMember,SECOND_ROLE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        List<AUserExperience> experiences = Arrays.asList(userExperience, userExperience2);
        when(userExperienceManagementService.loadAllUsers(server)).thenReturn(experiences);
        when(experienceRole1.getLevel()).thenReturn(level0);
        when(experienceRole2.getLevel()).thenReturn(level1);
        List<CompletableFuture<RoleCalculationResult>> calculationFutures = testUnit.syncUserRoles(server);
        verify(roleService, times(0)).removeRoleFromMemberAsync(secondMember, ROLE_ID);
        verify(roleService, times(0)).addRoleToMemberFuture(secondMember, SECOND_ROLE_ID);
        RoleCalculationResult firstResult = calculationFutures.get(0).join();
        Assert.assertEquals(SECOND_ROLE_ID, firstResult.getExperienceRoleId());
        RoleCalculationResult secondResult = calculationFutures.get(1).join();
        Assert.assertEquals(SECOND_ROLE_ID, secondResult.getExperienceRoleId());
    }

    @Test
    public void testGetRankForUser() {
        int rank = 1;
        AUserExperience experienceObj = Mockito.mock(AUserExperience.class);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(experienceObj);
        LeaderBoardEntryResult leaderBoardEntryTest = Mockito.mock(LeaderBoardEntryResult.class);
        when(leaderBoardEntryTest.getRank()).thenReturn(rank);
        when(userExperienceManagementService.getRankOfUserInServer(experienceObj)).thenReturn(leaderBoardEntryTest);
        LeaderBoardEntry rankOfUserInServer = testUnit.getRankOfUserInServer(aUserInAServer);
        Assert.assertEquals(experienceObj, rankOfUserInServer.getExperience());
        Assert.assertEquals(rank, rankOfUserInServer.getRank().intValue());
    }

    @Test
    public void testGetRankForUserNotExisting() {
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(null);
        LeaderBoardEntry rankOfUserInServer = testUnit.getRankOfUserInServer(aUserInAServer);
        Assert.assertNull(rankOfUserInServer.getExperience());
        Assert.assertEquals(0, rankOfUserInServer.getRank().intValue());
    }

    @Test
    public void testGetRankWhenRankReturnsNull() {
        AUserExperience experienceObj = Mockito.mock(AUserExperience.class);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(experienceObj);
        when(userExperienceManagementService.getRankOfUserInServer(experienceObj)).thenReturn(null);
        LeaderBoardEntry rankOfUserInServer = testUnit.getRankOfUserInServer(aUserInAServer);
        Assert.assertEquals(experienceObj, rankOfUserInServer.getExperience());
        Assert.assertEquals(0, rankOfUserInServer.getRank().intValue());
    }

    @Test
    public void testSyncRolesWithFeedBack() {
        AChannel channel = Mockito.mock(AChannel.class);
        when(channel.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        List<AUserExperience> experiences = getUserExperiences(25);

        checkStatusMessages(server, channel, experiences, 13);
    }

    @Test
    public void testSyncRolesWithNoUsers() {
        AChannel channel = Mockito.mock(AChannel.class);
        List<AUserExperience> experiences = new ArrayList<>();
        when(channel.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        checkStatusMessages(server, channel, experiences, 1);
    }

    private void checkStatusMessages(AServer server, AChannel channel, List<AUserExperience> experiences, int messageCount) {
        when(userExperienceManagementService.loadAllUsers(server)).thenReturn(experiences);
        MessageToSend statusMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq("user_sync_status_message"), any(UserSyncStatusModel.class), eq(SERVER_ID))).thenReturn(statusMessage);
        long messageId = 5L;
        Message statusMessageJDA = Mockito.mock(Message.class);
        when(statusMessageJDA.getIdLong()).thenReturn(messageId);
        when(messageService.createStatusMessage(statusMessage, channel)).thenReturn(CompletableFuture.completedFuture(statusMessageJDA));
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(channel);
        testUnit.syncUserRolesWithFeedback(server, CHANNEL_ID);
        verify(messageService, times(messageCount)).updateStatusMessage(channel, messageId, statusMessage);
    }

    private void setupUserInServer() {
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(userExperience));
        when(userInServerManagementService.loadOrCreateUser(firstMember)).thenReturn(aUserInAServer);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(aUserInAServer.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        when(memberService.getMemberInServerAsync(aUserInAServer)).thenReturn(CompletableFuture.completedFuture(firstMember));
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(user.getId()).thenReturn(USER_ID);
    }

    private void setupTwoExperienceRoles() {
        when(experienceRole1.getRole()).thenReturn(aRole1);
        setExperienceRoleLevels();
    }

    private void setupServerId() {
        when(server.getId()).thenReturn(SERVER_ID);
        when(serverManagementService.loadOrCreate(SERVER_ID)).thenReturn(server);
    }

    private void setupServerConfig() {
        when(configService.getLongValue(ExperienceFeatureConfig.MIN_EXP_KEY, SERVER_ID, DEFAULT_MIN_EXP)).thenReturn(20L);
        when(configService.getLongValue(ExperienceFeatureConfig.MAX_EXP_KEY, SERVER_ID, DEFAULT_MAX_EXP)).thenReturn(50L);
        when(configService.getDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, SERVER_ID, DEFAULT_EXP_MULTIPLIER)).thenReturn(1.2);
    }

    private void executeLeaderBoardTest(AServer server, Integer page) {
        int pageSize = 10;
        List<AUserExperience> experiences = Arrays.asList(userExperience, userExperience2);
        when(userExperience.getExperience()).thenReturn(LOW_EXP);
        when(userExperience.getCurrentLevel()).thenReturn(level0);
        when(userExperience.getUser()).thenReturn(aUserInAServer);
        when(userExperience2.getExperience()).thenReturn(MID_EXP);
        when(userExperience2.getCurrentLevel()).thenReturn(level1);
        when(userExperience2.getUser()).thenReturn(aUserInAServer2);
        when(userExperienceManagementService.findLeaderBoardUsersPaginated(server, (page - 1) * pageSize, page * pageSize)).thenReturn(experiences);
        LeaderBoard leaderBoardData = testUnit.findLeaderBoardData(server, page);
        page--;
        List<LeaderBoardEntry> entries = leaderBoardData.getEntries();
        LeaderBoardEntry firstEntry = entries.get(0);
        Assert.assertEquals(LOW_EXP, firstEntry.getExperience().getExperience());
        Assert.assertEquals(level0, firstEntry.getExperience().getCurrentLevel());
        Assert.assertEquals(aUserInAServer, firstEntry.getExperience().getUser());
        Assert.assertEquals((page * pageSize) + 1, firstEntry.getRank().intValue());
        LeaderBoardEntry secondEntry = entries.get(1);
        Assert.assertEquals(MID_EXP, secondEntry.getExperience().getExperience());
        Assert.assertEquals(level1, secondEntry.getExperience().getCurrentLevel());
        Assert.assertEquals(aUserInAServer2, secondEntry.getExperience().getUser());
        Assert.assertEquals((page * pageSize) + 2, secondEntry.getRank().intValue());
        Assert.assertEquals(2, entries.size());
    }

    private void setExperienceRoleLevels() {
        when(experienceRole1.getLevel()).thenReturn(level0);
        when(experienceRole2.getLevel()).thenReturn(level1);
    }

    private void setupLevelsAndRolesAndNoDisallowed() {
        when(experienceLevelManagementService.getLevelConfig()).thenReturn(levels);
        when(experienceRoleManagementService.getExperienceRolesForServer(server)).thenReturn(experienceRoles);
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(new ArrayList<>());
    }

    private void setupDefaultConfig() {
        SystemConfigProperty minExpProperty = Mockito.mock(SystemConfigProperty.class);
        when(minExpProperty.getLongValue()).thenReturn(DEFAULT_MIN_EXP);
        when(defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.MIN_EXP_KEY)).thenReturn(minExpProperty);
        SystemConfigProperty maxExpProperty = Mockito.mock(SystemConfigProperty.class);
        when(maxExpProperty.getLongValue()).thenReturn(DEFAULT_MAX_EXP);
        when(defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.MAX_EXP_KEY)).thenReturn(maxExpProperty);
        SystemConfigProperty expMultiplierProperty = Mockito.mock(SystemConfigProperty.class);
        when(expMultiplierProperty.getDoubleValue()).thenReturn(DEFAULT_EXP_MULTIPLIER);
        when(defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY)).thenReturn(expMultiplierProperty);
    }

    protected List<AUserExperience> getUserExperiences(int count) {
        List<AUserExperience> experiences = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AUserExperience experience = Mockito.mock(AUserExperience.class);
            when(experience.getUser()).thenReturn(aUserInAServer);
            when(aUserInAServer.getServerReference()).thenReturn(server);
            when(aUserInAServer.getUserReference()).thenReturn(user);
            experiences.add(experience);
        }
        return experiences;
    }

    private void setupLevels(int count) {
        if(count >= 0) {
            when(level0.getExperienceNeeded()).thenReturn(LVL_0_EXP);
        }
        if(count >= 1) {
            when(level1.getExperienceNeeded()).thenReturn(LVL_1_EXP);
        }
        if(count >= 2) {
            when(level2.getExperienceNeeded()).thenReturn(LVL_2_EXP);
        }
        if(count >= 3) {
            when(level3.getExperienceNeeded()).thenReturn(LVL_3_EXP);
        }
    }


}
