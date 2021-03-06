package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UnSetExpRoleTest {

    @InjectMocks
    private UnSetExpRole testUnit;

    @Mock
    private ExperienceRoleService experienceRoleService;

    @Mock
    private RoleManagementService roleManagementService;

    @Mock
    private ExperienceRoleManagementService experienceRoleManagementService;

    private static final Long CHANNEL_ID = 4L;

    @Test
    public void setUnSetExpRole() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        ARole changedRole = Mockito.mock(ARole.class);
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(changedRole));
        when(context.getChannel().getIdLong()).thenReturn(CHANNEL_ID);
        ARole actualRole = Mockito.mock(ARole.class);
        AServer server = Mockito.mock(AServer.class);
        when(actualRole.getServer()).thenReturn(server);
        when(roleManagementService.findRole(changedRole.getId())).thenReturn(actualRole);
        when(experienceRoleManagementService.getRoleInServerOptional(actualRole)).thenReturn(Optional.of(Mockito.mock(AExperienceRole.class)));
        when(experienceRoleService.unsetRoles(actualRole, CHANNEL_ID)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
