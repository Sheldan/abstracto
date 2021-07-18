package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.condition.AssignableRoleConditionEvaluator;
import dev.sheldan.abstracto.assignableroles.exception.AssignableRoleConditionAlreadyExistsException;
import dev.sheldan.abstracto.assignableroles.exception.AssignableRoleConditionValueNotUsableException;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionResult;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRoleCondition;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.exception.AssignableRoleConditionDoesNotExistException;
import dev.sheldan.abstracto.assignableroles.model.template.condition.AssignableRoleConditionDisplay;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRoleConditionManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlaceManagementService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AssignableRoleConditionServiceBean implements AssignableRoleConditionService {

    @Autowired
    private List<AssignableRoleConditionEvaluator> assignableRoleConditionEvaluators;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private AssignableRolePlaceManagementService assignableRolePlaceManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private AssignableRoleService assignableRoleService;

    @Autowired
    private AssignableRoleConditionManagementService assignableRoleConditionManagementService;

    @Override
    public AssignableRoleConditionResult evaluateConditions(List<AssignableRoleCondition> conditions, Member member, Role role) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        return evaluateConditions(conditions, aUserInAServer, role);
    }

    @Override
    public AssignableRoleConditionResult evaluateConditions(List<AssignableRoleCondition> conditions, AUserInAServer aUserInAServer, Role role) {
        log.debug("Evaluating {} conditions for role {}.", conditions.size(), role.getId());
        for (AssignableRoleCondition condition : conditions) {
            if(assignableRoleConditionEvaluators != null) {
                Optional<AssignableRoleConditionEvaluator> evaluatorOptional = findEvaluatorForCondition(condition.getType());
                if(evaluatorOptional.isPresent()) {
                    AssignableRoleConditionEvaluator evaluator = evaluatorOptional.get();
                    log.debug("Evaluating condition {} with evaluator {}.", condition.getType(), evaluator.getClass());
                    if(!evaluator.fulfillsCondition(condition, aUserInAServer)) {
                        log.info("Condition {} failed for role {} in server {}.", condition.getType(), role.getId(), aUserInAServer.getServerReference().getId());
                        return AssignableRoleConditionResult.fromFail(condition.getType(), evaluator.createNotificationModel(condition, role));
                    }
                }
            }
        }
        return AssignableRoleConditionResult.fromSuccess();
    }

    private Optional<AssignableRoleConditionEvaluator> findEvaluatorForCondition(AssignableRoleConditionType type) {
        return assignableRoleConditionEvaluators
                .stream()
                .filter(assignableRoleConditionEvaluator -> assignableRoleConditionEvaluator.handlesCondition(type))
                .findFirst();
    }

    @Override
    public AssignableRoleCondition createAssignableRoleCondition(String placeName, Role role, AssignableRoleConditionType type, String value) {
        AServer server = serverManagementService.loadServer(role.getGuild());
        AssignableRolePlace place = assignableRolePlaceManagementService.findByServerAndKey(server, placeName);
        AssignableRole assignableRole = assignableRoleService.getAssignableRoleInPlace(place, role);
        Optional<AssignableRoleConditionEvaluator> evaluatorOptional = findEvaluatorForCondition(type);
        if(!evaluatorOptional.isPresent()) {
            throw new AssignableRoleConditionDoesNotExistException();
        }
        evaluatorOptional.ifPresent(evaluator -> {
            boolean valueUsable = evaluator.usableValue(value);
            if(!valueUsable) {
                throw new AssignableRoleConditionValueNotUsableException();
            }
        });
        if(assignableRoleConditionManagementService.findAssignableRoleCondition(assignableRole, type).isPresent()) {
            throw new AssignableRoleConditionAlreadyExistsException();
        }
        log.info("Creating new condition for  role {} in place {} in server {}.", place.getId(), role.getId(), role.getGuild().getIdLong());
        return assignableRoleConditionManagementService.createAssignableRoleCondition(assignableRole, type, value);
    }

    @Override
    public void deleteAssignableRoleCondition(String placeName, Role role, AssignableRoleConditionType type) {
        AServer server = serverManagementService.loadServer(role.getGuild());
        AssignableRolePlace place = assignableRolePlaceManagementService.findByServerAndKey(server, placeName);
        AssignableRole assignableRole = assignableRoleService.getAssignableRoleInPlace(place, role);
        Optional<AssignableRoleCondition> existingCondition = assignableRoleConditionManagementService.findAssignableRoleCondition(assignableRole, type);
        if(!existingCondition.isPresent()) {
            throw new AssignableRoleConditionDoesNotExistException();
        }
        log.info("Deleting assignable role condition on place {} for role {} in server {}.", place.getId(), role.getId(), role.getGuild().getIdLong());
        existingCondition.ifPresent(condition -> assignableRoleConditionManagementService.deleteAssignableRoleCondition(condition));
    }

    @Override
    public List<AssignableRoleConditionDisplay> getConditionDisplays(List<AssignableRoleCondition> conditions) {
        return conditions.stream().map(condition -> {
            Optional<AssignableRoleConditionEvaluator> evaluatorOptional = findEvaluatorForCondition(condition.getType());
            if(evaluatorOptional.isPresent()) {
                AssignableRoleConditionEvaluator evaluator = evaluatorOptional.get();
                return evaluator.getConditionDisplay(condition);
            }
            return null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }
}
