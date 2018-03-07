package mil.darpa.immortals.internal

import mil.darpa.immortals.config.ImmortalsConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by awellman@bbn.com on 11/1/17.
 */
abstract class JavaModuleImpl implements Plugin<Project> {
    
    abstract boolean isShadow()
    
    @Override
    void apply(Project target) {
        target.logger.info('Applying JavaModule to ' + target.path)
        Helpers.applyRepositories(target)
        Helpers.applyJava(target)
        def bc = ImmortalsConfig.getInstance().build
        target.sourceCompatibility = bc.augmentations.javaVersionCompatibility
        target.version = bc.augmentations.publishVersion
        def pathDerivedGroup = (target.rootProject.name + target.path)
                .replace(':' + target.name, '')
                .replaceAll(':', '.')
        if (pathDerivedGroup.equals(target.group.toString())) {
            target.group = bc.das.rootGroup + (target.path.count(':') > 1 ? ('.' + target.path.split(':')[1]) : '')
        }
        Helpers.applyPublish(target, isShadow())
    }
}
