package mil.darpa.immortals

import com.android.build.gradle.AppExtension
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import groovy.xml.QName
import mil.darpa.immortals.analysis.adaptationtargets.*
import mil.darpa.immortals.config.ImmortalsConfig
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test

/**
 * Created by awellman@bbn.com on 3/29/18.
 */
class AnalyzeGradleBuildTask extends DefaultTask {

    public static final String TASK_IDENTIFIER = "analyzeGradleBuild"

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create()

    private static final ImmortalsGradlePublishData getPublishingData(Project project) {
        if (project.getPlugins().hasPlugin('maven-publish')) {
            PublishingExtension pe = project.getExtensions().findByType(PublishingExtension.class)
            Set<Publication> publications = pe.publications.findAll()
            MavenPublication publication = (MavenPublication) publications.first()

            if (publications.size() > 1) {
                project.logger.warn("Only single-publications are currently supported! Only considering '" + publication.toString() + "'!")
            }

            String[] publishParameters = ["publish"]
            return new ImmortalsGradlePublishData(
                    publication.groupId, publication.artifactId, publication.version, publishParameters)
        }
        return null
    }

    private static final ImmortalsGradleExecutionData getExecutionData(Project project) {
        String executionMainMethodClasspath = null
        String executionPackageIdentifier = null

        String executableFile = null

        if (project.hasProperty('jar')) {
            // If it is a java project that publishes a jar, extract applicable information from the manifest entry

            if (project.jar.hasProperty('archiveName')) {
                executableFile = project.jar.archiveName
            }

            if (project.jar.hasProperty('manifest') && project.jar.manifest.hasProperty('attributes') &&
                    project.jar.manifest.attributes instanceof Map<String, String>) {


                for (Map.Entry<String, String> entry : project.jar.manifest.attributes.entrySet()) {
                    if (entry.getKey().toLowerCase().equals("main-class")) {
                        executionMainMethodClasspath = entry.getValue()
                        executionPackageIdentifier = executionMainMethodClasspath.substring(0, executionMainMethodClasspath.lastIndexOf("."))
                        break
                    }
                }
            }

        } else if (project.hasProperty('android')) {
            // If it is an android application, extract the applicable information from the AndroidManifest.xml file
            
            // TODO; This can probably be done more programatically if I can figure out exactly where the final name is stored
            executableFile = project.name + '-debug.apk'

            if (project.android.hasProperty('sourceSets') && project.android.sourceSets.hasProperty('main') &&
                    project.android.sourceSets.main.hasProperty('properties') && project.android.sourceSets.main.hasProperty('manifestFile')) {
                File f = project.android.sourceSets.main.manifestFile
                Node data = new XmlParser().parse(f)

                if (data.attributes().containsKey('package')) {
                    executionPackageIdentifier = data.attribute('package')
                }

                def app = data['application']
                if (app.size() > 0) {
                    if (app[0]['activity'].size() > 0) {
                        for (Node activity : app[0]['activity']) {
                            String activityName = null
                            for (Map.Entry<Object, Object> entry : activity.attributes()) {
                                if (entry.key.class == QName) {
                                    QName key = (QName) entry.key
                                    if (key.namespaceURI.equals('http://schemas.android.com/apk/res/android') &&
                                            key.getLocalPart().equals('name')) {
                                        activityName = entry.value
                                    }
                                }
                            }

                            if (activity['intent-filter'].size() > 0) {
                                for (Node intentFilter : activity['intent-filter']) {
                                    if (intentFilter['action'].size() > 0) {
                                        for (Node intentFilterAction : intentFilter['action']) {
                                            for (Map.Entry<Object, Object> entry : intentFilterAction.attributes()) {
                                                if (entry.key.class == QName) {
                                                    QName key = (QName) entry.key
                                                    if (key.namespaceURI.equals('http://schemas.android.com/apk/res/android') &&
                                                            key.getLocalPart().equals('name') &&
                                                            entry.value.equals('android.intent.action.MAIN')
                                                    ) {
                                                        executionMainMethodClasspath = executionPackageIdentifier + activityName
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ImmortalsGradleExecutionData(
                2000,
                executableFile,
                null,
                executionPackageIdentifier,
                executionMainMethodClasspath
        )
    }

    private static final ImmortalsGradleTestData getTestingData(Project project) {
        String repoLocation = project.rootProject.buildFile.parent.toString() + "/"
        String projectRepoPath = (project.buildFile.parent + "/").replace(repoLocation, "")


        String testResultXmlSubdirectory = null
        if (project.hasProperty('test') && project.test.hasProperty('reports') &&
                project.test.reports.hasProperty('junitXml') && project.test.reports.junitXml.hasProperty('destination')) {
            testResultXmlSubdirectory = project.test.reports.junitXml.destination.absolutePath.replace(repoLocation, "").replace(projectRepoPath, "") + '/'
        }

        if (testResultXmlSubdirectory != null) {

            String testCoverageReportXmlFileSubpath = null
            if (project.hasProperty('jacocoTestReport') && project.jacocoTestReport.hasProperty('reports') &&
                    project.jacocoTestReport.reports.hasProperty('xml') &&
                    project.jacocoTestReport.reports.xml.hasProperty('destination')) {
                testCoverageReportXmlFileSubpath = project.jacocoTestReport.reports.xml.destination.absolutePath.replace(repoLocation, "").replace(projectRepoPath, "")
            }

            String[] buildToolValidationParameters = null
            if (project.tasks.findAll { t -> t instanceof Test && 'validate'.equals(t.name) }.size() > 0) {
                buildToolValidationParameters = ["--daemon", "clean", "validate"]
            } else if (project.tasks.findAll { t -> t instanceof Test }.size() > 0) {
                buildToolValidationParameters = ["--daemon", "clean", "test"]
            }

            if (buildToolValidationParameters != null) {
                return new ImmortalsGradleTestData(buildToolValidationParameters, testResultXmlSubdirectory, testCoverageReportXmlFileSubpath)
            }
        }
        return null
    }

    static final void performBuildAnalysis(Project project) {
        def logger = project.logger
        if (!(project.getPlugins().hasPlugin("com.android.application") || project.plugins.hasPlugin('java'))) {
            logger.warn("Nothing to do for project '" + project.path + "'")
            return
        }

        // Get the basic data common to all gradle projects
        String targetName = project.name
        String targetGroup = project.group
        String targetVersion = project.version
        String repoLocation = project.rootProject.buildFile.parent.toString() + "/"
        String projectRepoPath = (project.buildFile.parent + "/").replace(repoLocation, "")
        String buildFile = project.buildFile.toString().replace(repoLocation, "").replace(projectRepoPath, "")
        BuildPlatform buildPlatform = BuildPlatform.GRADLE

        Set<File> mainSrcDirs
        DeploymentTarget deploymentTarget
        String deploymentTargetVersion

        if (project.getPlugins().hasPlugin("com.android.application")) {
            deploymentTarget = DeploymentTarget.ANDROID
            AppExtension android = (AppExtension) project.getProperties().get('android')
            mainSrcDirs = android.getSourceSets().getByName("main").java.srcDirs
            deploymentTargetVersion = android.defaultConfig.targetSdkVersion.apiString

        } else {
            JavaPluginConvention jpc = project.getConvention().findPlugin(JavaPluginConvention.class)
            if (jpc == null) {
                throw new GradleException("All projects are expected to be Android or Java based!")
            }

            deploymentTarget = DeploymentTarget.JAVA
            mainSrcDirs = jpc.getSourceSets().getByName("main").java.srcDirs
            deploymentTargetVersion = project.targetCompatibility
        }

        File f = mainSrcDirs.first()
        if (mainSrcDirs.size() > 1) {
            logger.warn("Only single-directory source sets are currently supported! Only considering '" + f.toString() + "'!")
        }
        String sourceSubdirectory = (f.absolutePath + "/").replace(repoLocation + projectRepoPath, "")

        ImmortalsGradlePublishData publishingData = getPublishingData(project)
        ImmortalsGradleTestData testingData = getTestingData(project)
        ImmortalsGradleExecutionData executionData = getExecutionData(project)

        String[] buildToolBuildParameters = ["clean", "build", "-x", "test"]

        ImmortalsGradleProjectData igpd = new ImmortalsGradleProjectData(
                targetName,
                targetGroup,
                targetVersion,
                deploymentTarget,
                deploymentTargetVersion,
                repoLocation,
                projectRepoPath,
                buildFile,
                sourceSubdirectory,
                buildPlatform,
                buildToolBuildParameters,
                testingData,
                publishingData,
                executionData,
                null,
                null
        )

        File dataFile = ImmortalsConfig.instance.extensions.immortalizer.producedDataTargetFile.toFile()
        JsonObject jo = dataFile.exists() ? gson.fromJson(new FileReader(dataFile), JsonObject.class) : new JsonObject()
        jo.add(igpd.getIdentifier(), gson.toJsonTree(igpd, ImmortalsGradleProjectData.class))
        FileUtils.write(dataFile, gson.toJson(jo))
    }

    @TaskAction
    void analyzeGradleBuild() {
        performBuildAnalysis(project)
    }
}
