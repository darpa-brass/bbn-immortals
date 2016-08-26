package com.securboration.immortals.ontology.dataset;

/**
 * An IMMoRTALS dataset, which contains various graphs
 * 
 * 
 * @author Securboration
 *
 */
public class ImmortalsDataset {
    
    /**
     * A tag for the dataset
     */
    private String datasetTag;
    
    /**
     * The names of the graphs that belong to a dataset. These get merged into a
     * single model that is used for reasoning about a given CP instance.
     */
    private String[] graphNames;

    public String[] getGraphNames() {
        return graphNames;
    }

    public void setGraphNames(String[] graphNames) {
        this.graphNames = graphNames;
    }

    public String getDatasetTag() {
        return datasetTag;
    }

    public void setDatasetTag(String datasetTag) {
        this.datasetTag = datasetTag;
    }

}
