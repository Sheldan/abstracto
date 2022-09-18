package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceParameterKey;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlaceType;
import dev.sheldan.abstracto.assignableroles.model.template.AssignablePlaceOverview;
import dev.sheldan.abstracto.assignableroles.model.template.AssignableRolePlaceConfig;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.CompletableFuture;

public interface AssignableRolePlaceService {
    void createAssignableRolePlace(String name, AChannel channel, String text, AssignableRolePlaceType type);

    CompletableFuture<Void> addRoleToAssignableRolePlace(AServer server, String placeName, Role role, FullEmote emote, String description);

    CompletableFuture<Void> removeRoleFromAssignableRolePlace(AServer server, String placeName, ARole role);

    CompletableFuture<Void> setupAssignableRolePlace(AServer server, String name);

    CompletableFuture<Void> refreshTextFromPlace(AssignableRolePlace place);

    CompletableFuture<Void> setAssignablePlaceActiveTo(AServer server, String name, Boolean newValue);

    CompletableFuture<Void> activateAssignableRolePlace(AServer server, String name);

    CompletableFuture<Void> activateAssignableRolePlace(AssignableRolePlace place);

    CompletableFuture<Void> deactivateAssignableRolePlace(AServer server, String name);

    CompletableFuture<Void> deactivateAssignableRolePlace(AssignableRolePlace place);

    void setAssignablePlaceUniqueTo(AServer server, String name, Boolean newValue);

    void uniqueAssignableRolePlace(AServer server, String name);

    void uniqueAssignableRolePlace(AssignableRolePlace place);

    void multipleAssignableRolePlace(AServer server, String name);

    void multipleAssignableRolePlace(AssignableRolePlace place);

    AssignableRolePlaceConfig getAssignableRolePlaceConfig(Guild guild, String name);

    CompletableFuture<Void> moveAssignableRolePlace(AServer server, String name, TextChannel newChannel);

    CompletableFuture<Void> deleteAssignableRolePlace(AServer server, String name);

    CompletableFuture<Void> changeTextAsync(AServer server, String name, String newText);

    CompletableFuture<Void> changeConfiguration(AServer server, String name, AssignableRolePlaceParameterKey keyToChange, String newValue);

    AssignablePlaceOverview getAssignableRolePlaceOverview(Guild guild);
}
