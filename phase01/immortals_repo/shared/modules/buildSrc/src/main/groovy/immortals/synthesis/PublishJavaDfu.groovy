package immortals.synthesis

import org.gradle.api.Project

/**
 * Sets up the publishing of a basic Java DFU, including validation.
 */
class PublishJavaDfu extends immortals.PublishJavaDfu {

    @Override
    protected String getTargetRepository(Project project) {
        return project.synthRepoPath
    }

}