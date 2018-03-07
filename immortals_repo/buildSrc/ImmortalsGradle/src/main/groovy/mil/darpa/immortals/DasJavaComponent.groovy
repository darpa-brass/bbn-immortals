package mil.darpa.immortals

import mil.darpa.immortals.config.ImmortalsConfig
import mil.darpa.immortals.internal.Helpers
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Sets up the configuration for a java 8 component
 */
class DasJavaComponent implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.logger.info('Applying DasJavaComponent to ' + target.path)
        Helpers.applyRepositories(target)
        Helpers.applyJava(target)
        def bc = ImmortalsConfig.getInstance().build
        target.sourceCompatibility = bc.das.javaVersionCompatibility
        target.version = bc.das.publishVersion
        target.group = bc.das.rootGroup + '.components' + (target.path.count(':') > 1 ? ('.' + target.path.split(':')[1]) : '')
    }
}
