package dev.sheldan.abstracto.core.command.handler;

import net.dv8tion.jda.api.entities.*;
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
public class MemberParameterHandlerTest {

    @InjectMocks
    private MemberParameterHandler testUnit;

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
    public void testProperMemberMention() {
        oneMemberInIterator();
        String input = getUserMention();
        Member parsed = (Member) testUnit.handle(input, iterators, Member.class, null);
        Assert.assertEquals(parsed, member);
    }

    @Test
    public void testMemberById() {
        setupMessage();
        String input = USER_ID.toString();
        Member parsed = (Member) testUnit.handle(input, null, Member.class, message);
        Assert.assertEquals(parsed, member);
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidMemberMention() {
        String input = "test";
        testUnit.handle(input, null, Member.class, null);
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
        when(guild.getMemberById(USER_ID)).thenReturn(member);
    }

}
