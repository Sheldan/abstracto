package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.DefaultFeatureMode;
import dev.sheldan.abstracto.core.repository.DefaultFeatureModeRepository;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DefaultFeatureModeManagementBeanTest {

    @InjectMocks
    private DefaultFeatureModeManagementBean testUnit;

    @Mock
    private DefaultFeatureModeRepository defaultFeatureModeRepository;

    @Mock
    private FeatureConfigService featureConfigService;

    @Mock
    private AFeature feature;

    @Mock
    private DefaultFeatureMode defaultFeatureMode1;

    @Mock
    private DefaultFeatureMode defaultFeatureMode2;

    private static final String MODE_NAME = "mode";

    @Test
    public void getFeatureModesForFeature() {
        List<DefaultFeatureMode> defaultFeatureModes = Arrays.asList(defaultFeatureMode1, defaultFeatureMode2);
        when(defaultFeatureModeRepository.findByFeature(feature)).thenReturn(defaultFeatureModes);
        List<DefaultFeatureMode> featureModesForFeature = testUnit.getFeatureModesForFeature(feature);
        Assert.assertEquals(defaultFeatureModes.size(), featureModesForFeature.size());
        Assert.assertEquals(defaultFeatureModes, featureModesForFeature);
    }

    @Test
    public void testGetAll() {
        List<DefaultFeatureMode> defaultFeatureModes = Arrays.asList(defaultFeatureMode1, defaultFeatureMode2);
        when(defaultFeatureModeRepository.findAll()).thenReturn(defaultFeatureModes);
        List<DefaultFeatureMode> featureModesForFeature = testUnit.getAll();
        Assert.assertEquals(defaultFeatureModes.size(), featureModesForFeature.size());
        Assert.assertEquals(defaultFeatureModes, featureModesForFeature);
    }

    @Test
    public void getFeatureModeOptional() {
        when(defaultFeatureModeRepository.findByFeatureAndMode(feature, MODE_NAME)).thenReturn(Optional.of(defaultFeatureMode1));
        Optional<DefaultFeatureMode> featureModeOptional = testUnit.getFeatureModeOptional(feature, MODE_NAME);
        Assert.assertTrue(featureModeOptional.isPresent());
        featureModeOptional.ifPresent(defaultFeatureMode ->
            Assert.assertEquals(defaultFeatureMode1, defaultFeatureMode)
        );
    }

    @Test
    public void getFeatureModeOptionalNotExisting() {
        when(defaultFeatureModeRepository.findByFeatureAndMode(feature, MODE_NAME)).thenReturn(Optional.empty());
        Optional<DefaultFeatureMode> featureModeOptional = testUnit.getFeatureModeOptional(feature, MODE_NAME);
        Assert.assertFalse(featureModeOptional.isPresent());
    }

    @Test
    public void getFeatureMode() {
        when(defaultFeatureModeRepository.findByFeatureAndMode(feature, MODE_NAME)).thenReturn(Optional.of(defaultFeatureMode1));
        DefaultFeatureMode defaultFeatureMode = testUnit.getFeatureMode(feature, MODE_NAME);
        Assert.assertEquals(defaultFeatureMode1, defaultFeatureMode);
    }

    @Test(expected = FeatureModeNotFoundException.class)
    public void getFeatureModeNotExisting() {
        when(defaultFeatureModeRepository.findByFeatureAndMode(feature, MODE_NAME)).thenReturn(Optional.empty());
        testUnit.getFeatureMode(feature, MODE_NAME);
    }
}