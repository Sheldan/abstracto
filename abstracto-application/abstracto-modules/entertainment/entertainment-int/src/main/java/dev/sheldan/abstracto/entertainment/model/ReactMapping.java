package dev.sheldan.abstracto.entertainment.model;

import lombok.*;

import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactMapping {
    @Builder.Default
    private HashMap<String, List<String>> single = new HashMap<>();

    @Builder.Default
    private HashMap<String, String> combination = new HashMap<>();

    @Builder.Default
    private SortedSet<String> combinationKeys = new TreeSet<>((o1, o2) -> {
        if(o2.length() == o1.length()) {
            return o2.compareTo(o1);
        } else {
            return Integer.compare(o2.length(), o1.length());
        }
    });

    @Builder.Default
    private Set<String> combinationReplacements = new HashSet<>();

    public void populateKeys() {
        combinationKeys.addAll(combination.keySet());
        combinationKeys.forEach(s -> combinationReplacements.add(this.combination.get(s)));
    }

}
