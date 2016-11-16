package immortals

import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Imports the core definitions file into a project
 */
class CoreDefinitions implements Plugin<Project> {
    void apply(Project project) {
        project.logger.info("Applying CoreDefinitions to " + project.path)

        String IMMORTALS_ROOT = getImmortalsRootPath(project).toAbsolutePath().toString();
        project.apply from: IMMORTALS_ROOT + '/shared/common.gradle'
    }

    private static final Set<String> immortalsRootExpectedFiles = new HashSet<>();

    static {
        immortalsRootExpectedFiles.add("applications");
        immortalsRootExpectedFiles.add("das");
        immortalsRootExpectedFiles.add("dsl");
        immortalsRootExpectedFiles.add("knowledge-repo");
        immortalsRootExpectedFiles.add("shared");
        immortalsRootExpectedFiles.add("build.gradle");
        immortalsRootExpectedFiles.add("settings.gradle");
    }

    private Path getImmortalsRootPath(Project project) {
        Path executionPath = Paths.get(project.rootProject.buildscript.sourceFile.getParent());

        try {
            Path currentPath = executionPath;

            while (currentPath != null) {

                boolean isRoot = true;

                for (String subpath : immortalsRootExpectedFiles) {
                    if (!Files.exists(currentPath.resolve(subpath))) {
                        isRoot = false;
                        break;
                    }
                }
                if (isRoot) {
                    return currentPath.toAbsolutePath();
                } else {
                    currentPath = currentPath.getParent();
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
