package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceParameterKey;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.CompletableFuture;

public interface AssignableRolePlaceService {
    void createAssignableRolePlace(AServer server, String name, AChannel channel, String text);
    boolean hasAssignableRolePlaceEmote(AServer server, String placeName, AEmote emote);
    boolean hasAssignableRolePlaceEmote(AssignableRolePlace place, AEmote emote);
    boolean isPositionUsed(AServer server, String placeName, Integer position);
    CompletableFuture<Void> setEmoteToPosition(AServer server, String placeName, FullEmote emote, Integer position);
    CompletableFuture<Void> addRoleToAssignableRolePlace(AServer server, String placeName, ARole role, FullEmote emote, String description);
    CompletableFuture<Void> removeRoleFromAssignableRolePlace(AServer server, String placeName, FullEmote emote);
    CompletableFuture<Void> setupAssignableRolePlace(AServer server, String name);
    CompletableFuture<Void> refreshAssignablePlacePosts(AServer server, String name);
    CompletableFuture<Void> refreshAssignablePlacePosts(AssignableRolePlace place);
    CompletableFuture<Void> refreshTextFromPlace(AssignableRolePlace place);
    void setAssignablePlaceActiveTo(AServer server, String name, Boolean newValue);
    void activateAssignableRolePlace(AServer server, String name);
    void activateAssignableRolePlace(AssignableRolePlace place);
    void deactivateAssignableRolePlace(AServer server, String name);
    void deactivateAssignableRolePlace(AssignableRolePlace place);

    // inline attribute
    CompletableFuture<Void> setAssignablePlaceInlineTo(AServer server, String name, Boolean newValue);
    CompletableFuture<Void> inlineAssignableRolePlace(AServer server, String name);
    CompletableFuture<Void> inlineAssignableRolePlace(AssignableRolePlace place);
    CompletableFuture<Void> spreadAssignableRolePlace(AServer server, String name);
    CompletableFuture<Void> spreadAssignableRolePlace(AssignableRolePlace place);

    // unique attribute
    void setAssignablePlaceUniqueTo(AServer server, String name, Boolean newValue);
    void uniqueAssignableRolePlace(AServer server, String name);
    void uniqueAssignableRolePlace(AssignableRolePlace place);
    void multipleAssignableRolePlace(AServer server, String name);
    void multipleAssignableRolePlace(AssignableRolePlace place);

    // auto remove attribute
    void setAssignablePlaceAutoRemoveTo(AServer server, String name, Boolean newValue);
    void autoRemoveAssignableRolePlace(AServer server, String name);
    void autoRemoveAssignableRolePlace(AssignableRolePlace place);
    void keepReactionsAssignableRolePlace(AServer server, String name);
    void keepReactionsAssignableRolePlace(AssignableRolePlace place);

    void swapPositions(AServer server, String name, FullEmote firstEmote, FullEmote secondEmote);
    CompletableFuture<Void> testAssignableRolePlace(AServer server, String name, MessageChannel channel);
    void showAssignablePlaceConfig(AServer server, String name, MessageChannel channel);
    void moveAssignableRolePlace(AServer server, String name, TextChannel newChannel);
    void changeAssignablePlaceDescription(AServer server, String name, String newDescription);
    CompletableFuture<Void> deleteAssignableRolePlace(AServer server, String name);
    CompletableFuture<Void> changeText(AServer server, String name, String newText);
    CompletableFuture<Void> removeExistingReactionsAndRoles(AssignableRolePlace place, AssignedRoleUser user);
    CompletableFuture<Void> changeConfiguration(AServer server, String name, AssignableRolePlaceParameterKey keyToChange, Object newValue);

    CompletableFuture<Void> showAllAssignableRolePlaces(AServer server, MessageChannel channel);
}
