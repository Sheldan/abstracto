package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.repository.FeatureModeRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FeatureModeManagementServiceBeanTest {

    @InjectMocks
    private FeatureModeManagementServiceBean testUnit;

    @Mock
    private FeatureModeRepository featureModeRepository;

    @Mock
    private DefaultFeatureModeManagement defaultFeatureModeManagement;

    @Mock
    private AFeature feature;

    @Mock
    private AFeatureFlag featureFlag;

    @Mock
    private AServer server;

    @Mock
    private FeatureMode featureMode;

    @Mock
    private AFeatureMode aFeatureMode;

    private static final String FEATURE_MODE = "featureMode";

    @Test
    public void createModeWithModeAsString() {
        when(featureFlag.getServer()).thenReturn(server);
        AFeatureMode createdMode = testUnit.createMode(featureFlag, FEATURE_MODE, true);
        Assert.assertEquals(true, createdMode.getEnabled());
        Assert.assertEquals(featureFlag, createdMode.getFeatureFlag());
        Assert.assertEquals(FEATURE_MODE, createdMode.getFeatureMode());
        Assert.assertEquals(server, createdMode.getServer());
        verify(featureModeRepository, times(1)).save(createdMode);
    }

    @Test
    public void testCreateMode() {
        when(featureFlag.getServer()).thenReturn(server);
        when(featureMode.getKey()).thenReturn(FEATURE_MODE);
        AFeatureMode createdMode = testUnit.createMode(featureFlag, featureMode, true);
        Assert.assertEquals(true, createdMode.getEnabled());
        Assert.assertEquals(featureFlag, createdMode.getFeatureFlag());
        Assert.assertEquals(FEATURE_MODE, createdMode.getFeatureMode());
        Assert.assertEquals(server, createdMode.getServer());
        verify(featureModeRepository, times(1)).save(createdMode);
    }

    @Test
    public void featureModeActive() {
        when(featureMode.getKey()).thenReturn(FEATURE_MODE);
        when(aFeatureMode.getEnabled()).thenReturn(true);
        when(featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, feature, FEATURE_MODE)).thenReturn(Optional.of(aFeatureMode));
        Assert.assertTrue(testUnit.isFeatureModeActive(feature, server, featureMode));
    }

    @Test
    public void featureModeNotActive() {
        when(featureMode.getKey()).thenReturn(FEATURE_MODE);
        when(aFeatureMode.getEnabled()).thenReturn(false);
        when(featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, feature, FEATURE_MODE)).thenReturn(Optional.of(aFeatureMode));
        Assert.assertFalse(testUnit.isFeatureModeActive(feature, server, featureMode));
    }

    @Test
    public void featureModeNotPresent() {
        when(featureMode.getKey()).thenReturn(FEATURE_MODE);
        when(featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, feature, FEATURE_MODE)).thenReturn(Optional.empty());
        Assert.assertFalse(testUnit.isFeatureModeActive(feature, server, featureMode));
    }

    @Test
    public void featureModeStringExists() {
        when(featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, feature, FEATURE_MODE)).thenReturn(Optional.of(aFeatureMode));
        Assert.assertTrue(testUnit.doesFeatureModeExist(feature, server, FEATURE_MODE));
    }

    @Test
    public void featureModeObjectExists() {
        when(featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, feature, FEATURE_MODE)).thenReturn(Optional.of(aFeatureMode));
        when(featureMode.getKey()).thenReturn(FEATURE_MODE);
        Assert.assertTrue(testUnit.doesFeatureModeExist(feature, server, featureMode));
    }

    @Test
    public void featureModeDoesNotExist() {
        when(featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, feature, FEATURE_MODE)).thenReturn(Optional.empty());
        when(featureMode.getKey()).thenReturn(FEATURE_MODE);
        Assert.assertFalse(testUnit.doesFeatureModeExist(feature, server, featureMode));
    }

    @Test
    public void testGetFeatureModeOptional() {
        when(featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, feature, FEATURE_MODE)).thenReturn(Optional.of(aFeatureMode));
        Optional<AFeatureMode> featureModeOptional = testUnit.getFeatureMode(feature, server, FEATURE_MODE);
        Assert.assertTrue(featureModeOptional.isPresent());
        featureModeOptional.ifPresent(aFeatureMode1 ->
            Assert.assertEquals(aFeatureMode, aFeatureMode1)
        );
    }

    @Test
    public void testGetFeatureModeOptionalNotExisting() {
        when(featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, feature, FEATURE_MODE)).thenReturn(Optional.empty());
        Optional<AFeatureMode> featureModeOptional = testUnit.getFeatureMode(feature, server, FEATURE_MODE);
        Assert.assertFalse(featureModeOptional.isPresent());
    }

    @Test
    public void getFeatureModesOfServer() {
        List<AFeatureMode> expected = Arrays.asList(aFeatureMode, aFeatureMode);
        when(featureModeRepository.findByServer(server)).thenReturn(expected);
        List<AFeatureMode> featureModesOfServer = testUnit.getFeatureModesOfServer(server);
        Assert.assertEquals(expected, featureModesOfServer);
    }

    @Test
    public void getFeatureModesOfFeatureInServer() {
        List<AFeatureMode> expected = Arrays.asList(aFeatureMode);
        when(featureModeRepository.findByServerAndFeatureFlag_Feature(server, feature)).thenReturn(expected);
        List<AFeatureMode> featureModesOfServer = testUnit.getFeatureModesOfFeatureInServer(server, feature);
        Assert.assertEquals(expected, featureModesOfServer);
    }
}