package dev.sheldan.abstracto.moderation.commands.invite;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.models.database.FilteredInviteLink;
import dev.sheldan.abstracto.moderation.models.template.commands.TrackedInviteLinksModel;
import dev.sheldan.abstracto.moderation.service.InviteLinkFilterService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.commands.invite.ShowTrackedInviteLinks.TRACKED_INVITE_LINKS_EMBED_TEMPLATE_KEY;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShowTrackedInviteLinksTest {

    @InjectMocks
    private ShowTrackedInviteLinks testUnit;

    @Mock
    private InviteLinkFilterService inviteLinkFilterService;

    @Mock
    private ChannelService channelService;

    @Mock
    private TemplateService templateService;

    @Test
    public void testExecuteCommandNoParameter() {
        CommandContext parameters = CommandTestUtilities.getNoParameters();
        FilteredInviteLink filteredInviteLink = Mockito.mock(FilteredInviteLink.class);
        when(inviteLinkFilterService.getTopFilteredInviteLinks(parameters.getGuild().getIdLong())).thenReturn(Arrays.asList(filteredInviteLink));
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(TRACKED_INVITE_LINKS_EMBED_TEMPLATE_KEY), any(TrackedInviteLinksModel.class))).thenReturn(messageToSend);
        when(channelService.sendMessageToSendToChannel(messageToSend, parameters.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> resultFuture = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(resultFuture);
    }

    @Test
    public void testExecuteCommandAmountParameter() {
        Integer amount = 4;
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(amount));
        FilteredInviteLink filteredInviteLink = Mockito.mock(FilteredInviteLink.class);
        when(inviteLinkFilterService.getTopFilteredInviteLinks(parameters.getGuild().getIdLong(), amount)).thenReturn(Arrays.asList(filteredInviteLink));
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(TRACKED_INVITE_LINKS_EMBED_TEMPLATE_KEY), any(TrackedInviteLinksModel.class))).thenReturn(messageToSend);
        when(channelService.sendMessageToSendToChannel(messageToSend, parameters.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> resultFuture = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(resultFuture);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
