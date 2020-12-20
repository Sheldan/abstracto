package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.Prioritized;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

@NoArgsConstructor
public class BeanUtils {

    public static <T extends Prioritized> void sortPrioritizedListeners(List<T> prioritized) {
        if(prioritized != null) {
            prioritized.sort(Comparator.comparing(Prioritized::getPriority).reversed());
        }
    }
}
