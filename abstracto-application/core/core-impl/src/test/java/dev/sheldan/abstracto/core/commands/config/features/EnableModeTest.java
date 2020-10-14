package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.exception.FeatureNotFoundException;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EnableModeTest {

    @InjectMocks
    private EnableMode testUnit;

    @Mock
    private FeatureConfigService featureConfigService;

    @Mock
    private FeatureModeService featureModeService;

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void testExecuteDisable() {
        String featureName = "text";
        String modeName = "mode";
        FeatureEnum featureEnum = Mockito.mock(FeatureEnum.class);
        when(featureConfigService.getFeatureEnum(featureName)).thenReturn(featureEnum);
        FeatureMode featureMode = Mockito.mock(FeatureMode.class);
        when(featureModeService.getFeatureModeForKey(modeName)).thenReturn(featureMode);
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(featureName, modeName));
        CommandResult commandResultCompletableFuture = testUnit.execute(context);
        CommandTestUtilities.checkSuccessfulCompletion(commandResultCompletableFuture);
        verify(featureModeService, times(1)).enableFeatureModeForFeature(featureEnum, context.getUserInitiatedContext().getServer(), featureMode);
    }

    @Test(expected = FeatureNotFoundException.class)
    public void testExecuteDisableNotExistingFeature() {
        String featureName = "text";
        String modeName = "mode";
        when(featureConfigService.getFeatureEnum(featureName)).thenThrow(new FeatureNotFoundException(featureName, new ArrayList<>()));
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(featureName, modeName));
        testUnit.execute(context);
    }

    @Test(expected = FeatureModeNotFoundException.class)
    public void testExecuteDisableNotExistingFeatureMode() {
        String featureName = "text";
        String modeName = "mode";
        FeatureEnum featureEnum = Mockito.mock(FeatureEnum.class);
        when(featureConfigService.getFeatureEnum(featureName)).thenReturn(featureEnum);
        when(featureModeService.getFeatureModeForKey(modeName)).thenThrow(new FeatureModeNotFoundException(modeName, new ArrayList<>()));
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(featureName, modeName));
        testUnit.execute(context);
    }
}