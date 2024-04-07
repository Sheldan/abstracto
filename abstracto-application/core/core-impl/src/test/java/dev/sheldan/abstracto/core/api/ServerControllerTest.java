package dev.sheldan.abstracto.core.api;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.api.GuildDisplay;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerControllerTest {
    @InjectMocks
    private ServerController unitToTest;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private GuildService guildService;

    @Mock
    private Guild guild;

    private static final Long GUILD_ID = 1L;

    @Test
    public void testExecuteRequest() {
        when(guildService.getGuildById(GUILD_ID)).thenReturn(guild);
        String guildIdString = String.valueOf(GUILD_ID);
        when(guild.getId()).thenReturn(guildIdString);
        GuildDisplay response = unitToTest.getLeaderboard(GUILD_ID);
        assertThat(response.getId()).isEqualTo(guildIdString);
    }

    @Test
    public void testExecuteServerNotFound() {
        when(serverManagementService.loadServer(GUILD_ID)).thenThrow(new GuildNotFoundException(GUILD_ID));
        assertThatThrownBy(() -> unitToTest.getLeaderboard(GUILD_ID))
                .isInstanceOf(GuildNotFoundException.class)
                .hasMessageContaining("Guild not found");
    }
}
