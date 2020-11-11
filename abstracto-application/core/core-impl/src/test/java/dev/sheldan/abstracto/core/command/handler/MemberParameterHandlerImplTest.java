package dev.sheldan.abstracto.core.command.handler;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MemberParameterHandlerImplTest {

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

    private static final Long USER_ID = 111111111111111111L;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Member.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProperMemberMention() {
        oneMemberInIterator();
        String input = getUserMention();
        CompletableFuture<Member> parsed = (CompletableFuture) testUnit.handleAsync(input, iterators, Member.class, null);
        Assert.assertEquals(parsed.join(), member);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMemberById() {
        setupMessage();
        String input = USER_ID.toString();
        CompletableFuture<Member> parsed = (CompletableFuture) testUnit.handleAsync(input, null, Member.class, message);
        Assert.assertEquals(parsed.join(), member);
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidMemberMention() {
        String input = "test";
        testUnit.handleAsync(input, null, Member.class, null);
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
