package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoleParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private RoleParameterHandlerImpl testUnit;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Role role;

    @Mock
    private Message message;

    @Mock
    private Guild guild;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    private static final Long ROLE_ID = 111111111111111111L;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);
        assertThat(testUnit.handles(Role.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testProperRoleMention() {
        setupMessage();
        String input = getRoleMention();

        Role parsed = (Role) testUnit.handle(getPieceWithValue(input), iterators, parameter, message, command);

        assertThat(parsed).isEqualTo(role);
    }

    @Test
    public void testRoleById() {
        setupMessage();
        String input = ROLE_ID.toString();

        Role parsed = (Role) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);

        assertThat(parsed).isEqualTo(role);
    }

    @Test
    public void testInvalidRoleMention() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        when(guild.getRolesByName(input, true)).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        }).isInstanceOf(AbstractoTemplatedException.class);

    }

    @Test
    public void testMultipleRolesFoundByName() {
        String input = "test";
        Role secondRole = Mockito.mock(Role.class);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getRolesByName(input, true)).thenReturn(Arrays.asList(role, secondRole));

        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        }).isInstanceOf(AbstractoTemplatedException.class);
    }

    @Test
    public void testFindRoleByName() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        when(guild.getRolesByName(input, true)).thenReturn(Arrays.asList(role));

        Role returnedRole =  (Role) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);

        assertThat(returnedRole).isEqualTo(role);
    }

    private String getRoleMention() {
        return String.format("<@&%d>", ROLE_ID);
    }

    private void setupMessage()  {
        when(message.getGuild()).thenReturn(guild);
        when(guild.getRoleById(String.valueOf(ROLE_ID))).thenReturn(role);
    }

}
