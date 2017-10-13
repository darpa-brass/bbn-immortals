package immortals

import org.gradle.api.Plugin
import org.gradle.api.Project
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

        project.clean.finalizedBy {
            String rootPublishDir = getTargetRepository(project) + ((String) project.group).replaceAll('\\.', '/') + '/'
            String publishDir = rootPublishDir + project.getName().replace(':', '/') + '/'

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

            String fp = publishDir + '/' + (String) project.defaultSoftwarePublishVersion

            while (fp.length() >= getTargetRepository(project).length()) {
                File f = new File(fp)
                if (f.isDirectory() && project.fileTree(dir: f.getAbsolutePath()).isEmpty()) {
                    new File(f.getAbsolutePath()).deleteDir()
                }
                fp = fp.substring(0, fp.lastIndexOf('/'))
            }
        }

        project.jar.finalizedBy(project.publish)
    }

}
