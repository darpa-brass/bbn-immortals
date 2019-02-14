package mil.darpa.immortals.analysis.adaptationtargets;

/**
 * Created by awellman@bbn.com on 3/29/18.
 */
public class ImmortalsGradlePublishData {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String[] buildToolPublishParameters;

    public ImmortalsGradlePublishData(String groupId, String artifactId, String version, String[] buildToolPublishParameters) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.buildToolPublishParameters = buildToolPublishParameters;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String[] getPublishBuildToolParameters() {
        return buildToolPublishParameters;
    }
    
    public String getPublishCoordinates() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
