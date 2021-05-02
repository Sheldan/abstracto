package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask;
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
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MemberParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private MemberParameterHandlerImpl testUnit;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Member member;

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

    private static final Long USER_ID = 111111111111111111L;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Member.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProperMemberMention() {
        oneMemberInIterator();
        String input = getUserMention();
        CompletableFuture<Member> parsed = (CompletableFuture) testUnit.handleAsync(getPieceWithValue(input), iterators, parameter, null, command);
        Assert.assertEquals(member, parsed.join());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMemberById() {
        setupMessage();
        String input = USER_ID.toString();
        CompletableFuture<Member> parsed = (CompletableFuture) testUnit.handleAsync(getPieceWithValue(input), null, parameter, message, command);
        Assert.assertEquals(member, parsed.join());
    }

    @Test
    public void testNotExistingMember() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        GatewayTask task = new GatewayTask(CompletableFuture.completedFuture(new ArrayList()), () -> {});
        when(guild.retrieveMembersByPrefix(input, 1)).thenReturn(task);
        CompletableFuture<Object> future = testUnit.handleAsync(getPieceWithValue(input), null, parameter, message, command);
        Assert.assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testMultipleFoundMemberByName() {
        String input = "test";
        Member secondMember = Mockito.mock(Member.class);
        when(message.getGuild()).thenReturn(guild);
        GatewayTask task = new GatewayTask(CompletableFuture.completedFuture(Arrays.asList(member, secondMember)), () -> {});
        when(guild.retrieveMembersByPrefix(input, 1)).thenReturn(task);
        CompletableFuture<Object> future = testUnit.handleAsync(getPieceWithValue(input), null, parameter, message, command);
        Assert.assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testFindMemberByName() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        GatewayTask task = new GatewayTask(CompletableFuture.completedFuture(Arrays.asList(member)), () -> {});
        when(guild.retrieveMembersByPrefix(input, 1)).thenReturn(task);
        CompletableFuture<Object> future = testUnit.handleAsync(getPieceWithValue(input), null, parameter, message, command);
        Member returnedMember = (Member) future.join();
        Assert.assertFalse(future.isCompletedExceptionally());
        Assert.assertEquals(member, returnedMember);
    }

    private String getUserMention() {
        return String.format("<@%d>", USER_ID);
    }

    private void oneMemberInIterator() {
        List<Member> members = Arrays.asList(member);
        when(iterators.getMemberIterator()).thenReturn(members.iterator());
    }

    private void setupMessage()  {
        when(message.getGuild()).thenReturn(guild);
        RestAction<Member> restAction = Mockito.mock(RestAction.class);
        when(restAction.submit()).thenReturn(CompletableFuture.completedFuture(member));
        when(guild.retrieveMemberById(USER_ID)).thenReturn(restAction);
    }

}
