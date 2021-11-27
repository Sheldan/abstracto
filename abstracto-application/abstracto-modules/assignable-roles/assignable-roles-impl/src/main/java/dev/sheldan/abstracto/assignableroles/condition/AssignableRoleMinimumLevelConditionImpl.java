package dev.sheldan.abstracto.assignableroles.condition;

import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleMinLevelModel;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRolePlaceConditionModel;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleMinLevelResult;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRoleCondition;
import dev.sheldan.abstracto.assignableroles.model.template.condition.AssignableRoleConditionDisplay;
import dev.sheldan.abstracto.assignableroles.model.template.condition.AssignableRoleMinLevelDisplay;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.ConditionService;
import dev.sheldan.abstracto.core.service.SystemCondition;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AssignableRoleMinimumLevelConditionImpl implements AssignableRoleConditionEvaluator {

    private final String conditionName = "HAS_LEVEL";
    private final String userIdParameter = "userId";
    private final String levelParameter = "level";
    private final String serverParameter = "serverId";

    @Autowired
    private ConditionService conditionService;

    @Override
    public boolean fulfillsCondition(AssignableRoleCondition conditionDefinition, AUserInAServer aUserInAServer) {
        Integer level = parseLevel(conditionDefinition);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(userIdParameter, aUserInAServer.getUserInServerId());
        parameters.put(levelParameter, level);
        parameters.put(serverParameter, aUserInAServer.getServerReference().getId());

        ConditionContextInstance contextInstance = ConditionContextInstance
                .builder()
                .conditionName(conditionName)
                .parameters(parameters)
                .build();
        SystemCondition.Result result = conditionService.checkConditions(contextInstance);
        return SystemCondition.Result.consideredSuccessful(result);
    }

    @Override
    public boolean usableValue(String value) {
        try {
            parseLevelValue(value);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private Integer parseLevel(AssignableRoleCondition conditionDefinition) {
        return parseLevelValue(conditionDefinition.getConditionValue());
    }

    private int parseLevelValue(String value) {
        return Integer.parseInt(value);
    }

    @Override
    public AssignableRolePlaceConditionModel createNotificationModel(AssignableRoleCondition conditionDefinition, Role role) {
        Integer level = parseLevel(conditionDefinition);
        AssignableRoleMinLevelModel model = AssignableRoleMinLevelModel
                .builder()
                .minLevel(level)
                .roleDisplay(RoleDisplay.fromRole(role))
                .build();
        return AssignableRoleMinLevelResult
                .builder()
                .model(model)
                .build();
    }

    @Override
    public AssignableRoleConditionDisplay getConditionDisplay(AssignableRoleCondition conditionDefinition) {
        return new AssignableRoleMinLevelDisplay(Integer.parseInt(conditionDefinition.getConditionValue()));
    }

    @Override
    public boolean handlesCondition(AssignableRoleConditionType type) {
        return AssignableRoleConditionType.MIN_LEVEL.equals(type);
    }
}
