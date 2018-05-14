package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.AppConfigInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 2/20/18.
 */
public interface MavenArtifactInterface extends AppConfigInterface {

    String getMavenRepositoryUrl();

    String getMavenGroupId();

    String getMavenArtifactId();

    String getMavenVersion();

    String getMavenFullDependencyCoordinate();

    @Nullable
    String getMavenOptionalClassifier();

    static String toDependencyCoordinate(@Nonnull MavenArtifactInterface a) {
        if (a.getMavenOptionalClassifier() == null) {
            return a.getMavenGroupId() + ":" + a.getMavenArtifactId() + ":" + a.getMavenVersion();
        } else {
            return a.getMavenGroupId() + ":" + a.getMavenArtifactId() + ":" + a.getMavenVersion() + ":jar:" + a.getMavenOptionalClassifier();

        }
    }
}
