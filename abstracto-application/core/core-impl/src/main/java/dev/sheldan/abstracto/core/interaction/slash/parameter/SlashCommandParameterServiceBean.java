package dev.sheldan.abstracto.core.interaction.slash.parameter;

import dev.sheldan.abstracto.core.interaction.slash.parameter.provider.SlashCommandParameterProvider;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.EmoteService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class SlashCommandParameterServiceBean implements SlashCommandParameterService {

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private List<SlashCommandParameterProvider> parameterProviders;

    @Override
    public <T, Z> Z getCommandOption(String name, SlashCommandInteractionEvent event, Class<T> parameterType, Class<Z> slashParameterType) {
        name = name.toLowerCase(Locale.ROOT);
        List<OptionType> potentialOptionTypes = getTypesFromParameter(parameterType);
        OptionType actualOptionType = potentialOptionTypes.size() == 1 ? potentialOptionTypes.get(0) : null;
        if (potentialOptionTypes.size() > 1) {
            for (OptionType optionType: potentialOptionTypes) {
                if(event.getOption(getFullQualifiedParameterName(name, optionType)) != null) {
                    actualOptionType = optionType;
                    break;
                }
            }
        }
        if(actualOptionType == null) {
            throw new IllegalArgumentException(String.format("Could not determine option type for parameter %s", name));
        }
        if(potentialOptionTypes.size() > 1) {
            name = getFullQualifiedParameterName(name, actualOptionType);
        }
        if(actualOptionType == OptionType.BOOLEAN) {
            return slashParameterType.cast(event.getOption(name).getAsBoolean());
        } else if (actualOptionType == OptionType.ATTACHMENT) {
            return slashParameterType.cast(event.getOption(name).getAsAttachment());
        } else if (actualOptionType == OptionType.NUMBER) {
            return slashParameterType.cast(event.getOption(name).getAsDouble());
        } else if(actualOptionType == OptionType.STRING) {
            return slashParameterType.cast(event.getOption(name).getAsString());
        } else if(actualOptionType == OptionType.INTEGER) {
            return slashParameterType.cast(event.getOption(name).getAsInt());
        } else if(actualOptionType == OptionType.ROLE) {
            return slashParameterType.cast(event.getOption(name).getAsRole());
        } else if(actualOptionType == OptionType.MENTIONABLE) {
            return slashParameterType.cast(event.getOption(name).getAsMentionable());
        } else if(actualOptionType == OptionType.CHANNEL) {
            return slashParameterType.cast(event.getOption(name).getAsChannel());
        } else if(actualOptionType == OptionType.USER) {
            if(parameterType.equals(User.class) && slashParameterType.equals(User.class)) {
                return slashParameterType.cast(event.getOption(name).getAsUser());
            } else {
                return slashParameterType.cast(event.getOption(name).getAsMember());
            }
        } else {
            throw new AbstractoRunTimeException("Unknown parameter type");
        }
    }

    @Override
    public <T, Z> boolean hasCommandOption(String name, SlashCommandInteractionEvent event, Class<T> parameterType, Class<Z> slashParameterType) {
        name = name.toLowerCase(Locale.ROOT);
        List<OptionType> potentialOptionTypes = getTypesFromParameter(parameterType);
        OptionType actualOptionType = potentialOptionTypes.size() == 1 ? potentialOptionTypes.get(0) : null;
        if (potentialOptionTypes.size() > 1) {
            for (OptionType optionType: potentialOptionTypes) {
                if(event.getOption(getFullQualifiedParameterName(name, optionType)) != null) {
                    actualOptionType = optionType;
                    break;
                }
            }
        }
        if(actualOptionType == null) {
            return false;
        }
        if(potentialOptionTypes.size() > 1) {
            name = getFullQualifiedParameterName(name, actualOptionType);
        }
        if(actualOptionType == OptionType.BOOLEAN) {
            return slashParameterType.isInstance(event.getOption(name).getAsBoolean());
        } else if (actualOptionType == OptionType.ATTACHMENT) {
            return slashParameterType.isInstance(event.getOption(name).getAsAttachment());
        } else if (actualOptionType == OptionType.NUMBER) {
            return slashParameterType.isInstance(event.getOption(name).getAsDouble());
        } else if(actualOptionType == OptionType.STRING) {
            return slashParameterType.isInstance(event.getOption(name).getAsString());
        } else if(actualOptionType == OptionType.INTEGER) {
            return slashParameterType.isInstance(event.getOption(name).getAsInt());
        } else if(actualOptionType == OptionType.ROLE) {
            return slashParameterType.isInstance(event.getOption(name).getAsRole());
        } else if(actualOptionType == OptionType.MENTIONABLE) {
            return slashParameterType.isInstance(event.getOption(name).getAsMentionable());
        } else if(actualOptionType == OptionType.CHANNEL) {
            return slashParameterType.isInstance(event.getOption(name).getAsChannel());
        } else if(actualOptionType == OptionType.USER) {
            if (parameterType.equals(User.class) && slashParameterType.equals(User.class)) {
                return slashParameterType.isInstance(event.getOption(name).getAsUser());
            } else {
                return slashParameterType.isInstance(event.getOption(name).getAsMember());
            }
        } else {
            return false;
        }
    }

    @Override
    public <T> T getCommandOption(String name, SlashCommandInteractionEvent event, Class<T> parameterType) {
        return getCommandOption(name, event, parameterType, parameterType);
    }

    @Override
    public Object getCommandOption(String name, SlashCommandInteractionEvent event) {
        return event.getOption(name);
    }

    @Override
    public Boolean hasCommandOption(String name, SlashCommandInteractionEvent event) {
        return event.getOption(name) != null;
    }

    @Override
    public Boolean hasCommandOptionWithFullType(String name, SlashCommandInteractionEvent event, OptionType optionType) {
        return hasCommandOption(getFullQualifiedParameterName(name, optionType), event);
    }

    @Override
    public AEmote loadAEmoteFromString(String input, SlashCommandInteractionEvent event) {
        Emoji emoji = loadEmoteFromString(input, event);
        return emoteService.getFakeEmoteFromEmoji(emoji);
    }

    @Override
    public Emoji loadEmoteFromString(String input, SlashCommandInteractionEvent event) {
        if(StringUtils.isNumeric(input)) {
            long emoteId = Long.parseLong(input);
            return event.getGuild().getEmojiById(emoteId);
        }
        return Emoji.fromFormatted(input);
    }

    @Override
    public List<OptionType> getTypesFromParameter(Class clazz) {
        return parameterProviders
                .stream()
                .filter(slashCommandParameterProvider -> slashCommandParameterProvider.getOptionMapping().getType().equals(clazz))
                .findAny()
                .map(slashCommandParameterProvider -> slashCommandParameterProvider.getOptionMapping().getOptionTypes())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown type for slash command parameter desired %s", clazz.getName())));
    }


    @Override
    public String getFullQualifiedParameterName(String name, OptionType type) {
        switch (type) {
            case STRING:
                return name + "_string";
            case INTEGER:
                return name + "_integer";
            case BOOLEAN:
                return name + "_boolean";
            case USER:
                return name + "_user";
            case CHANNEL:
                return name + "_channel";
            case ROLE:
                return name + "_role";
            case MENTIONABLE:
                return name + "_mentionable";
            case NUMBER:
                return name + "_number";
            case ATTACHMENT:
                return name + "_attachment";
            default: throw new IllegalArgumentException(String.format("Not supported parameter type %s", type));
        }
    }

}
