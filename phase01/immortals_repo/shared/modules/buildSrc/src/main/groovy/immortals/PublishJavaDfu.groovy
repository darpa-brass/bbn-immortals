package immortals

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.publish.maven.MavenPublication

/**
 * Sets up the publishing of a basic Java DFU, including validation.
 */
class PublishJavaDfu implements Plugin<Project> {

    protected String getTargetRepository(Project project) {
        return project.localRepoPath
    }

    void apply(Project project) {
        project.logger.info("Applying PublishJavaDfu to " + project.path)

        project.apply plugin: 'maven-publish'

        project.publishing {
            publications {
                JavaComponent(MavenPublication) {
                    from project.components.java
                }
            }
            repositories {
                maven {
                    url getTargetRepository(project)
                }
            }
        }

        project.clean.doLast {
            String rootPublishDir = getTargetRepository(project) + ((String) project.rootGroup).replaceAll('\\.', '/') + '/'
            String publishDir = rootPublishDir + project.getPath().substring(1).replace(':', '/') + '/'

            String[] artifacts = [
                    'maven-metadata.xml',
                    'maven-metadata.xml.md5',
                    'maven-metadata.xml.sha1',
                    project.defaultSoftwarePublishVersion + '/' + project.name + '-' + project.defaultSoftwarePublishVersion + '.jar',
                    project.defaultSoftwarePublishVersion + '/' + project.name + '-' + project.defaultSoftwarePublishVersion + '.jar.md5',
                    project.defaultSoftwarePublishVersion + '/' + project.name + '-' + project.defaultSoftwarePublishVersion + '.jar.sha1',
                    project.defaultSoftwarePublishVersion + '/' + project.name + '-' + project.defaultSoftwarePublishVersion + '.pom',
                    project.defaultSoftwarePublishVersion + '/' + project.name + '-' + project.defaultSoftwarePublishVersion + '.pom.md5',
                    project.defaultSoftwarePublishVersion + '/' + project.name + '-' + project.defaultSoftwarePublishVersion + '.pom.sha1',
            ]

            for (String artifact : artifacts) {
                File f = new File(publishDir + artifact)
                if (f.exists()) {
                    f.delete()
                }
            }

            FileTree ft = project.fileTree(dir: publishDir)

            for (File f : ft.getFiles()) {
                if (f.isDirectory() && project.fileTree(dir: f.getAbsolutePath()).isEmpty()) {
                    new File(f.getAbsolutePath()).deleteDir()
                } else {
                    println('The publish directory for "' + f.getPath() + '" is not empty! Please update the cleaning scripts!')
                }
            }
        }


        project.task('publishIfUnpublished') {
            String publishDir = getTargetRepository(project) +
                    ((String) project.rootGroup).replaceAll('\\.', '/') +
                    project.getPath().replaceAll(':bundles:', ':').replaceAll(':', '/') + '/'

            String xmlFile = publishDir + 'maven-metadata.xml'
            String pomFile = publishDir + project.defaultSoftwarePublishVersion + '/' + project.getName() + '-' + project.defaultSoftwarePublishVersion + '.pom'
            String jarFile = publishDir + project.defaultSoftwarePublishVersion + '/' + project.getName() + '-' + project.defaultSoftwarePublishVersion + '.jar'

            if (!((new File(xmlFile)).exists() && (new File(pomFile)).exists() && (new File(jarFile)).exists())) {
                finalizedBy(project.publish)
            }
        }

    }

}