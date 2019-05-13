package com.securboration.immortals.ontology.java.project;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;

public class AnalysisMetrics {

    private int baseVocabTriples;

    private int domainTriples;

    private int bytecodeTriples;

    private int mineTriples;

    private int inferenceTriples;

    private int adaptationTriples;

    private Long bytecodeExec;

    private Long mineExec;

    private Long ingestExec;

    private Long adaptExec;

    private String[] classesModified;

    private String[] methodsModified;

    private String[] newClasses;

    private String[] newMethods;

    private BytecodeArtifactCoordinate[] newDependencies;

    public int getBaseVocabTriples() {
        return baseVocabTriples;
    }

    public void setBaseVocabTriples(int baseVocabTriples) {
        this.baseVocabTriples = baseVocabTriples;
    }

    public int getDomainTriples() {
        return domainTriples;
    }

    public void setDomainTriples(int domainTriples) {
        this.domainTriples = domainTriples;
    }

    public int getBytecodeTriples() {
        return bytecodeTriples;
    }

    public void setBytecodeTriples(int bytecodeTriples) {
        this.bytecodeTriples = bytecodeTriples;
    }

    public int getMineTriples() {
        return mineTriples;
    }

    public void setMineTriples(int mineTriples) {
        this.mineTriples = mineTriples;
    }

    public int getInferenceTriples() {
        return inferenceTriples;
    }

    public void setInferenceTriples(int inferenceTriples) {
        this.inferenceTriples = inferenceTriples;
    }

    public int getAdaptationTriples() {
        return adaptationTriples;
    }

    public void setAdaptationTriples(int adaptationTriples) {
        this.adaptationTriples = adaptationTriples;
    }

    public Long getBytecodeExec() {
        return bytecodeExec;
    }

    public void setBytecodeExec(Long bytecodeExec) {
        this.bytecodeExec = bytecodeExec;
    }

    public Long getMineExec() {
        return mineExec;
    }

    public void setMineExec(Long mineExec) {
        this.mineExec = mineExec;
    }

    public Long getIngestExec() {
        return ingestExec;
    }

    public void setIngestExec(Long ingestExec) {
        this.ingestExec = ingestExec;
    }

    public Long getAdaptExec() {
        return adaptExec;
    }

    public void setAdaptExec(Long adaptExec) {
        this.adaptExec = adaptExec;
    }

    public String[] getClassesModified() {
        return classesModified;
    }

    public void setClassesModified(String[] classesModified) {
        this.classesModified = classesModified;
    }

    public String[] getMethodsModified() {
        return methodsModified;
    }

    public void setMethodsModified(String[] methodsModified) {
        this.methodsModified = methodsModified;
    }

    public String[] getNewClasses() {
        return newClasses;
    }

    public void setNewClasses(String[] newClasses) {
        this.newClasses = newClasses;
    }

    public String[] getNewMethods() {
        return newMethods;
    }

    public void setNewMethods(String[] newMethods) {
        this.newMethods = newMethods;
    }

    public BytecodeArtifactCoordinate[] getNewDependencies() {
        return newDependencies;
    }

    public void setNewDependencies(BytecodeArtifactCoordinate[] newDependencies) {
        this.newDependencies = newDependencies;
    }
}
