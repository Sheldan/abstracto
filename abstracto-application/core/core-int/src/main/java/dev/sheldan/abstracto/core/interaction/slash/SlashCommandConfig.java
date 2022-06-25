package dev.sheldan.abstracto.core.interaction.slash;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;

import java.util.Locale;

@Getter
@Builder
@EqualsAndHashCode
public class SlashCommandConfig {
    private boolean enabled;
    private String rootCommandName;
    private String groupName;
    private String commandName;

    public boolean matchesInteraction(CommandInteractionPayload payload) {
        if(getSlashCompatibleRootName() != null && payload.getName() != null && !getSlashCompatibleRootName().equals(payload.getName())) {
            return false;
        }
        if(getSlashCompatibleGroupName() != null && payload.getSubcommandGroup() != null && !getSlashCompatibleGroupName().equals(payload.getSubcommandGroup())) {
            return false;
        }
        if(getSlashCompatibleCommandName() != null && payload.getSubcommandName() != null && !getSlashCompatibleCommandName().equals(payload.getSubcommandName())) {
            return false;
        }

        return true;
    }

    public String getSlashCompatibleRootName() {
        return rootCommandName != null ? rootCommandName.toLowerCase(Locale.ROOT) : null;
    }

    public String getSlashCompatibleGroupName() {
        return groupName != null ? groupName.toLowerCase(Locale.ROOT) : null;
    }

    public String getSlashCompatibleCommandName() {
        return commandName != null ? commandName.toLowerCase(Locale.ROOT) : null;
    }
}
