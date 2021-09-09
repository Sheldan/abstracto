package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.exception.FeatureNotFoundException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
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


@RunWith(MockitoJUnitRunner.class)
public class EnableFeatureModeTest {

    @InjectMocks
    private EnableMode testUnit;

    @Mock
    private FeatureConfigService featureConfigService;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private ServerManagementService serverManagementService;

    private static final Long SERVER_ID = 3L;

    @Test
    public void testExecuteDisable() {
        String featureName = "text";
        String modeName = "mode";
        FeatureDefinition featureDefinition = Mockito.mock(FeatureDefinition.class);
        when(featureConfigService.getFeatureEnum(featureName)).thenReturn(featureDefinition);
        FeatureMode featureMode = Mockito.mock(FeatureMode.class);
        when(featureModeService.getFeatureModeForKey(featureName, modeName)).thenReturn(featureMode);
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(featureName, modeName));
        AServer server = Mockito.mock(AServer.class);
        when(context.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        CommandResult commandResultCompletableFuture = testUnit.execute(context);
        CommandTestUtilities.checkSuccessfulCompletion(commandResultCompletableFuture);
        verify(featureModeService, times(1)).enableFeatureModeForFeature(featureDefinition, server, featureMode);
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
        FeatureDefinition featureDefinition = Mockito.mock(FeatureDefinition.class);
        when(featureConfigService.getFeatureEnum(featureName)).thenReturn(featureDefinition);
        when(featureModeService.getFeatureModeForKey(featureName, modeName)).thenThrow(new FeatureModeNotFoundException(modeName, new ArrayList<>()));
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(featureName, modeName));
        testUnit.execute(context);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}