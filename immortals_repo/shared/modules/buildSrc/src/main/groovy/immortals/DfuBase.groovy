package immortals

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Sets up the core project within a DFU
 */
class DfuBase implements Plugin<Project> {
    void apply(Project project) {
        project.logger.info("Applying DfuBase to " + project.path)

        project.dependencies {
            compile project.rootProject.project('core')
        }
    }
}
