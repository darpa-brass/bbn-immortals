package com.securboration.immortals.deployment.parser;

import org.json.JSONObject;

public class DeploymentJson{
    
    private DeploymentJsonRoot root;
    private DeploymentJsonContainment containment;
    private DeploymentJsonBases bases;
    private DeploymentJsonNode[] nodes;
    private DeploymentJsonRelid[] relids;
    private DeploymentJsonMetaSheets metaSheets;
    
    public DeploymentJson(){}
    
    DeploymentJson(JSONObject obj){
        this.root = new DeploymentJsonRoot(obj.getJSONObject("root"));
        this.containment = new DeploymentJsonContainment(obj.getJSONObject("containment"));
        this.bases = new DeploymentJsonBases(obj.getJSONObject("bases"));
        this.nodes = DeploymentJsonNode.parse(obj.getJSONObject("nodes"));
        this.relids = DeploymentUmlIngestor.getRelids(obj.getJSONObject("relids"));
        this.metaSheets = new DeploymentJsonMetaSheets(obj.getJSONObject("metaSheets"));
    }

    public DeploymentJsonRoot getRoot() {
        return root;
    }

    public void setRoot(DeploymentJsonRoot root) {
        this.root = root;
    }

    public DeploymentJsonContainment getContainment() {
        return containment;
    }

    public void setContainment(DeploymentJsonContainment containment) {
        this.containment = containment;
    }

    public DeploymentJsonBases getBases() {
        return bases;
    }

    public void setBases(DeploymentJsonBases bases) {
        this.bases = bases;
    }

    public DeploymentJsonNode[] getNodes() {
        return nodes;
    }

    public void setNodes(DeploymentJsonNode[] nodes) {
        this.nodes = nodes;
    }

    public DeploymentJsonRelid[] getRelids() {
        return relids;
    }

    public void setRelids(DeploymentJsonRelid[] relids) {
        this.relids = relids;
    }

    public DeploymentJsonMetaSheets getMetaSheets() {
        return metaSheets;
    }

    public void setMetaSheets(DeploymentJsonMetaSheets metaSheets) {
        this.metaSheets = metaSheets;
    }
    
}