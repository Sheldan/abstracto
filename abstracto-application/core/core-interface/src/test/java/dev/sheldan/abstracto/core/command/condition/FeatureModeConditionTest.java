package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.exception.IncorrectFeatureModeException;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeatureModeConditionTest {

    @InjectMocks
    private FeatureModeCondition testUnit;

    @Mock
    private FeatureModeService modeService;

    @Mock
    private Command command;

    @Mock
    private CommandContext commandContext;

    @Mock
    private FeatureMode featureMode;

    @Mock
    private FeatureEnum featureEnum;

    @Mock
    private AServer server;

    @Mock
    private UserInitiatedServerContext userInitiatedServerContext;

    @Test
    public void testNoLimitations() {
        when(command.getFeatureModeLimitations()).thenReturn(new ArrayList<>());
        CommandTestUtilities.checkSuccessfulCondition(testUnit.shouldExecute(commandContext, command));
    }

    @Test
    public void testMetLimitations() {
        when(commandContext.getUserInitiatedContext()).thenReturn(userInitiatedServerContext);
        when(userInitiatedServerContext.getServer()).thenReturn(server);
        when(command.getFeature()).thenReturn(featureEnum);
        when(modeService.featureModeActive(featureEnum, server, featureMode)).thenReturn(true);
        when(command.getFeatureModeLimitations()).thenReturn(Arrays.asList(featureMode));
        CommandTestUtilities.checkSuccessfulCondition(testUnit.shouldExecute(commandContext, command));
    }

    @Test(expected = IncorrectFeatureModeException.class)
    public void testLimitedCommand() {
        when(commandContext.getUserInitiatedContext()).thenReturn(userInitiatedServerContext);
        when(userInitiatedServerContext.getServer()).thenReturn(server);
        when(command.getFeature()).thenReturn(featureEnum);
        when(modeService.featureModeActive(featureEnum, server, featureMode)).thenReturn(false);
        when(command.getFeatureModeLimitations()).thenReturn(Arrays.asList(featureMode));
        testUnit.shouldExecute(commandContext, command);
    }
}