package com.securboration.immortals.soot;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import soot.SootMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DfuDependency {

    public DfuDependency() {

    }

    public DfuDependency(SootMethod _invokedDfuMethod, BytecodeArtifactCoordinate _dependencyCoordinate,
                         File _dependencyFile) {
        invokedDfuMethod = _invokedDfuMethod;
        dependencyCoordinate = _dependencyCoordinate;
        dependencyFile = _dependencyFile;
    }

    private SootMethod invokedDfuMethod;

    private BytecodeArtifactCoordinate dependencyCoordinate;

    private File dependencyFile;

    private List<DfuDependency> dependenciesOfDfu = new ArrayList<>();

    public SootMethod getInvokedDfuMethod() {
        return invokedDfuMethod;
    }

    public void setInvokedDfuMethod(SootMethod invokedDfuMethod) {
        this.invokedDfuMethod = invokedDfuMethod;
    }

    public BytecodeArtifactCoordinate getDependencyCoordinate() {
        return dependencyCoordinate;
    }

    public void setDependencyCoordinate(BytecodeArtifactCoordinate dependencyCoordinate) {
        this.dependencyCoordinate = dependencyCoordinate;
    }

    public File getDependencyFile() {
        return dependencyFile;
    }

    public void setDependencyFile(File dependencyFile) {
        this.dependencyFile = dependencyFile;
    }

    public List<DfuDependency> getDependenciesOfDfu() {
        return dependenciesOfDfu;
    }

    public void setDependenciesOfDfu(List<DfuDependency> dependenciesOfDfu) {
        this.dependenciesOfDfu = dependenciesOfDfu;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof DfuDependency)) {
            return false;
        }

        DfuDependency dfuDependency = (DfuDependency) o;

        if (this.invokedDfuMethod == null && dfuDependency.getInvokedDfuMethod() != null) {
            return false;
        } else if (dfuDependency.getInvokedDfuMethod() == null && this.invokedDfuMethod != null) {
            return false;
        }

        //TODO don't properly handle recursive dependencies yet, so this criteria won't work for now
        //if (this.getDependenciesOfDfu().size() != dfuDependency.getDependenciesOfDfu().size()) {
            //return false;
        //}

        if (this.dependencyFile == null && dfuDependency.getDependencyFile() != null) {
            return false;
        } else if (dfuDependency.getDependencyFile() == null && this.dependencyFile != null) {
            return false;
        }

        if (this.dependencyCoordinate.getGroupId().equals(dfuDependency.getDependencyCoordinate().getGroupId()) &&
            this.dependencyCoordinate.getArtifactId().equals(dfuDependency.getDependencyCoordinate().getArtifactId()) &&
            this.dependencyCoordinate.getVersion().equals(dfuDependency.getDependencyCoordinate().getVersion())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.dependencyCoordinate.getGroupId().hashCode();
        result = 31 * result + this.dependencyCoordinate.getArtifactId().hashCode();
        result = 31 * result + this.dependencyCoordinate.getVersion().hashCode();
        return result;
    }
}
