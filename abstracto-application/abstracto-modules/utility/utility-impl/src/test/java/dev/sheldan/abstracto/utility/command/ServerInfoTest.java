package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.model.ServerInfoModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerInfoTest {

    @InjectMocks
    private ServerInfo testUnit;

    @Mock
    private ChannelService channelService;

    @Test
    public void executeCommand() {
        CommandContext context = CommandTestUtilities.getNoParameters();
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        verify(channelService, times(1)).sendEmbedTemplateInTextChannelList(eq("serverinfo_response"), any(ServerInfoModel.class), eq(context.getChannel()));
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
