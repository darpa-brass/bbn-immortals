package immortals

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Sets up the configuration for a java DFU limited to java 7 since android and analysis do not like java 8
 */
class JavaDfu implements Plugin<Project> {

    void apply(Project project) {
        project.logger.info('Applying JavaDfu to ' + project.path)

        project.apply plugin: 'java'

        project.sourceCompatibility = project.dfuAndApplicationsJavaSourceCompatibility
        project.version project.defaultModulePublishVersion

        project.group project.rootGroup + (project.path.count(':') > 1 ? ('.' + project.path.split(':')[1]) : '')

        project.apply plugin: 'maven'

        project.sourceSets {
            main {
                java {
                    srcDirs = ['src/main/java']
                }
            }
            test {
                java {
                    srcDirs = ['src/test/java']
                }
            }
        }

        project.dependencies {
            compile 'com.securboration:immortals-adsl-generate:+'
            compile 'com.google.code.findbugs:jsr305:3.0.1'
            compile 'com.google.code.gson:gson:2.5'
//            compile 'log4j:log4j:1.2.17'
            // TODO: This is horrid.... But it works for now
            compile project.files(project.androidSDKJar)
            testCompile group: 'junit', name: 'junit', version: '4.11'
        }
    }
}