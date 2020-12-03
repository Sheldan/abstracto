package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.template.commands.ShowAvatarModel;
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
public class ShowAvatarTest {

    @InjectMocks
    private ShowAvatar testUnit;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<ShowAvatarModel> argumentCaptor;

    @Test
    public void executeWithoutParameter() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(ShowAvatar.SHOW_AVATAR_RESPONSE_TEMPLATE), argumentCaptor.capture(), eq(noParameters.getChannel()));
        ShowAvatarModel usedModel = argumentCaptor.getValue();
        Assert.assertEquals(noParameters.getAuthor(), usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void executeWithParameter() {
        Member target = Mockito.mock(Member.class);
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(target));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(ShowAvatar.SHOW_AVATAR_RESPONSE_TEMPLATE), argumentCaptor.capture(), eq(noParameters.getChannel()));
        ShowAvatarModel usedModel = argumentCaptor.getValue();
        Assert.assertEquals(target, usedModel.getMemberInfo());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
