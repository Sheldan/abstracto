package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.service.management.UserNoteManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeleteNoteTest {

    @InjectMocks
    private DeleteNote testUnit;

    @Mock
    private UserNoteManagementService userNoteManagementService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ServerManagementService serverManagementService;

    private static final Long NOTE_ID = 5L;
    private static final Long SERVER_ID = 4L;

    @Test
    public void testDeleteExistingNote() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(NOTE_ID));

        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(parameters.getGuild())).thenReturn(server);
        when(userNoteManagementService.noteExists(NOTE_ID, server)).thenReturn(true);
        CommandResult result = testUnit.execute(parameters);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(userNoteManagementService, times(1)).deleteNote(NOTE_ID, server);
    }

    @Test
    public void testDeleteNotExistingNote() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(NOTE_ID));
        AServer server = Mockito.mock(AServer.class);
        when(parameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(parameters.getGuild())).thenReturn(server);
        when(userNoteManagementService.noteExists(NOTE_ID, server)).thenReturn(false);
        when(templateService.renderSimpleTemplate(DeleteNote.NOTE_NOT_FOUND_EXCEPTION_TEMPLATE, SERVER_ID)).thenReturn("error");
        CommandResult result = testUnit.execute(parameters);
        Assert.assertEquals(ResultState.ERROR, result.getResult());
        Assert.assertNotNull(result.getMessage());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
