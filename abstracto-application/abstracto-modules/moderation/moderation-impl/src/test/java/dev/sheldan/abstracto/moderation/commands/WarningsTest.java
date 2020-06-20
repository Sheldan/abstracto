package dev.sheldan.abstracto.moderation.commands;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.converter.WarnEntryConverter;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnEntry;
import dev.sheldan.abstracto.moderation.models.template.commands.WarningsModel;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

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
    private EventWaiter eventWaiter;

    @Captor
    private ArgumentCaptor<WarningsModel> captor;

    @Test
    public void testNoParametersForWarningsCommand(){
        CommandContext noParams = CommandTestUtilities.getNoParameters();
        Warning firstWarning = Warning.builder().build();
        WarnEntry firstModelWarning = WarnEntry.builder().build();
        Warning secondWarning = Warning.builder().build();
        WarnEntry secondModelWarning = WarnEntry.builder().build();
        List<Warning> warningsToDisplay = Arrays.asList(firstWarning, secondWarning);
        List<WarnEntry> modelWarnings = Arrays.asList(firstModelWarning, secondModelWarning);
        when(warnManagementService.getAllWarningsOfServer(noParams.getUserInitiatedContext().getServer())).thenReturn(warningsToDisplay);
        when(warnEntryConverter.fromWarnings(warningsToDisplay)).thenReturn(modelWarnings);
        Paginator paginator = Mockito.mock(Paginator.class);
        when(paginatorService.createPaginatorFromTemplate(eq(Warnings.WARNINGS_RESPONSE_TEMPLATE), captor.capture(), eq(eventWaiter))).thenReturn(paginator);
        CommandResult result = testUnit.execute(noParams);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        WarningsModel warningsModel = captor.getValue();
        Assert.assertEquals(warningsToDisplay.size(), warningsModel.getWarnings().size());
        Assert.assertEquals(firstModelWarning, warningsModel.getWarnings().get(0));
        Assert.assertEquals(secondModelWarning, warningsModel.getWarnings().get(1));
    }

    @Test
    public void testExecuteWarningsForMember(){
        Member member = Mockito.mock(Member.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(member));
        AUserInAServer warnedUser = MockUtils.getUserObject(5L, parameters.getUserInitiatedContext().getServer());
        when(userInServerManagementService.loadUser(member)).thenReturn(warnedUser);
        Warning firstWarning = Warning.builder().build();
        WarnEntry firstModelWarning = WarnEntry.builder().build();
        Warning secondWarning = Warning.builder().build();
        WarnEntry secondModelWarning = WarnEntry.builder().build();
        List<Warning> warningsToDisplay = Arrays.asList(firstWarning, secondWarning);
        when(warnManagementService.getAllWarnsForUser(warnedUser)).thenReturn(warningsToDisplay);
        List<WarnEntry> modelWarnings = Arrays.asList(firstModelWarning, secondModelWarning);
        when(warnEntryConverter.fromWarnings(warningsToDisplay)).thenReturn(modelWarnings);
        Paginator paginator = Mockito.mock(Paginator.class);
        when(paginatorService.createPaginatorFromTemplate(eq(Warnings.WARNINGS_RESPONSE_TEMPLATE), captor.capture(), eq(eventWaiter))).thenReturn(paginator);
        CommandResult result = testUnit.execute(parameters);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        WarningsModel warningsModel = captor.getValue();
        Assert.assertEquals(warningsToDisplay.size(), warningsModel.getWarnings().size());
        Assert.assertEquals(firstModelWarning, warningsModel.getWarnings().get(0));
        Assert.assertEquals(secondModelWarning, warningsModel.getWarnings().get(1));
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
