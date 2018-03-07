package mil.darpa.immortals

import mil.darpa.immortals.config.ImmortalsConfig
import mil.darpa.immortals.internal.Helpers
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by awellman@bbn.com on 11/1/17.
 */
class AndroidModule implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.logger.info('Applying AndroidModule to ' + target.path)
        Helpers.applyRepositories(target)
        Helpers.applyJava(target)
        def bc = ImmortalsConfig.getInstance().build
        target.sourceCompatibility = '1.7'
        target.version = bc.augmentations.publishVersion
        def pathDerivedGroup = (target.rootProject.name + target.path)
                .replace(':' + target.name, '')
                .replaceAll(':', '.')
        if (pathDerivedGroup.equals(target.group.toString())) {
            target.group = bc.das.rootGroup + (target.path.count(':') > 1 ? ('.' + target.path.split(':')[1]) : '')
        }
        target.dependencies {
            compile target.files(bc.augmentations.androidSdkJar.toString())
        }
        Helpers.applyPublish(target)
    }
}
