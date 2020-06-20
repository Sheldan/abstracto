package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.moderation.config.features.WarningDecayFeature;
import dev.sheldan.abstracto.moderation.models.template.commands.MyWarningsModel;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyWarningsTest {

    @InjectMocks
    private MyWarnings testUnit;

    @Mock
    private ChannelService channelService;

    @Mock
    private WarnManagementService warnManagementService;

    @Mock
    private FeatureFlagService featureFlagService;

    @Mock
    private WarningDecayFeature warningDecayFeature;

    @Captor
    private ArgumentCaptor<MyWarningsModel> argumentCaptor;

    @Test
    public void testExecuteMyWarningsCommand() {
        CommandContext noParameter = CommandTestUtilities.getNoParameters();
        Long activeWarnCount = 8L;
        when(warnManagementService.getActiveWarnsForUser(noParameter.getUserInitiatedContext().getAUserInAServer())).thenReturn(activeWarnCount);
        Long totalWarnCount = 10L;
        when(warnManagementService.getTotalWarnsForUser(noParameter.getUserInitiatedContext().getAUserInAServer())).thenReturn(totalWarnCount);
        CommandResult result = testUnit.execute(noParameter);
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(MyWarnings.MY_WARNINGS_RESPONSE_EMBED_TEMPLATE), argumentCaptor.capture(), eq(noParameter.getChannel()));
        CommandTestUtilities.checkSuccessfulCompletion(result);
        MyWarningsModel usedModel = argumentCaptor.getValue();
        Assert.assertEquals(activeWarnCount, usedModel.getCurrentWarnCount());
        Assert.assertEquals(totalWarnCount, usedModel.getTotalWarnCount());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
