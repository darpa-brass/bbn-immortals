package mil.darpa.immortals

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by awellman@bbn.com on 1/18/18.
 */
class ImmortalizerPlugin implements Plugin<Project> {

    private final String ANALYSIS_TASK_IDENTIFIER = "immortalize"
    private final String PLUGIN_EXTENSION_IDENTIFIER = "immortalizer"

    private final String SECURBORATION_PLUGIN_IDENTIFIER = "com.securboration.p2t"
    private final String SECURBORATION_BYTECODE_TASK = "bytecode"

    @Override
    void apply(Project project) {
        // Add the immortalize task
        project.tasks.create(ImmortalizeTask.TASK_IDENTIFIER, ImmortalizeTask.class)
        project.tasks.create(ImmortalizeCleanTask.TASK_IDENTIFIER, ImmortalizeCleanTask.class);

        // Add the immortalizer configuration extension
        project.getExtensions().add(PLUGIN_EXTENSION_IDENTIFIER, new ImmortalizerPluginExtension())

        project.afterEvaluate {

            // Get the original task list
            Set<Task> originalTasks = project.tasks.findAll()

            // Apply the Securboration gradle plugin
            project.getPlugins().apply(SECURBORATION_PLUGIN_IDENTIFIER)

            // Rename the group for Securboration tasks 
            project.tasks.findAll {
                it
                if (!originalTasks.contains(it)) {
                    if (it.group.equals('IMMoRTALS')) {
                        it.group = 'IMMoRTALS KRGP'
                    }
                }
            }

            // After everything has been evaluated, propagate settings from the immortalizer plugin extension to other plugins
            ImmortalizerPluginExtension ipe = project.getExtensions().getByType(ImmortalizerPluginExtension.class)
            project.krgp.targetDir = ipe.ttlTargetDir
            project.krgp.includedLibs = []

            if (ipe.performBytecodeAnalysis) {
                project.getTasks().findByName(ANALYSIS_TASK_IDENTIFIER).finalizedBy(project.getTasks().findByName("bytecode"))
            }
        }
    }
}
