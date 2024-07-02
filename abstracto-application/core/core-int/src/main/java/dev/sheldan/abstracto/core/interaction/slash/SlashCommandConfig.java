package dev.sheldan.abstracto.core.interaction.slash;

import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

@Getter
@Builder
@EqualsAndHashCode
public class SlashCommandConfig {
    private boolean enabled;
    private String rootCommandName;
    private String userRootCommandName;
    private String groupName;
    private String userGroupName;
    private String commandName;
    private String userCommandName;

    @Builder.Default
    private boolean userInstallable = false;
    private UserCommandConfig userCommandConfig;

    public boolean matchesInteraction(CommandInteractionPayload payload) {
        String rootNameToUse = ContextUtils.isUserCommand(payload) ? StringUtils.defaultString(getUserSlashCompatibleRootName(), getSlashCompatibleRootName()) : getSlashCompatibleRootName();
        String groupNameToUse = ContextUtils.isUserCommand(payload) ? StringUtils.defaultString(getUserSlashCompatibleGroupName(), getSlashCompatibleGroupName()) : getSlashCompatibleGroupName();
        String commandNameToUse = ContextUtils.isUserCommand(payload) ? StringUtils.defaultString(getUserSlashCompatibleCommandName(), getSlashCompatibleCommandName()) : getSlashCompatibleCommandName();
        if(!StringUtils.equals(rootNameToUse, payload.getName())) {
            return false;
        }
        if(!StringUtils.equals(groupNameToUse, payload.getSubcommandGroup())) {
            return false;
        }
        if(!StringUtils.equals(commandNameToUse, payload.getSubcommandName())) {
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

    public String getUserSlashCompatibleRootName() {
        return userRootCommandName != null ? userRootCommandName.toLowerCase(Locale.ROOT) : null;
    }

    public String getUserSlashCompatibleGroupName() {
        return userGroupName != null ? userGroupName.toLowerCase(Locale.ROOT) : null;
    }

    public String getUserSlashCompatibleCommandName() {
        return userCommandName != null ? userCommandName.toLowerCase(Locale.ROOT) : null;
    }
}
