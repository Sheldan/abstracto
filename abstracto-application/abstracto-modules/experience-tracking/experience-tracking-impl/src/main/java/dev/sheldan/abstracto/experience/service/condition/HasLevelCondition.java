package dev.sheldan.abstracto.experience.service.condition;

import dev.sheldan.abstracto.core.models.ConditionContext;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;
import dev.sheldan.abstracto.core.models.ConditionContextVariable;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.SystemCondition;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

@Component
@Slf4j
public class HasLevelCondition implements SystemCondition {

    public static final String USER_ID_VARIABLE = "userId";
    public static final String LEVEL_VARIABLE = "level";
    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public boolean checkCondition(ConditionContextInstance conditionContext) {
        HashMap<String, Object> parameters = conditionContext.getParameters();
        Long userId = (Long) parameters.get(USER_ID_VARIABLE);
        Integer level = (Integer) parameters.get(LEVEL_VARIABLE);
        log.info("Evaluating has level condition.");
        Optional<AUserInAServer> userInServerOptional = userInServerManagementService.loadUserOptional(userId);
        if(userInServerOptional.isPresent()) {
            AUserInAServer userInServer = userInServerOptional.get();
            log.info("Evaluating has level condition for user {} in server {} with level {}.",
                    userInServer.getUserReference().getId(), userInServer.getServerReference().getId(), level);
            AUserExperience user = userExperienceManagementService.findUserInServer(userInServer);
            return user.getCurrentLevel() != null && user.getCurrentLevel().getLevel() >= level;
        }
        log.info("No user experience object was found. Evaluating to false.");

        return false;
    }

    @Override
    public String getConditionName() {
        return "HAS_LEVEL";
    }

    @Override
    public ConditionContext getExpectedContext() {
        ConditionContextVariable userIdVariable = ConditionContextVariable.builder().name(USER_ID_VARIABLE).type(Long.class).build();
        ConditionContextVariable levelVariable = ConditionContextVariable.builder().name(LEVEL_VARIABLE).type(Integer.class).build();
        return ConditionContext.builder().expectedVariables(Arrays.asList(userIdVariable, levelVariable)).build();
    }
}
