package mil.darpa.immortals

import mil.darpa.immortals.config.ImmortalsConfig
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path

/**
 * Created by awellman@bbn.com on 1/26/18.
 */
class ImmortalizeCleanTask extends DefaultTask {

    public static final String TASK_IDENTIFIER = "immortalizeClean"

    @TaskAction
    void immortalizeClean() {
        Path p = ImmortalsConfig.getInstance().extensions.krgp.getTtlTargetDirectory().toAbsolutePath();
        if (Files.exists(p)) {
            
            String expectedSubstring = "knowledge-repo/vocabulary/ontology-static/ontology/individuals/_ANALYSIS/_krgp"
            
            try {
                if (p.toString().contains(expectedSubstring)) {
                    FileUtils.deleteDirectory(p.toFile())
                    
                } else {
                    System.err.println("COWARDLY REFUSING TO DELETE DIRECTORY '" + p.toString() + "' SINCE IT DOES NOT CONTAIN '" + expectedSubstring + "'!")
                }

            } catch (IOException e) {
                throw new RuntimeException(e)
            }
//            FileUtils.deleteDirectory(ImmortalsConfig.getInstance().extensions.krgp.getTtlTargetDirectory());
        }

    }
}
