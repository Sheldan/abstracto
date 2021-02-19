package dev.sheldan.abstracto.core.command.handler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

    private static final Long ROLE_ID = 111111111111111111L;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Role.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testProperRoleMention() {
        oneRoleIterator();
        String input = getRoleMention();
        Role parsed = (Role) testUnit.handle(getPieceWithValue(input), iterators, Role.class, null);
        Assert.assertEquals(parsed, role);
    }

    @Test
    public void testRoleById() {
        setupMessage();
        String input = ROLE_ID.toString();
        Role parsed = (Role) testUnit.handle(getPieceWithValue(input), null, Role.class, message);
        Assert.assertEquals(parsed, role);
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidRoleMention() {
        String input = "test";
        testUnit.handle(getPieceWithValue(input), null, Role.class, null);
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
