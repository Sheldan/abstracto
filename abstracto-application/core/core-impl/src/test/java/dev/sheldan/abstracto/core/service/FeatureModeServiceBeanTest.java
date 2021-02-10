package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.property.FeatureModeProperty;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModeDisplay;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureModeManagement;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureModeManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FeatureModeServiceBeanTest {

    @InjectMocks
    private FeatureModeServiceBean testUnit;

    @Mock
    private FeatureConfigService featureConfigService;

    @Mock
    private FeatureFlagManagementService featureFlagManagementService;

    @Mock
    private FeatureManagementService featureManagementService;

    @Mock
    private FeatureModeManagementService featureModeManagementService;

    @Mock
    private DefaultFeatureModeManagement defaultFeatureModeManagement;

    @Mock
    private FeatureMode featureMode;

    @Mock
    private AFeatureMode aFeatureMode;

    @Mock
    private AServer server;

    @Mock
    private FeatureEnum featureEnum;

    @Mock
    private AFeature feature;

    @Mock
    private AFeatureFlag featureFlag;

    @Mock
    private FeatureModeProperty defaultFeatureMode;

    @Mock
    private FeatureConfig featureConfig;

    private static final String FEATURE_NAME = "feature";
    private static final String FEATURE_MODE = "mode";

    @Test
    public void enableFeatureModeForFeatureWhichAlreadyExists() {
        when(featureEnum.getKey()).thenReturn(FEATURE_NAME);
        when(featureManagementService.getFeature(featureEnum.getKey())).thenReturn(feature);
        when(featureModeManagementService.getFeatureMode(feature, server, featureMode)).thenReturn(Optional.of(aFeatureMode));
        testUnit.enableFeatureModeForFeature(featureEnum, server, featureMode);
        verify(aFeatureMode, times(1)).setEnabled(true);
    }

    @Test
    public void enableFeatureModeForFeatureCreatingNewMode() {
        when(featureEnum.getKey()).thenReturn(FEATURE_NAME);
        when(featureManagementService.getFeature(featureEnum.getKey())).thenReturn(feature);
        when(featureModeManagementService.getFeatureMode(feature, server, featureMode)).thenReturn(Optional.empty());
        when(featureFlagManagementService.getFeatureFlag(feature, server)).thenReturn(Optional.of(featureFlag));
        testUnit.enableFeatureModeForFeature(featureEnum, server, featureMode);
        verify(featureModeManagementService, times(1)).createMode(featureFlag, featureMode, true);
    }

    @Test
    public void setFutureModeForFutureEnable() {
        when(featureEnum.getKey()).thenReturn(FEATURE_NAME);
        when(featureManagementService.getFeature(featureEnum.getKey())).thenReturn(feature);
        when(featureModeManagementService.getFeatureMode(feature, server, featureMode)).thenReturn(Optional.of(aFeatureMode));
        testUnit.setFutureModeForFuture(featureEnum, server, featureMode, true);
        verify(aFeatureMode, times(1)).setEnabled(true);
    }

    @Test
    public void setFutureModeForFutureDisable() {
        when(featureEnum.getKey()).thenReturn(FEATURE_NAME);
        when(featureManagementService.getFeature(featureEnum.getKey())).thenReturn(feature);
        when(featureModeManagementService.getFeatureMode(feature, server, featureMode)).thenReturn(Optional.of(aFeatureMode));
        testUnit.setFutureModeForFuture(featureEnum, server, featureMode, false);
        verify(aFeatureMode, times(1)).setEnabled(false);
    }

    @Test
    public void disableFeatureModeForFeatureWhichAlreadyExists() {
        when(featureEnum.getKey()).thenReturn(FEATURE_NAME);
        when(featureManagementService.getFeature(featureEnum.getKey())).thenReturn(feature);
        when(featureModeManagementService.getFeatureMode(feature, server, featureMode)).thenReturn(Optional.of(aFeatureMode));
        testUnit.disableFeatureModeForFeature(featureEnum, server, featureMode);
        verify(aFeatureMode, times(1)).setEnabled(false);
    }

    @Test
    public void disableFeatureModeForFeatureCreatingNewMode() {
        when(featureEnum.getKey()).thenReturn(FEATURE_NAME);
        when(featureManagementService.getFeature(featureEnum.getKey())).thenReturn(feature);
        when(featureModeManagementService.getFeatureMode(feature, server, featureMode)).thenReturn(Optional.empty());
        when(featureFlagManagementService.getFeatureFlag(feature, server)).thenReturn(Optional.of(featureFlag));
        testUnit.disableFeatureModeForFeature(featureEnum, server, featureMode);
        verify(featureModeManagementService, times(1)).createMode(featureFlag, featureMode, false);
    }

    @Test
    public void testFeatureModeActiveForCustomizedFeatureMode() {
        when(featureEnum.getKey()).thenReturn(FEATURE_NAME);
        when(featureManagementService.getFeature(featureEnum.getKey())).thenReturn(feature);
        when(featureModeManagementService.doesFeatureModeExist(feature, server, featureMode)).thenReturn(true);
        when(featureModeManagementService.isFeatureModeActive(feature, server, featureMode)).thenReturn(true);
        boolean actualResult = testUnit.featureModeActive(featureEnum, server, featureMode);
        Assert.assertTrue(actualResult);
    }

    @Test
    public void testFeatureModeActiveForDefaultFeatureMode() {
        when(featureEnum.getKey()).thenReturn(FEATURE_NAME);
        when(featureMode.getKey()).thenReturn(FEATURE_MODE);
        when(featureManagementService.getFeature(featureEnum.getKey())).thenReturn(feature);
        when(featureModeManagementService.doesFeatureModeExist(feature, server, featureMode)).thenReturn(false);
        when(defaultFeatureModeManagement.getFeatureMode(feature, FEATURE_MODE)).thenReturn(defaultFeatureMode);
        when(defaultFeatureMode.getEnabled()).thenReturn(true);
        boolean actualResult = testUnit.featureModeActive(featureEnum, server, featureMode);
        Assert.assertTrue(actualResult);
    }

    @Test
    public void getFeatureModeForKey() {
        FeatureConfig featureConfig1 = Mockito.mock(FeatureConfig.class);
        when(featureMode.getKey()).thenReturn(FEATURE_MODE);
        FeatureMode featureMode2 = Mockito.mock(FeatureMode.class);
        when(featureConfig1.getAvailableModes()).thenReturn(Arrays.asList(featureMode, featureMode2));
        when(featureConfigService.getAllFeatureConfigs()).thenReturn(Arrays.asList(featureConfig1));
        FeatureMode featureModeForKey = testUnit.getFeatureModeForKey(FEATURE_MODE);
        Assert.assertEquals(featureMode, featureModeForKey);
    }

    @Test
    public void testGetAllAvailableFeatureModes() {
        FeatureConfig featureConfig1 = Mockito.mock(FeatureConfig.class);
        FeatureMode featureMode2 = Mockito.mock(FeatureMode.class);
        when(featureConfig1.getAvailableModes()).thenReturn(Arrays.asList(featureMode, featureMode2));
        FeatureConfig featureConfig2 = Mockito.mock(FeatureConfig.class);
        FeatureMode featureMode3 = Mockito.mock(FeatureMode.class);
        FeatureMode featureMode4 = Mockito.mock(FeatureMode.class);
        when(featureConfig2.getAvailableModes()).thenReturn(Arrays.asList(featureMode3, featureMode4));
        when(featureConfigService.getAllFeatureConfigs()).thenReturn(Arrays.asList(featureConfig1, featureConfig2));
        List<FeatureMode> allAvailableFeatureModes = testUnit.getAllAvailableFeatureModes();
        Assert.assertEquals(4, allAvailableFeatureModes.size());
        Assert.assertEquals(featureMode, allAvailableFeatureModes.get(0));
        Assert.assertEquals(featureMode2, allAvailableFeatureModes.get(1));
        Assert.assertEquals(featureMode3, allAvailableFeatureModes.get(2));
        Assert.assertEquals(featureMode4, allAvailableFeatureModes.get(3));
    }

    @Test
    public void testEffectiveFeatureModesOnlyOneDefault() {
        when(defaultFeatureMode.getEnabled()).thenReturn(true);
        when(defaultFeatureMode.getFeatureName()).thenReturn(FEATURE_NAME);
        when(defaultFeatureModeManagement.getAll()).thenReturn(Arrays.asList(defaultFeatureMode));
        when(featureConfigService.getFeatureDisplayForFeature(FEATURE_NAME)).thenReturn(featureConfig);
        when(featureModeManagementService.getFeatureModesOfServer(server)).thenReturn(new ArrayList<>());
        when(defaultFeatureMode.getMode()).thenReturn(FEATURE_MODE);
        when(featureFlagManagementService.getFeatureFlag(FEATURE_NAME, server)).thenReturn(Optional.of(featureFlag));
        List<FeatureModeDisplay> effectiveFeatureModes = testUnit.getEffectiveFeatureModes(server);
        Assert.assertEquals(1, effectiveFeatureModes.size());
        FeatureModeDisplay featureModeDisplay = effectiveFeatureModes.get(0);
        Assert.assertEquals(true, featureModeDisplay.getIsDefaultValue());
        Assert.assertTrue(featureModeDisplay.getFeatureMode().getEnabled());
        Assert.assertEquals(featureFlag, featureModeDisplay.getFeatureMode().getFeatureFlag());
        Assert.assertEquals(server, featureModeDisplay.getFeatureMode().getServer());
        Assert.assertEquals(FEATURE_MODE, featureModeDisplay.getFeatureMode().getFeatureMode());
        Assert.assertEquals(featureConfig, featureModeDisplay.getFeatureConfig());
    }

    @Test
    public void testEffectiveFeatureModesOnlyOneCustomized() {
        when(aFeatureMode.getEnabled()).thenReturn(true);
        when(aFeatureMode.getFeatureFlag()).thenReturn(featureFlag);
        when(aFeatureMode.getServer()).thenReturn(server);
        when(aFeatureMode.getFeatureMode()).thenReturn(FEATURE_MODE);
        when(featureFlag.getFeature()).thenReturn(feature);
        when(feature.getKey()).thenReturn(FEATURE_NAME);
        when(defaultFeatureModeManagement.getAll()).thenReturn(Arrays.asList(defaultFeatureMode));
        when(featureConfigService.getFeatureDisplayForFeature(FEATURE_NAME)).thenReturn(featureConfig);
        when(featureModeManagementService.getFeatureModesOfServer(server)).thenReturn(Arrays.asList(aFeatureMode));
        when(defaultFeatureMode.getMode()).thenReturn(FEATURE_MODE);
        List<FeatureModeDisplay> effectiveFeatureModes = testUnit.getEffectiveFeatureModes(server);
        Assert.assertEquals(1, effectiveFeatureModes.size());
        FeatureModeDisplay featureModeDisplay = effectiveFeatureModes.get(0);
        Assert.assertEquals(false, featureModeDisplay.getIsDefaultValue());
        Assert.assertTrue(featureModeDisplay.getFeatureMode().getEnabled());
        Assert.assertEquals(featureFlag, featureModeDisplay.getFeatureMode().getFeatureFlag());
        Assert.assertEquals(server, featureModeDisplay.getFeatureMode().getServer());
        Assert.assertEquals(FEATURE_MODE, featureModeDisplay.getFeatureMode().getFeatureMode());
        Assert.assertEquals(featureConfig, featureModeDisplay.getFeatureConfig());
    }

    @Test
    public void testEffectiveFeatureModesMixed() {
        when(aFeatureMode.getEnabled()).thenReturn(true);
        when(aFeatureMode.getFeatureFlag()).thenReturn(featureFlag);
        when(aFeatureMode.getServer()).thenReturn(server);
        when(aFeatureMode.getFeatureMode()).thenReturn(FEATURE_MODE);
        when(featureFlag.getFeature()).thenReturn(feature);
        when(feature.getKey()).thenReturn(FEATURE_NAME);
        FeatureModeProperty defaultFeatureMode2 = Mockito.mock(FeatureModeProperty.class);
        String secondMode = "SECOND";
        when(defaultFeatureMode2.getMode()).thenReturn(secondMode);
        String feature2 = "FEATURE2";
        when(defaultFeatureMode2.getFeatureName()).thenReturn(feature2);
        when(defaultFeatureMode2.getEnabled()).thenReturn(false);
        when(defaultFeatureModeManagement.getAll()).thenReturn(Arrays.asList(defaultFeatureMode2, defaultFeatureMode));
        when(featureConfigService.getFeatureDisplayForFeature(FEATURE_NAME)).thenReturn(featureConfig);
        when(featureConfigService.getFeatureDisplayForFeature(feature2)).thenReturn(featureConfig);
        when(featureModeManagementService.getFeatureModesOfServer(server)).thenReturn(Arrays.asList(aFeatureMode));
        when(defaultFeatureMode.getMode()).thenReturn(FEATURE_MODE);
        when(featureFlagManagementService.getFeatureFlag(feature2, server)).thenReturn(Optional.of(featureFlag));

        List<FeatureModeDisplay> effectiveFeatureModes = testUnit.getEffectiveFeatureModes(server);

        Assert.assertEquals(2, effectiveFeatureModes.size());
        FeatureModeDisplay activeFeaturemodeDisplay = effectiveFeatureModes.get(0);
        Assert.assertEquals(false, activeFeaturemodeDisplay.getIsDefaultValue());
        Assert.assertTrue(activeFeaturemodeDisplay.getFeatureMode().getEnabled());
        Assert.assertEquals(featureFlag, activeFeaturemodeDisplay.getFeatureMode().getFeatureFlag());
        Assert.assertEquals(server, activeFeaturemodeDisplay.getFeatureMode().getServer());
        Assert.assertEquals(FEATURE_MODE, activeFeaturemodeDisplay.getFeatureMode().getFeatureMode());
        Assert.assertEquals(featureConfig, activeFeaturemodeDisplay.getFeatureConfig());

        FeatureModeDisplay defaultFeatureModeDisplay = effectiveFeatureModes.get(1);
        Assert.assertEquals(true, defaultFeatureModeDisplay.getIsDefaultValue());
        Assert.assertFalse(defaultFeatureModeDisplay.getFeatureMode().getEnabled());
        Assert.assertEquals(featureFlag, defaultFeatureModeDisplay.getFeatureMode().getFeatureFlag());
        Assert.assertEquals(server, defaultFeatureModeDisplay.getFeatureMode().getServer());
        Assert.assertEquals(secondMode, defaultFeatureModeDisplay.getFeatureMode().getFeatureMode());
        Assert.assertEquals(featureConfig, defaultFeatureModeDisplay.getFeatureConfig());
    }

    @Test
    public void testGetEffectiveFeatureModesForFeature() {
        when(defaultFeatureMode.getEnabled()).thenReturn(true);
        when(defaultFeatureMode.getFeatureName()).thenReturn(FEATURE_NAME);
        when(defaultFeatureModeManagement.getFeatureModesForFeature(feature)).thenReturn(Arrays.asList(defaultFeatureMode));
        when(featureConfigService.getFeatureDisplayForFeature(FEATURE_NAME)).thenReturn(featureConfig);
        when(featureModeManagementService.getFeatureModesOfFeatureInServer(server, feature)).thenReturn(new ArrayList<>());
        when(defaultFeatureMode.getMode()).thenReturn(FEATURE_MODE);
        when(featureFlagManagementService.getFeatureFlag(FEATURE_NAME, server)).thenReturn(Optional.of(featureFlag));
        List<FeatureModeDisplay> effectiveFeatureModes = testUnit.getEffectiveFeatureModes(server, feature);
        Assert.assertEquals(1, effectiveFeatureModes.size());
        FeatureModeDisplay featureModeDisplay = effectiveFeatureModes.get(0);
        Assert.assertEquals(true, featureModeDisplay.getIsDefaultValue());
        Assert.assertTrue(featureModeDisplay.getFeatureMode().getEnabled());
        Assert.assertEquals(featureFlag, featureModeDisplay.getFeatureMode().getFeatureFlag());
        Assert.assertEquals(server, featureModeDisplay.getFeatureMode().getServer());
        Assert.assertEquals(FEATURE_MODE, featureModeDisplay.getFeatureMode().getFeatureMode());
        Assert.assertEquals(featureConfig, featureModeDisplay.getFeatureConfig());
    }
}