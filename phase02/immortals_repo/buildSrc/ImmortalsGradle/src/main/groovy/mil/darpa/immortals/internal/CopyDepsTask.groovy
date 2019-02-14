package mil.darpa.immortals.internal

import org.gradle.api.tasks.Copy

/**
 * Created by awellman@bbn.com on 1/18/18.
 */
class CopyDepsTask extends Copy {
    CopyDepsTask() {
        super()
        from getProject().configurations.findResults { i -> i.isCanBeResolved() ? i : null }
        into "krgp"
    }
}
