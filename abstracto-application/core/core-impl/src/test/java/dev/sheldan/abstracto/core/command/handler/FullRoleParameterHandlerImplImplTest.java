package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FullRoleParameterHandlerImplImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private FullRoleParameterHandlerImpl testUnit;

    @Mock
    private RoleParameterHandlerImpl roleParameterHandler;

    @Mock
    private RoleService roleService;

    @Mock
    private CommandService commandService;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Role role;

    @Mock
    private Message message;

    @Mock
    private ARole aRole;

    @Mock
    private Parameter parameter;

    @Mock
    private Parameter parameter2;

    @Mock
    private Command command;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(FullRole.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testProperEmoteMention() {
        UnparsedCommandParameterPiece piece = getPiece();
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(roleParameterHandler.handle(piece, iterators, parameter2, message, command)).thenReturn(role);
        when(roleService.getFakeRoleFromRole(role)).thenReturn(aRole);
        FullRole parsed = (FullRole) testUnit.handle(piece, iterators, parameter, message, command);
        Assert.assertEquals(aRole, parsed.getRole());
        Assert.assertEquals(role, parsed.getServerRole());
    }


}
