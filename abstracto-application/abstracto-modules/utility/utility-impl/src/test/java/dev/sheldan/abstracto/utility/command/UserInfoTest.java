package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.model.UserInfoModel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserInfoTest {

    @InjectMocks
    private UserInfo testUnit;

    @Mock
    private ChannelService channelService;

    @Mock
    private MemberService memberService;

    @Mock
    private UserInfo self;

    @Captor
    private ArgumentCaptor<UserInfoModel> modelArgumentCaptor;

    @Test
    public void executeWithoutParameterAndLoadedMember() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        when(noParameters.getAuthor().getGuild()).thenReturn(noParameters.getGuild());
        when(noParameters.getAuthor().hasTimeJoined()).thenReturn(true);
        when(self.sendResponse(eq(noParameters),any(UserInfoModel.class))).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        verify(self, times(1)).sendResponse(eq(noParameters), modelArgumentCaptor.capture());
        UserInfoModel usedModel = modelArgumentCaptor.getValue();
        Assert.assertEquals(noParameters.getAuthor(), usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void executeWithoutParameterWithoutLoadedMember() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        when(noParameters.getAuthor().hasTimeJoined()).thenReturn(false);
        Member loadedAuthor = Mockito.mock(Member.class);
        when(noParameters.getAuthor().getGuild()).thenReturn(noParameters.getGuild());
        when(memberService.forceReloadMember(noParameters.getAuthor())).thenReturn(CompletableFuture.completedFuture(loadedAuthor));
        when(self.sendResponse(eq(noParameters), modelArgumentCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        UserInfoModel usedModel = modelArgumentCaptor.getValue();
        Assert.assertEquals(loadedAuthor, usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void executeTestWithParameterLoadedMember() {
        Member member = Mockito.mock(Member.class);
        when(member.hasTimeJoined()).thenReturn(true);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(member));
        when(member.getGuild()).thenReturn(parameters.getGuild());
        when(self.sendResponse(eq(parameters), modelArgumentCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        UserInfoModel usedModel = modelArgumentCaptor.getValue();
        Assert.assertEquals(member, usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void executeWithParameterWithoutLoadedMember() {
        Member member = Mockito.mock(Member.class);
        when(member.hasTimeJoined()).thenReturn(false);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(member));
        when(member.getGuild()).thenReturn(parameters.getGuild());
        Member loadedAuthor = Mockito.mock(Member.class);
        when(memberService.forceReloadMember(member)).thenReturn(CompletableFuture.completedFuture(loadedAuthor));
        when(self.sendResponse(eq(parameters), modelArgumentCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        UserInfoModel usedModel = modelArgumentCaptor.getValue();
        Assert.assertEquals(loadedAuthor, usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
