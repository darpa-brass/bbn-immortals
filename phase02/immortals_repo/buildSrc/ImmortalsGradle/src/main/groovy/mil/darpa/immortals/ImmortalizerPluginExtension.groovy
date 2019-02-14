package mil.darpa.immortals

import mil.darpa.immortals.config.ImmortalsConfig

/**
 * Created by awellman@bbn.com on 1/18/18.
 */
class ImmortalizerPluginExtension {
    boolean performCompleteGradleTaskAnalysis = ImmortalsConfig.instance.extensions.immortalizer.performKrgpCompleteGradleTaskAnalysis
    boolean performBytecodeAnalysis = ImmortalsConfig.instance.extensions.immortalizer.performKrgpBytecodeAnalysis
    boolean perfromGradleBuildAnalysis = ImmortalsConfig.instance.extensions.immortalizer.performBuildFileAnalysis
    String ttlTargetDir = ImmortalsConfig.instance.extensions.krgp.ttlTargetDirectory
}

