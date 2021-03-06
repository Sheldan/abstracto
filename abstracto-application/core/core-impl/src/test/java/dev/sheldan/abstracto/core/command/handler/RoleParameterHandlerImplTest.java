package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Assert.assertTrue(testUnit.handles(Role.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testProperRoleMention() {
        oneRoleIterator();
        String input = getRoleMention();
        Role parsed = (Role) testUnit.handle(getPieceWithValue(input), iterators, parameter, null, command);
        Assert.assertEquals(role, parsed);
    }

    @Test
    public void testRoleById() {
        setupMessage();
        String input = ROLE_ID.toString();
        Role parsed = (Role) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        Assert.assertEquals(role, parsed);
    }

    @Test(expected = AbstractoTemplatedException.class)
    public void testInvalidRoleMention() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        when(guild.getRolesByName(input, true)).thenReturn(new ArrayList<>());
        testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
    }

    @Test(expected = AbstractoTemplatedException.class)
    public void testMultipleRolesFoundByName() {
        String input = "test";
        Role secondRole = Mockito.mock(Role.class);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getRolesByName(input, true)).thenReturn(Arrays.asList(role, secondRole));
        testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
    }

    @Test
    public void testFindRoleByName() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        when(guild.getRolesByName(input, true)).thenReturn(Arrays.asList(role));
        Role returnedRole =  (Role) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        Assert.assertEquals(role, returnedRole);
    }

    private String getRoleMention() {
        return String.format("<@&%d>", ROLE_ID);
    }

    private void oneRoleIterator() {
        List<Role> members = Arrays.asList(role);
        when(iterators.getRoleIterator()).thenReturn(members.iterator());
    }

    private void setupMessage()  {
        when(message.getGuild()).thenReturn(guild);
        when(guild.getRoleById(ROLE_ID)).thenReturn(role);
    }

}
