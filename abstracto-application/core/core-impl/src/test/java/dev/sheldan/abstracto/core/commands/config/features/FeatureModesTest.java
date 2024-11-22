package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.FeatureNotFoundException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModeDisplay;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModesModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class FeatureModesTest {

    @InjectMocks
    private FeatureModes testUnit;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private ChannelService channelService;

    @Mock
    private FeatureConfigService featureConfigService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private FeatureManagementService featureManagementService;

    @Captor
    private ArgumentCaptor<FeatureModesModel> modelCaptor;

    private static final String FEATURE_NAME = "feature";

    @Test
    public void testExecuteNoParameters() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        FeatureModeDisplay display1 = Mockito.mock(FeatureModeDisplay.class);
        FeatureModeDisplay display2 = Mockito.mock(FeatureModeDisplay.class);
        List<FeatureModeDisplay> featureModeDisplays = Arrays.asList(display1, display2);
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(noParameters.getGuild())).thenReturn(server);
        when(featureModeService.getEffectiveFeatureModes(server)).thenReturn(featureModeDisplays);
        when(channelService.sendEmbedTemplateInMessageChannel(eq(FeatureModes.FEATURE_MODES_RESPONSE_TEMPLATE_KEY), modelCaptor.capture(), eq(noParameters.getChannel()))).thenReturn(new ArrayList<>());
        CompletableFuture<CommandResult> commandResultCompletableFuture = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(commandResultCompletableFuture);
        List<FeatureModeDisplay> usedDisplays = modelCaptor.getValue().getFeatureModes();
        Assert.assertEquals(featureModeDisplays.size(), usedDisplays.size());
        Assert.assertEquals(featureModeDisplays, usedDisplays);
    }

    @Test
    public void testExecuteFeatureParameter() {
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(FEATURE_NAME));
        FeatureModeDisplay display1 = Mockito.mock(FeatureModeDisplay.class);
        FeatureDefinition featureDefinition = Mockito.mock(FeatureDefinition.class);
        when(featureDefinition.getKey()).thenReturn(FEATURE_NAME);
        when(featureConfigService.getFeatureEnum(FEATURE_NAME)).thenReturn(featureDefinition);
        AFeature feature = Mockito.mock(AFeature.class);
        when(featureManagementService.getFeature(FEATURE_NAME)).thenReturn(feature);
        List<FeatureModeDisplay> featureModeDisplays = Arrays.asList(display1);
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(noParameters.getGuild())).thenReturn(server);
        when(featureModeService.getEffectiveFeatureModes(server, feature)).thenReturn(featureModeDisplays);
        when(channelService.sendEmbedTemplateInMessageChannel(eq(FeatureModes.FEATURE_MODES_RESPONSE_TEMPLATE_KEY), modelCaptor.capture(), eq(noParameters.getChannel()))).thenReturn(new ArrayList<>());
        CompletableFuture<CommandResult> commandResultCompletableFuture = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(commandResultCompletableFuture);
        List<FeatureModeDisplay> usedDisplays = modelCaptor.getValue().getFeatureModes();
        Assert.assertEquals(featureModeDisplays.size(), usedDisplays.size());
        Assert.assertEquals(featureModeDisplays, usedDisplays);
    }

    @Test(expected = FeatureNotFoundException.class)
    public void testExecuteDisableNotExistingFeature() {
        when(featureConfigService.getFeatureEnum(FEATURE_NAME)).thenThrow(new FeatureNotFoundException(FEATURE_NAME, new ArrayList<>()));
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(FEATURE_NAME));
        testUnit.executeAsync(context);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}