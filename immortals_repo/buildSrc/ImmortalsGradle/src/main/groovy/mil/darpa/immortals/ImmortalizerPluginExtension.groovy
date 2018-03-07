package mil.darpa.immortals

import mil.darpa.immortals.config.ImmortalsConfig

/**
 * Created by awellman@bbn.com on 1/18/18.
 */
class ImmortalizerPluginExtension {
    boolean performBytecodeAnalysis = ImmortalsConfig.instance.extensions.krgp.performBytecodeAnalysis
    String ttlTargetDir = ImmortalsConfig.instance.extensions.krgp.ttlTargetDirectory
}

