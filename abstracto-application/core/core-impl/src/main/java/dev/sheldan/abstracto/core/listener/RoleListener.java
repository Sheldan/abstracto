package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

@Component
@Slf4j
public class RoleListener extends ListenerAdapter {

    @Autowired
    private RoleManagementService service;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    @Transactional
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        AServer server = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        service.createRole(event.getRole().getIdLong(), server);
    }

    @Override
    @Transactional
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        AServer server = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        roleService.markDeleted(event.getRole(), server);
    }
}
