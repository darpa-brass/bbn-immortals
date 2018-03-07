package mil.darpa.immortals

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by awellman@bbn.com on 1/18/18.
 */
class ImmortalizeTask extends DefaultTask {

    public static final String TASK_IDENTIFIER = "immortalize"

    @TaskAction
    immortalize() {
    }
}
