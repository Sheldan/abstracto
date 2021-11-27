package dev.sheldan.abstracto.experience.service.condition;

import dev.sheldan.abstracto.core.models.ConditionContext;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;
import dev.sheldan.abstracto.core.models.ConditionContextVariable;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.SystemCondition;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * This condition evaluates whether or not a given {@link AUserExperience userExperience}, defined by the ID of {@link AUserInAServer userInAServer}
 * has at least the given level.
 */
@Component
@Slf4j
public class HasLevelCondition implements SystemCondition {

    public static final String USER_IN_SERVER_ID_VARIABLE_KEY = "userId";
    public static final String LEVEL_VARIABLE = "level";
    public static final String SERVER_VARIABLE = "serverId";
    public static final String HAS_LEVEL_CONDITION_KEY = "HAS_LEVEL";

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    public Result checkCondition(ConditionContextInstance conditionContext) {
        Map<String, Object> parameters = conditionContext.getParameters();
        Long userInServerId = (Long) parameters.get(USER_IN_SERVER_ID_VARIABLE_KEY);
        Long serverId = (Long) parameters.get(SERVER_VARIABLE);
        Integer level = (Integer) parameters.get(LEVEL_VARIABLE);
        if(!featureFlagService.getFeatureFlagValue(ExperienceFeatureDefinition.EXPERIENCE, serverId)) {
            return Result.IGNORED;
        }
        Optional<AUserInAServer> userInServerOptional = userInServerManagementService.loadUserOptional(userInServerId);
        if(userInServerOptional.isPresent()) {
            AUserInAServer userInServer = userInServerOptional.get();
            log.info("Evaluating has level condition for user {} in server {} with level {}.",
                    userInServer.getUserReference().getId(), userInServer.getServerReference().getId(), level);
            AUserExperience user = userExperienceManagementService.findUserInServer(userInServer);
            boolean conditionResult = user.getCurrentLevel() != null && user.getCurrentLevel().getLevel() >= level;
            log.info("Condition evaluated to {}", conditionResult);
            return Result.fromBoolean(conditionResult);
        }
        log.info("No user in server object was found. Evaluating has level to false.");

        return Result.FAILED;
    }

    @Override
    public String getConditionName() {
        return HAS_LEVEL_CONDITION_KEY;
    }

    @Override
    public ConditionContext getExpectedContext() {
        ConditionContextVariable userIdVariable = ConditionContextVariable
                .builder()
                .name(USER_IN_SERVER_ID_VARIABLE_KEY)
                .type(Long.class)
                .build();
        ConditionContextVariable levelVariable = ConditionContextVariable
                .builder()
                .name(LEVEL_VARIABLE)
                .type(Integer.class)
                .build();
        ConditionContextVariable serverVariable = ConditionContextVariable
                .builder()
                .name(SERVER_VARIABLE)
                .type(Long.class)
                .build();
        return ConditionContext
                .builder()
                .requiredVariables(Arrays.asList(userIdVariable, levelVariable, serverVariable))
                .build();
    }
}
