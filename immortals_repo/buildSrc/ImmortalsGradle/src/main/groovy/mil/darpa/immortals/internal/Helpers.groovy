package mil.darpa.immortals.internal

import mil.darpa.immortals.config.ImmortalsConfig
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

import java.nio.file.Paths

/**
 * Created by awellman@bbn.com on 11/6/17.
 */
class Helpers {

    static void applyRepositories(Project target) {
        target.apply plugin: 'maven'
        target.repositories {
            mavenCentral()
            maven {
                url ImmortalsConfig.getInstance().globals.immortalsRepo
            }
        }
    }

    static void applyPublish(Project target, boolean shadowJar) {
        def configuration = ImmortalsConfig.getInstance()
        def bc = configuration.build
        def immortalsRepo = Paths.get(configuration.globals.immortalsRepo).toAbsolutePath().toString()

        target.apply plugin: 'maven-publish'
        
        target.tasks.create("sourceJar", Jar.class) {
            classifier 'sources'
            from target.sourceSets.main.allJava
        }
        
        if (shadowJar) {
            target.apply plugin: 'com.github.johnrengelman.shadow'
            
            target.shadowJar {
                classifier = ''
                mergeServiceFiles()
            }

            target.publishing {
                publications {
                    JavaComponentPublication(MavenPublication) { publication ->
                        target.shadow.component(publication)
                        artifact target.tasks.sourceJar
                    }
                }
                repositories {
                    maven {
                        url immortalsRepo
                    }
                }
            }
            
        } else {
            target.publishing {
                publications {
                    JavaComponentPublication(MavenPublication) {
                        from target.components.java
                        artifact target.tasks.sourceJar
                    }
                }
                repositories {
                    maven {
                        url immortalsRepo
                    }
                }
            }
        }

        target.clean.doLast {
            String rootPublishDir = immortalsRepo + '/' + ((String) target.group).replaceAll('\\.', '/') + '/'
            String publishDir = rootPublishDir + target.getName().replace(':', '/') + '/'

            String[] artifacts = [
                    'maven-metadata.xml',
                    'maven-metadata.xml.md5',
                    'maven-metadata.xml.sha1',
                    target.version + '/' + target.name + '-' + bc.das.publishVersion + '.jar',
                    target.version + '/' + target.name + '-' + bc.das.publishVersion + '.jar.md5',
                    target.version + '/' + target.name + '-' + bc.das.publishVersion + '.jar.sha1',
                    target.version + '/' + target.name + '-' + bc.das.publishVersion + '.pom',
                    target.version + '/' + target.name + '-' + bc.das.publishVersion + '.pom.md5',
                    target.version + '/' + target.name + '-' + bc.das.publishVersion + '.pom.sha1',
            ]

            for (String artifact : artifacts) {
                File f = new File(publishDir + artifact)
                if (f.exists()) {
                    f.delete()
                }
            }

            String fp = publishDir + '/' + (String) target.version

            while (fp.length() >= immortalsRepo.length()) {
                File f = new File(fp)
                if (f.isDirectory() && target.fileTree(dir: f.getAbsolutePath()).isEmpty()) {
                    new File(f.getAbsolutePath()).deleteDir()
                }
                fp = fp.substring(0, fp.lastIndexOf('/'))
            }
        }

        if (target.hasProperty('jar')) {
            target.jar.finalizedBy(target.publish)
        }
    }

    static void applyJava(Project target) {
        target.apply plugin: 'java'

        def bc = ImmortalsConfig.getInstance().build
        target.sourceCompatibility = bc.das.javaVersionCompatibility
        target.version = bc.das.publishVersion
        
        target.jar {
            manifest {
                attributes "Implementation-Vendor": "BBN Technologies",
                "Implementation-Version": ImmortalsConfig.instance.build.augmentations.publishVersion
            }
        }

        target.sourceSets {
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
                resources {
                    srcDirs = ['src/test/resources']
                }
            }
        }

        // TODO: Shadow these if shadowing in use!
        target.dependencies {
            compile 'com.securboration:immortals-adsl-generate:+'
            compile 'com.google.code.findbugs:jsr305:3.0.1'
            compile 'com.google.code.gson:gson:2.7'
            compile "org.slf4j:slf4j-api:${ImmortalsConfig.instance.build.das.slf4jVersion}"
            testCompile group: 'junit', name: 'junit', version: '4.11'
        }
    }
}
