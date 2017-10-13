//package mil.darpa.immortals.modulerunner.configuration;
//
//import javax.annotation.Nonnull;
//import java.lang.reflect.Field;
//import java.util.*;
//
///**
// * Created by awellman@bbn.com on 6/6/16.
// */
//public class AnalysisComparisonConfig {
//
////    public AnalysisGeneratorConfiguration originalDfu;
//    public String generatorConfigurationIdentifier;
//    public HashMap<String, LinkedList<String>> generatorPermutationMap;
//    public LinkedList<String> monitoredValues;
//
////    public AnalysisComparisonConfig(AnalysisGeneratorConfiguration originalDfu) {
////        this.originalDfu = originalDfu.clone();
////        this.generatorPermutationMap = new HashMap<>();
////        this.monitoredValues = new LinkedList<>();
////    }
//
//    private static AnalysisGeneratorConfiguration generateModifiedAnalysisConfig(@Nonnull AnalysisGeneratorConfiguration baseAnalysisConfig, @Nonnull Map<String, String> varianceMap) throws NoSuchFieldException, IllegalAccessException {
//        AnalysisGeneratorConfiguration newConfig = baseAnalysisConfig.clone();
//
//        for (String fieldIdentifier : varianceMap.keySet()) {
//            String fieldValue = varianceMap.get(fieldIdentifier);
//            Field field = AnalysisGeneratorConfiguration.class.getDeclaredField(fieldIdentifier);
//
//            if (field.getType() == Integer.class) {
//                field.set(newConfig, Integer.valueOf(fieldValue));
//
//            } else if (field.getType() == String.class) {
//                field.set(newConfig, fieldValue);
//
//            } else {
//                throw new RuntimeException("Field type '" + field.getType() + "' does not match any known supported types!");
//            }
//        }
//        return newConfig;
//    }
//
//    public Map<Map<String, String>, AnalysisGeneratorConfiguration> getModifiedPermutations() throws IllegalAccessException, NoSuchFieldException {
//        Map<Map<String, String>, AnalysisGeneratorConfiguration> returnMap = new HashMap<>();
//
//        Set<Map<String, String>> permutationCombos = new HashSet<>();
//
//        for (String fieldIdentifier : generatorPermutationMap.keySet()) {
//
//            Set<Map<String, String>> currentPermutationCombos = permutationCombos;
//            permutationCombos = new HashSet<>();
//
//            for (String fieldValue : generatorPermutationMap.get(fieldIdentifier)) {
//
//                if (currentPermutationCombos.isEmpty()) {
//                    Map<String, String> newMap = new HashMap<>();
//                    newMap.put(fieldIdentifier, fieldValue);
//                    permutationCombos.add(newMap);
//
//                } else {
//                    for (Map<String, String> tempCombo : currentPermutationCombos) {
//                        Map<String, String> newMap = new HashMap<>(tempCombo);
//                        newMap.put(fieldIdentifier, fieldValue);
//                        permutationCombos.add(newMap);
//                    }
//                }
//            }
//        }
//
//        AnalysisGeneratorConfiguration baseConfiguration = ConfigurationManager.getInstance().getGeneratorConfiguration(generatorConfigurationIdentifier);
//
//        for (Map<String, String> combo : permutationCombos) {
//            AnalysisGeneratorConfiguration newConfig = generateModifiedAnalysisConfig(baseConfiguration, combo);
//            returnMap.put(combo, newConfig);
//        }
//        return returnMap;
//    }
//}
