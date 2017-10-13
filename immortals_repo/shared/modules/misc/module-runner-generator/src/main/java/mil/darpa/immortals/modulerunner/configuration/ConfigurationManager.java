//package mil.darpa.immortals.modulerunner.configuration;
//
//import javax.annotation.Nonnull;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.*;
//
///**
// * Created by awellman@bbn.com on 8/5/16.
// */
//public class ConfigurationManager {
//
//    ArrayList<AnalysisModuleConfiguration> configurations;
////    GsonHelper.AnalysisModuleConfigurationList configurations;
//
//    private static ConfigurationManager instance = null;
//
//    private ConfigurationManager() {
//        ClassLoader classLoader = getClass().getClassLoader();
//
//        InputStream is = classLoader.getResourceAsStream("Configurations.json");
//
//        try {
//            configurations = GsonHelper.getInstance().fromInputStream(is, GsonHelper.AnalysisModuleConfigurationList.class);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static ConfigurationManager getInstance() {
//        if (instance == null) {
//            instance = new ConfigurationManager();
//        }
//        return instance;
//    }
//
//    public Set<AnalysisModuleConfiguration> getModuleConfigurations(@Nonnull RuntimeEnvironmentConfiguration runtimeConfiguration, @Nonnull AnalysisModuleConfiguration desiredConfiguration) {
//        Set<AnalysisModuleConfiguration> matchingConfigurations = new HashSet<>();
//
//
//
//
//        for (AnalysisModuleConfiguration predefinedConfiguration : configurations) {
//            if (configurationMatches(runtimeConfiguration, desiredConfiguration, predefinedConfiguration)) {
//                matchingConfigurations.add(predefinedConfiguration);
//            }
//        }
//        return matchingConfigurations;
//    }
//
//    private boolean configurationMatches(@Nonnull RuntimeEnvironmentConfiguration runtimeConfiguration, @Nonnull AnalysisModuleConfiguration desiredConfiguration, @Nonnull AnalysisModuleConfiguration predefinedCompleteConfiguration) {
//
//        if (runtimeConfiguration.controlPointFormat != predefinedCompleteConfiguration.controlPointFormat) {
//            return false;
//        }
//
//        if (runtimeConfiguration.deploymentPlatform != predefinedCompleteConfiguration.deploymentPlatform) {
//            return false;
//        }
//
//        if (desiredConfiguration.classPackageIdentifier != null && !desiredConfiguration.classPackageIdentifier.equals("") &&
//                !desiredConfiguration.classPackageIdentifier.equals(predefinedCompleteConfiguration.classPackageIdentifier)) {
//            return false;
//        }
//
//        if (desiredConfiguration.aspect != null && !desiredConfiguration.aspect.equals("") &&
//                !desiredConfiguration.aspect.equals(predefinedCompleteConfiguration.aspect)) {
//            return false;
//        }
//
//        if (desiredConfiguration.functionalityBeingPerformed != null && !desiredConfiguration.functionalityBeingPerformed.equals("") &&
//                !desiredConfiguration.functionalityBeingPerformed.equals(predefinedCompleteConfiguration.functionalityBeingPerformed)) {
//            return false;
//        }
//
//        return true;
//    }
//
//    public AnalysisModuleConfiguration getModuleConfiguration(@Nonnull String classPackage, @Nonnull String aspect) {
//        for (AnalysisModuleConfiguration configuration : configurations) {
//            if (classPackage.equals(configuration.classPackageIdentifier) && aspect.equals(configuration.aspect)) {
//                return configuration;
//            }
//        }
//        return null;
//    }
//
//    public AnalysisModuleConfiguration getModuleConfiguration(@Nonnull String classPackage, @Nonnull String aspect, @Nonnull String controlPointFormat) {
//        for (AnalysisModuleConfiguration configuration : configurations) {
//            if (classPackage.equals(configuration.classPackageIdentifier) &&
//                    aspect.equals(configuration.aspect) &&
//                    controlPointFormat.equals(configuration.controlPointFormat.name())) {
//                return configuration;
//            }
//        }
//        return null;
//    }
//
//    public AnalysisGeneratorConfiguration getGeneratorConfiguration(@Nonnull String configurationIdentifier) {
//        try {
//            InputStream is = getClass().getClassLoader().getResourceAsStream("configurations/generators/" + configurationIdentifier + ".json");
//            return GsonHelper.getInstance().fromInputStream(is, AnalysisGeneratorConfiguration.class);
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public Map<String, List<String>> getDfuPackageAspectCombinations() {
//        Map<String, List<String>> m = new HashMap<>();
//
//        for (AnalysisModuleConfiguration configuration : configurations) {
//            List<String> aspectList = null;
//
//            if ((aspectList = m.get(configuration.classPackageIdentifier)) == null) {
//                aspectList = new LinkedList<>();
//                m.put(configuration.classPackageIdentifier, aspectList);
//            }
//
//            aspectList.add(configuration.aspect);
//        }
//        return m;
//    }
//}
