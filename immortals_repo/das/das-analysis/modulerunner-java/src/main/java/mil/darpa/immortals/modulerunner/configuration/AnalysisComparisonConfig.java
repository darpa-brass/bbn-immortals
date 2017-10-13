package mil.darpa.immortals.modulerunner.configuration;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by awellman@bbn.com on 6/6/16.
 */
public class AnalysisComparisonConfig {

    public final AnalysisConfig runConfig;
    public final HashMap<String, LinkedList<String>> permutationMap;
    public final LinkedList<String> monitoredValues;

    public AnalysisComparisonConfig(AnalysisConfig runConfig) {
        this.runConfig = runConfig;
        this.permutationMap = new HashMap<>();
        this.monitoredValues = new LinkedList<>();
    }

    private static AnalysisConfig generateModifiedAnalysisConfig(@Nonnull AnalysisConfig baseAnalysisConfig, @Nonnull Map<String, String> varianceMap) throws NoSuchFieldException, IllegalAccessException {
        AnalysisConfig newConfig = baseAnalysisConfig.clone();

        for (String fieldIdentifier : varianceMap.keySet()) {
            String fieldValue = varianceMap.get(fieldIdentifier);
            Field field = AnalysisConfig.class.getDeclaredField(fieldIdentifier);

            if (field.getType() == Integer.class) {
                field.set(newConfig, Integer.valueOf(fieldValue));

            } else if (field.getType() == String.class) {
                field.set(newConfig, fieldValue);

            } else {
                throw new RuntimeException("Field type '" + field.getType() + "' does not match any known supported types!");
            }
        }
        return newConfig;
    }

    public Map<Map<String, String>, AnalysisConfig> getModifiedPermutations() throws IllegalAccessException, NoSuchFieldException {
        Map<Map<String, String>, AnalysisConfig> returnMap = new HashMap<>();

        Set<Map<String, String>> permutationCombos = new HashSet<>();

        for (String fieldIdentifier : permutationMap.keySet()) {

            Set<Map<String, String>> currentPermutationCombos = permutationCombos;
            permutationCombos = new HashSet<>();

            for (String fieldValue : permutationMap.get(fieldIdentifier)) {

                if (currentPermutationCombos.isEmpty()) {
                    Map<String, String> newMap = new HashMap<>();
                    newMap.put(fieldIdentifier, fieldValue);
                    permutationCombos.add(newMap);

                } else {
                    for (Map<String, String> tempCombo : currentPermutationCombos) {
                        Map<String, String> newMap = new HashMap<>(tempCombo);
                        newMap.put(fieldIdentifier, fieldValue);
                        permutationCombos.add(newMap);
                    }
                }
            }
        }

        for (Map<String, String> combo : permutationCombos) {
            AnalysisConfig newConfig = generateModifiedAnalysisConfig(runConfig, combo);
            returnMap.put(combo, newConfig);
        }
        return returnMap;
    }
}
