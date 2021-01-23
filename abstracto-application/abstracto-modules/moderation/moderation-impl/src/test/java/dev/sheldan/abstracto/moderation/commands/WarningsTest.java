package dev.sheldan.abstracto.moderation.commands;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.converter.WarnEntryConverter;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnEntry;
import dev.sheldan.abstracto.moderation.models.template.commands.WarningsModel;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WarningsTest {

    @InjectMocks
    private Warnings testUnit;

    @Mock
    private WarnManagementService warnManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private WarnEntryConverter warnEntryConverter;

    @Mock
    private PaginatorService paginatorService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private EventWaiter eventWaiter;

    @Captor
    private ArgumentCaptor<WarningsModel> captor;

    @Mock
    private Warnings self;

    @Test
    public void testNoParametersForWarningsCommand(){
        CommandContext noParams = CommandTestUtilities.getNoParameters();
        Warning firstWarning = Mockito.mock(Warning.class);
        WarnEntry firstModelWarning = Mockito.mock(WarnEntry.class);
        Warning secondWarning = Mockito.mock(Warning.class);
        WarnEntry secondModelWarning = Mockito.mock(WarnEntry.class);
        List<Warning> warningsToDisplay = Arrays.asList(firstWarning, secondWarning);
        List<WarnEntry> modelWarnings = Arrays.asList(firstModelWarning, secondModelWarning);
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(noParams.getGuild())).thenReturn(server);
        when(warnManagementService.getAllWarningsOfServer(server)).thenReturn(warningsToDisplay);
        when(warnEntryConverter.fromWarnings(warningsToDisplay)).thenReturn(CompletableFuture.completedFuture(modelWarnings));

        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParams);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        verify(self, times(1)).renderWarnings(noParams, modelWarnings);

    }

    @Test
    public void testWarningsRendering() {
        CommandContext noParams = CommandTestUtilities.getNoParameters();
        WarnEntry firstModelWarning = Mockito.mock(WarnEntry.class);
        WarnEntry secondModelWarning = Mockito.mock(WarnEntry.class);
        Paginator paginator = Mockito.mock(Paginator.class);
        when(paginatorService.createPaginatorFromTemplate(eq(Warnings.WARNINGS_RESPONSE_TEMPLATE), captor.capture(), eq(eventWaiter))).thenReturn(paginator);
        List<WarnEntry> modelWarnings = Arrays.asList(firstModelWarning, secondModelWarning);
        testUnit.renderWarnings(noParams, modelWarnings);
        WarningsModel warningsModel = captor.getValue();
        Assert.assertEquals(firstModelWarning, warningsModel.getWarnings().get(0));
        Assert.assertEquals(secondModelWarning, warningsModel.getWarnings().get(1));
    }

    @Test
    public void testExecuteWarningsForMember(){
        Member member = Mockito.mock(Member.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(member));
        AUserInAServer warnedUser = Mockito.mock(AUserInAServer.class);
        Warning firstWarning = Mockito.mock(Warning.class);
        WarnEntry firstModelWarning = Mockito.mock(WarnEntry.class);
        Warning secondWarning = Mockito.mock(Warning.class);
        WarnEntry secondModelWarning = Mockito.mock(WarnEntry.class);
        List<Warning> warningsToDisplay = Arrays.asList(firstWarning, secondWarning);
        List<WarnEntry> modelWarnings = Arrays.asList(firstModelWarning, secondModelWarning);
        when(userInServerManagementService.loadUser(member)).thenReturn(warnedUser);
        when(warnManagementService.getAllWarnsForUser(warnedUser)).thenReturn(warningsToDisplay);
        when(warnEntryConverter.fromWarnings(warningsToDisplay)).thenReturn(CompletableFuture.completedFuture(modelWarnings));

        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        verify(self, times(1)).renderWarnings(parameters, modelWarnings);

    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
