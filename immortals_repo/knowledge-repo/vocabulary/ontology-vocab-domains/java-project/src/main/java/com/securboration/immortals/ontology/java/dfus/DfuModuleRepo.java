package com.securboration.immortals.ontology.java.dfus;

import com.securboration.immortals.ontology.dfu.DfuModule;

public class DfuModuleRepo {

    private String pathToRepo;

    private DfuModule[] dfuModules;

    public DfuModule[] getDfuModules() {
        return dfuModules;
    }

    public void setDfuModules(DfuModule[] dfuModules) {
        this.dfuModules = dfuModules;
    }

    public String getPathToRepo() {
        return pathToRepo;
    }

    public void setPathToRepo(String pathToRepo) {
        this.pathToRepo = pathToRepo;
    }
}
