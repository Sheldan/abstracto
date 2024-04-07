package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);

        assertThat(testUnit.handles(FullRole.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testProperEmoteMention() {
        UnparsedCommandParameterPiece piece = getPiece();
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(roleParameterHandler.handle(piece, iterators, parameter2, message, command)).thenReturn(role);
        when(roleService.getFakeRoleFromRole(role)).thenReturn(aRole);

        FullRole parsed = (FullRole) testUnit.handle(piece, iterators, parameter, message, command);

        assertThat(parsed.getRole()).isEqualTo(aRole);
        assertThat(parsed.getServerRole()).isEqualTo(role);
    }


}
