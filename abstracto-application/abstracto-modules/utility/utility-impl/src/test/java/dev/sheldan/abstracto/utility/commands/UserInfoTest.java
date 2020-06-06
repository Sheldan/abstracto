package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.template.commands.UserInfoModel;
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
    private BotService botService;

    @Mock
    private UserInfo self;

    @Captor
    private ArgumentCaptor<UserInfoModel> modelArgumentCaptor;

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void executeWithoutParameterAndLoadedMember() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        when(noParameters.getAuthor().hasTimeJoined()).thenReturn(true);
        CommandResult result = testUnit.execute(noParameters);
        verify(self, times(1)).sendResponse(eq(noParameters), modelArgumentCaptor.capture());
        UserInfoModel usedModel = modelArgumentCaptor.getValue();
        Assert.assertEquals(noParameters.getAuthor(), usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void executeWithoutParameterWithoutLoadedMember() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        when(noParameters.getAuthor().hasTimeJoined()).thenReturn(false);
        Member loadedAuthor = Mockito.mock(Member.class);
        when(botService.forceReloadMember(noParameters.getAuthor())).thenReturn(CompletableFuture.completedFuture(loadedAuthor));
        CommandResult result = testUnit.execute(noParameters);
        verify(self, times(1)).sendResponse(eq(noParameters), modelArgumentCaptor.capture());
        UserInfoModel usedModel = modelArgumentCaptor.getValue();
        Assert.assertEquals(loadedAuthor, usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void executeTestWithParameterLoadedMember() {
        Member member = Mockito.mock(Member.class);
        when(member.hasTimeJoined()).thenReturn(true);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(member));
        CommandResult result = testUnit.execute(parameters);
        verify(self, times(1)).sendResponse(eq(parameters), modelArgumentCaptor.capture());
        UserInfoModel usedModel = modelArgumentCaptor.getValue();
        Assert.assertEquals(member, usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void executeWithParameterWithoutLoadedMember() {
        Member member = Mockito.mock(Member.class);
        when(member.hasTimeJoined()).thenReturn(false);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(member));
        Member loadedAuthor = Mockito.mock(Member.class);
        when(botService.forceReloadMember(member)).thenReturn(CompletableFuture.completedFuture(loadedAuthor));
        CommandResult result = testUnit.execute(parameters);
        verify(self, times(1)).sendResponse(eq(parameters), modelArgumentCaptor.capture());
        UserInfoModel usedModel = modelArgumentCaptor.getValue();
        Assert.assertEquals(loadedAuthor, usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
