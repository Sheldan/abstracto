package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.models.database.AEmote;
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
public class ARoleParameterHandlerImplImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private ARoleParameterHandlerImpl testUnit;

    @Mock
    private RoleParameterHandlerImpl roleParameterHandler;

    @Mock
    private RoleService roleService;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Role role;

    @Mock
    private Message message;

    @Mock
    private ARole aRole;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(ARole.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testProperRoleMention() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("test");
        when(roleParameterHandler.handle(piece, iterators, Role.class, message)).thenReturn(role);
        when(roleService.getFakeRoleFromRole(role)).thenReturn(aRole);
        ARole parsed = (ARole) testUnit.handle(piece, iterators, AEmote.class, message);
        Assert.assertEquals(aRole, parsed);
    }


}
