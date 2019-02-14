package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.AppConfigInterface;

/**
 * Created by awellman@bbn.com on 2/20/18.
 */
public interface MavenArtifactInterface extends AppConfigInterface {
    String getMavenGroupId();
    String getMavenArtifactId();
    String getMavenArtifactExtension();
}
