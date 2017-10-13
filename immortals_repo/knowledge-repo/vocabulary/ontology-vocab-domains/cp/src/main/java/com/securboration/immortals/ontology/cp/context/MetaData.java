package com.securboration.immortals.ontology.cp.context;

/**
 * Created by CharlesEndicott on 6/22/2017.
 */
public class MetaData {
    
    private String origin;
    
    public MetaData (String _origin) {
        switch (_origin) {
            case ".jar":
                origin = OriginsOfGraph.JAR.getOrigin();
                break;
            case ".class":
                origin = OriginsOfGraph.CLASS.getOrigin();
                break;
            case ".ttl":
                origin = OriginsOfGraph.EMBEDDED.getOrigin();
                break;
            default:
                origin = OriginsOfGraph.UNSUPPORTED.getOrigin();
                break;
        }
    }

    public enum OriginsOfGraph {

        JAR("jar"),
        CLASS("class"),
        EMBEDDED("ttl"),
        UNSUPPORTED("?");

        private String origin;

        OriginsOfGraph(String origin) {
            this.origin = origin;
        }

        public String getOrigin() {
            return origin;
        }
    }
    
}
