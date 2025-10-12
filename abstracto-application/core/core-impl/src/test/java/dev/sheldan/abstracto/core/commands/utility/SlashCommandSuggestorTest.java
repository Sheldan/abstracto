package dev.sheldan.abstracto.core.commands.utility;

import static dev.sheldan.abstracto.core.commands.utility.SlashCommandSuggestor.SUGGESTION_TEMPLATE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandReceivedHandler;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureMode;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SlashCommandSuggestorTest {

    @InjectMocks
    private SlashCommandSuggestor unitUnderTest;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private CommandReceivedHandler commandReceivedHandler;

    @Mock
    private CommandManager commandManager;

    @Mock
    private FeatureFlagService featureFlagService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ChannelService channelService;

    @Mock
    private Guild guild;

    @Mock
    private Message message;

    @Mock
    private Command command;

    @Mock
    private FeatureMode featureMode;

    private static final Long SERVER_ID = 1L;
    private static final String COMMAND_NAME = "commandName";

    @Before
    public void setup() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(message.getGuildIdLong()).thenReturn(SERVER_ID);
        when(message.getAuthor()).thenReturn(Mockito.mock(User.class));
        when(message.getChannel()).thenReturn(Mockito.mock(MessageChannelUnion.class));
    }

    @Test
    public void shouldNotExecute_DueToFeatureMode() {
        when(featureModeService.featureModeActive(CoreFeatureDefinition.CORE_FEATURE, SERVER_ID, CoreFeatureMode.SUGGEST_SLASH_COMMANDS)).thenReturn(false);
        boolean shouldExecute = unitUnderTest.shouldExecute(null, guild, message);
        assertThat(shouldExecute).isFalse();
    }

    @Test
    public void shouldNotExecute_DueToNotFoundCommand() {
        when(featureModeService.featureModeActive(CoreFeatureDefinition.CORE_FEATURE, SERVER_ID, CoreFeatureMode.SUGGEST_SLASH_COMMANDS)).thenReturn(true);
        commandFound(null);
        boolean shouldExecute = unitUnderTest.shouldExecute(null, guild, message);
        assertThat(shouldExecute).isFalse();
    }

    @Test
    public void shouldNotExecute_DueToFoundCommandWhichIsNotSlashCommandOnly() {
        when(featureModeService.featureModeActive(CoreFeatureDefinition.CORE_FEATURE, SERVER_ID, CoreFeatureMode.SUGGEST_SLASH_COMMANDS)).thenReturn(true);
        commandSetup(false);
        boolean shouldExecute = unitUnderTest.shouldExecute(null, guild, message);
        assertThat(shouldExecute).isFalse();
    }
    
    @Test
    public void shouldExecute_DueToFoundCommandWhichIsSlashCommandOnly() {
        when(featureModeService.featureModeActive(CoreFeatureDefinition.CORE_FEATURE, SERVER_ID, CoreFeatureMode.SUGGEST_SLASH_COMMANDS)).thenReturn(true);
        commandSetup(true);
        boolean shouldExecute = unitUnderTest.shouldExecute(null, guild, message);
        assertThat(shouldExecute).isTrue();
    }

    @Test
    public void shouldNotFindCommand() {
        commandFound(null);
        unitUnderTest.execute(null, message);
        verify(templateService, times(0)).renderEmbedTemplate(eq(SUGGESTION_TEMPLATE_KEY), any(), any());
    }

    @Test
    public void foundCommandIsNotSlashCommandOnly() {
        commandSetup(false);
        unitUnderTest.execute(null, message);
        verify(templateService, times(0)).renderEmbedTemplate(eq(SUGGESTION_TEMPLATE_KEY), any(), any());
    }

    @Test
    public void featureNotEnabled() {
        commandSetup(true);
        when(featureFlagService.getFeatureFlagValue(any(), eq(SERVER_ID))).thenReturn(false);
        unitUnderTest.execute(null, message);
        verify(templateService, times(0)).renderEmbedTemplate(eq(SUGGESTION_TEMPLATE_KEY), any(), any());
    }

    @Test
    public void noFeatureModesAvailable() {
        commandSetup(true);
        when(command.getFeatureModeLimitations()).thenReturn(new ArrayList<>());
        when(featureFlagService.getFeatureFlagValue(any(), eq(SERVER_ID))).thenReturn(true);
        unitUnderTest.execute(null, message);
        verify(templateService).renderEmbedTemplate(eq(SUGGESTION_TEMPLATE_KEY), any(), any());
    }

    @Test
    public void featureModesAvailable() {
        commandSetup(true);
        when(command.getFeatureModeLimitations()).thenReturn(Collections.singletonList(featureMode));
        when(featureFlagService.getFeatureFlagValue(any(), eq(SERVER_ID))).thenReturn(true);
        when(featureModeService.featureModeActive(any(), eq(SERVER_ID), any())).thenReturn(true);
        unitUnderTest.execute(null, message);
        verify(templateService).renderEmbedTemplate(eq(SUGGESTION_TEMPLATE_KEY), any(), any());
    }

    private void commandSetup(boolean slashCommandOnly) {
        commandFound(command);
        CommandConfiguration commandConfiguration = CommandConfiguration
            .builder()
            .slashCommandOnly(slashCommandOnly)
            .build();
        when(command.getConfiguration()).thenReturn(commandConfiguration);
    }

    private void commandFound(Command command) {
        when(commandReceivedHandler.getCommandName(message)).thenReturn(COMMAND_NAME);
        when(commandManager.getCommandByNameOptional(COMMAND_NAME, true, SERVER_ID)).thenReturn(Optional.ofNullable(command));
    }
}
