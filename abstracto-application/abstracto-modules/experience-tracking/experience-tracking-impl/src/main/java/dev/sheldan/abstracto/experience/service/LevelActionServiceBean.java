package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.experience.listener.LevelActionListener;
import dev.sheldan.abstracto.experience.listener.MemberActionModification;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import dev.sheldan.abstracto.experience.model.template.LevelActionDisplay;
import dev.sheldan.abstracto.experience.model.template.LevelActionsDisplay;
import dev.sheldan.abstracto.experience.service.management.LevelActionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LevelActionServiceBean implements LevelActionService {

    @Autowired
    private LevelActionManagementService levelActionManagementService;

    @Autowired(required = false)
    private List<LevelActionListener> levelActions = new ArrayList<>();

    @Autowired
    private RoleService roleService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<Void> applyLevelActionsToUser(AUserExperience user) {
        return applyLevelActionsToUser(user,  user.getLevelOrDefault());
    }

    @Override
    public CompletableFuture<Void> applyLevelActionsToUser(AUserExperience user, Integer oldLevel) {
        if(levelActions == null || levelActions.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        List<LevelAction> levelActionsOfUserInServer = levelActionManagementService.getLevelActionsOfUserInServer(user);
        if(levelActionsOfUserInServer.isEmpty()) {
            log.info("No actions available - no actions executed.");
            return CompletableFuture.completedFuture(null);
        }

        Map<Integer, List<LevelAction>> actionConfigMap = new HashMap<>();

        Map<String, LevelActionListener> actionStringListenerMap = levelActions
                .stream()
                .collect(Collectors.toMap(a -> a.getName().toLowerCase(), Function.identity()));

        levelActionsOfUserInServer.forEach(levelAction -> {
            LevelActionListener listener = actionStringListenerMap.get(levelAction.getAction());
            if(listener == null) { // if for some reason the config is still in the database, but we don't have code for it anymore
                return;
            }
            if(!listener.shouldExecute(user, oldLevel, levelAction)) {
                return;
            }
            if(actionConfigMap.containsKey(levelAction.getLevel().getLevel())) {
                actionConfigMap.get(levelAction.getLevel().getLevel()).add(levelAction);
            } else {
                List<LevelAction> levelLevelActions = new ArrayList<>();
                levelLevelActions.add(levelAction);
                actionConfigMap.put(levelAction.getLevel().getLevel(), levelLevelActions);
            }
        });


        List<Integer> levels = actionConfigMap
                .keySet()
                .stream()
                .sorted()
                .toList();

        log.debug("Performing actions for {} levels.", levels.size());

        MemberActionModification modification = MemberActionModification
                .builder()
                .build();
        levels.forEach(level -> {
            List<LevelAction> actionsOnLevel = actionConfigMap.get(level);
            actionsOnLevel.forEach(levelAction -> {
                LevelActionListener listener = actionStringListenerMap.get(levelAction.getAction().toLowerCase());
                listener.prepareAction(levelAction);
                listener.apply(user, levelAction, modification);
            });
        });

        return evaluateModifications(user, modification);
    }

    private CompletableFuture<Void> evaluateModifications(AUserExperience user, MemberActionModification modification) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Long userId = user.getUser().getUserReference().getId();
        log.info("Updating user {}, rolesToAdd: {}, rolesToRemove: {}",
                userId, modification.getRolesToAdd().size(), modification.getRolesToRemove().size());
        if(!modification.getRolesToAdd().isEmpty() || !modification.getRolesToRemove().isEmpty()) {
            CompletableFuture<Void> roleFuture = roleService.updateRolesIds(user.getUser(), new ArrayList<>(modification.getRolesToAdd()), new ArrayList<>(modification.getRolesToRemove()));
            futures.add(roleFuture);
        }
        log.info("Updating user {}, channelsToAdd: {}, channelsToRemove: {}.", userId, modification.getChannelsToAdd().size(), modification.getChannelsToRemove().size());
        Guild guild = guildService.getGuildById(user.getServer().getId());
        EnumSet<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
        modification.getChannelsToAdd().forEach(channelId -> {
            futures.add(channelService.addMemberViewToChannel(guild, channelId, userId, permissions));
        });
        modification.getChannelsToRemove().forEach(channelId -> {
            futures.add(channelService.removeChannelOverrideForMember(guild, channelId, userId));
        });
        if(!futures.isEmpty()) {
            return new CompletableFutureList<>(futures).getMainFuture();
        } else {
            log.info("Actions resulted in no actions performed.");
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public List<String> getAvailableLevelActions() {
        return levelActions
                .stream()
                .map(LevelActionListener::getName)
                .map(String::toLowerCase)
                .toList();
    }

    @Override
    public Optional<LevelActionListener> getLevelActionListenerForName(String name) {
        return levelActions
                .stream()
                .filter(levelActionListener -> levelActionListener.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public Optional<LevelAction> getLevelAction(AUserExperience userExperience, String action, Integer level) {
        return Optional.empty();
    }

    @Override
    public LevelActionsDisplay getLevelActionsToDisplay(Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        List<LevelActionDisplay> actions = levelActionManagementService.getLevelActionsOfServer(server)
                .stream().map(levelAction -> LevelActionDisplay
                        .builder()
                        .actionKey(levelAction.getAction().toLowerCase())
                        .level(levelAction.getLevel().getLevel())
                        .parameters(levelAction.getPayload())
                        .member(levelAction.getAffectedUser() != null ? MemberDisplay.fromAUserInAServer(levelAction.getAffectedUser().getUser()) : null)
                        .build())
                .toList();
        return LevelActionsDisplay
                .builder()
                .actions(actions)
                .build();
    }


}
