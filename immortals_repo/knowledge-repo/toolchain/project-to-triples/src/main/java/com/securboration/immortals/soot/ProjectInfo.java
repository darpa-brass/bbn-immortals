package com.securboration.immortals.soot;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;

import java.io.File;
import java.util.List;

public class ProjectInfo {

    private String projectUUID;

    private File baseProjectFile;

    private List<File> projectDependencies;

    private BytecodeArtifactCoordinate projectCoordinate;

    public List<File> getProjectDependencies() {
        return projectDependencies;
    }

    public void setProjectDependencies(List<File> projectDependencies) {
        this.projectDependencies = projectDependencies;
    }

    public BytecodeArtifactCoordinate getProjectCoordinate() {
        return projectCoordinate;
    }

    public void setProjectCoordinate(BytecodeArtifactCoordinate projectCoordinate) {
        this.projectCoordinate = projectCoordinate;
    }

    public File getBaseProjectFile() {
        return baseProjectFile;
    }

    public void setBaseProjectFile(File baseProjectFile) {
        this.baseProjectFile = baseProjectFile;
    }

    public String getProjectUUID() {
        return projectUUID;
    }

    public void setProjectUUID(String projectUUID) {
        this.projectUUID = projectUUID;
    }
}
