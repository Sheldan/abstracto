package dev.sheldan.abstracto.core.command.execution;

import dev.sheldan.abstracto.core.command.exception.CommandParameterKeyValueWrongTypeException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface CommandParameterKey {
    static <T extends Enum<T>> T getEnumFromKey(Class<T> clazz, String key) {
        try {
            if(clazz != null && key != null ) {
                return Enum.valueOf(clazz, key.trim().toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            List<T> ts = Arrays.asList(clazz.getEnumConstants());
            List<String> keys = ts.stream().map(Enum::toString).map(String::toLowerCase).collect(Collectors.toList());
            throw new CommandParameterKeyValueWrongTypeException(keys);
        }
        throw new IllegalArgumentException("Clazz and key must not be null");
    }

    static <T extends Enum<T>> List<String> getKeys(Class<T> clazz) {
        return Arrays.stream(clazz.getEnumConstants())
            .map(Enum::name)
            .toList();
    }
}
