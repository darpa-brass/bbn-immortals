package immortals

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Sets up the configuration for a java 8 component
 */
class Java8Component implements Plugin<Project> {

    void apply(Project project) {
        project.logger.info('Applying JavaDfu to ' + project.path)

        project.apply plugin: 'java'

        project.sourceCompatibility = project.toolJavaSourceCompatibility
        project.version project.defaultModulePublishVersion

        project.group project.rootGroup + (project.path.count(':') > 1 ? ('.' + project.path.split(':')[1]) : '')

        project.apply plugin: 'maven'

        project.sourceSets {
            main {
                java {
                    srcDirs = ['src/main/java']
                }
                resources {
                    srcDirs = ['src/main/resources']
                }
            }
            test {
                java {
                    srcDirs = ['src/test/java']
                }
            }
        }

        project.dependencies {
            compile 'com.google.code.findbugs:jsr305:3.0.1'
            compile 'com.google.code.gson:gson:2.5'
            testCompile group: 'junit', name: 'junit', version: '4.11'
        }
    }
}
