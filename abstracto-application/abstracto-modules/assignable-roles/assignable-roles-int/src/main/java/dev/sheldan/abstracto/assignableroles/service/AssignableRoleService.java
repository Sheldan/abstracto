package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface AssignableRoleService {
    CompletableFuture<Void> assignAssignableRoleToUser(Long assignableRoleId, Member toAdd);
    CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, Member member);
}
