package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.DefaultConfigProperties;
import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.property.FeatureModeProperty;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DefaultFeatureModeManagementBeanTest {

    @InjectMocks
    private DefaultFeatureModeManagementBean testUnit;

    @Mock
    private DefaultConfigProperties defaultConfigProperties;

    @Mock
    private AFeature feature;

    @Mock
    private FeatureConfigService featureConfigService;

    @Mock
    private FeatureModeProperty defaultFeatureMode1;

    @Mock
    private FeatureModeProperty defaultFeatureMode2;

    private static final String MODE_NAME = "mode";
    private static final String FEATURE_NAME = "feature";

    @Mock
    private HashMap<String, FeatureModeProperty> mockedMap;

    @Test
    public void getFeatureModesForFeature() {
        when(defaultFeatureMode1.getFeatureName()).thenReturn(FEATURE_NAME);
        when(defaultFeatureMode2.getFeatureName()).thenReturn(FEATURE_NAME);
        when(feature.getKey()).thenReturn(FEATURE_NAME);
        List<FeatureModeProperty> defaultFeatureModes = Arrays.asList(defaultFeatureMode1, defaultFeatureMode2);
        when(defaultConfigProperties.getFeatureModes()).thenReturn(mockedMap);
        when(mockedMap.values()).thenReturn(defaultFeatureModes);
        List<FeatureModeProperty> featureModesForFeature = testUnit.getFeatureModesForFeature(feature);
        Assert.assertEquals(defaultFeatureModes.size(), featureModesForFeature.size());
        Assert.assertEquals(defaultFeatureModes, featureModesForFeature);
    }

    @Test
    public void testGetAll() {
        List<FeatureModeProperty> defaultFeatureModes = Arrays.asList(defaultFeatureMode1, defaultFeatureMode2);
        when(defaultConfigProperties.getFeatureModes()).thenReturn(mockedMap);
        when(mockedMap.values()).thenReturn(defaultFeatureModes);
        List<FeatureModeProperty> featureModesForFeature = testUnit.getAll();
        Assert.assertEquals(defaultFeatureModes.size(), featureModesForFeature.size());
        Assert.assertEquals(defaultFeatureModes, featureModesForFeature);
    }

    @Test
    public void getFeatureModeOptional() {
        when(defaultConfigProperties.getFeatureModes()).thenReturn(mockedMap);
        when(mockedMap.get(MODE_NAME)).thenReturn(defaultFeatureMode1);
        Optional<FeatureModeProperty> featureModeOptional = testUnit.getFeatureModeOptional(feature, MODE_NAME);
        Assert.assertTrue(featureModeOptional.isPresent());
        featureModeOptional.ifPresent(defaultFeatureMode ->
            Assert.assertEquals(defaultFeatureMode1, defaultFeatureMode)
        );
    }

    @Test
    public void getFeatureModeOptionalNotExisting() {
        Optional<FeatureModeProperty> featureModeOptional = testUnit.getFeatureModeOptional(feature, MODE_NAME);
        Assert.assertFalse(featureModeOptional.isPresent());
    }

    @Test
    public void getFeatureMode() {
        when(defaultConfigProperties.getFeatureModes()).thenReturn(mockedMap);
        when(mockedMap.get(MODE_NAME)).thenReturn(defaultFeatureMode1);
        FeatureModeProperty defaultFeatureMode = testUnit.getFeatureMode(feature, MODE_NAME);
        Assert.assertEquals(defaultFeatureMode1, defaultFeatureMode);
    }

    @Test(expected = FeatureModeNotFoundException.class)
    public void getFeatureModeNotExisting() {
        when(feature.getKey()).thenReturn(FEATURE_NAME);
        when(featureConfigService.getFeatureModesFromFeatureAsString(FEATURE_NAME)).thenReturn(new ArrayList<>());
        testUnit.getFeatureMode(feature, MODE_NAME);
    }
}