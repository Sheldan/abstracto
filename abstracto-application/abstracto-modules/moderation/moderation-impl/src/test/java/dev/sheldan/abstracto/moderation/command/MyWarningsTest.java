package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.config.feature.WarningDecayFeatureConfig;
import dev.sheldan.abstracto.moderation.model.template.command.MyWarningsModel;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
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
    private WarningDecayFeatureConfig warningDecayFeatureConfig;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Captor
    private ArgumentCaptor<MyWarningsModel> argumentCaptor;

    @Test
    public void testExecuteMyWarningsCommand() {
        CommandContext noParameter = CommandTestUtilities.getNoParameters();
        Long activeWarnCount = 8L;
        AUserInAServer aUserInAServer = Mockito.mock(AUserInAServer.class);
        when(userInServerManagementService.loadOrCreateUser(noParameter.getAuthor())).thenReturn(aUserInAServer);
        when(warnManagementService.getActiveWarnCountForUser(aUserInAServer)).thenReturn(activeWarnCount);
        Long totalWarnCount = 10L;
        when(warnManagementService.getTotalWarnsForUser(aUserInAServer)).thenReturn(totalWarnCount);
        CommandResult result = testUnit.execute(noParameter);
        verify(channelService, times(1)).sendEmbedTemplateInTextChannelList(eq(MyWarnings.MY_WARNINGS_RESPONSE_EMBED_TEMPLATE), argumentCaptor.capture(), eq(noParameter.getChannel()));
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
