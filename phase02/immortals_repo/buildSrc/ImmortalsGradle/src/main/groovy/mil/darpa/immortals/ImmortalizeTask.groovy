package mil.darpa.immortals

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by awellman@bbn.com on 1/18/18.
 */
class ImmortalizeTask extends DefaultTask {

    public static final String TASK_IDENTIFIER = "immortalize"

    private final String SECURBORATION_BYTECODE_TASK = "bytecode"

    @TaskAction
    immortalize() {
        ImmortalizerPluginExtension ipe = project.getExtensions().findByType(ImmortalizerPluginExtension.class)
        if (ipe.perfromGradleBuildAnalysis) {
            AnalyzeGradleBuildTask.performBuildAnalysis(project)
        }
    }
}
