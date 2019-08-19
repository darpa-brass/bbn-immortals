package com.securboration.test.xsdmut;

public class XsdMutatorConfig extends ConfigurableTypeBase {
    
    @ConfigurationProperty(
        desc = "Path to an XSD to mutate.  Should reside in a flat directory containing only XSD content."
        )
    private String inputXsd;
    
    @ConfigurationProperty(
        desc = "Path to a directory to dump output into.  Defaults to ./out"
        )
    private String outputDir = "./out";
    
    @ConfigurationProperty(
        desc="Iff specified, this seed value will be used.  Otherwise, a random seed will be used."
        )
    private Long rngSeed = null;//seed for RNG
    
    
    @ConfigurationProperty(
        desc="The number of sequence element shuffles to perform (default is 10)."
        )
    private int numSequenceShuffles = 10;
    
    @ConfigurationProperty(
        desc="The number of sequence element deletions to perform (default is 10)."
        )
    private int numSequenceElementDeletions = 10;
    
    @ConfigurationProperty(
        desc="The number of sequence element renames to perform (default is 10)."
        )
    private int numSequenceElementRenames = 10;
    
    @ConfigurationProperty(
        desc="The number of type renames to perform (default is 10)."
        )
    private int numTypeRenames = 10;
    
    @ConfigurationProperty(
        desc="The number of sequence element type changes to perform (default is 10)."
        )
    private int numElementTypeChanges = 10;
    
    @ConfigurationProperty(
        desc="The number of min/max occurs changes to perform (default is 10)."
        )
    private int numMinMaxOccursChanges = 10;
    
    @ConfigurationProperty(
        desc="The number of enumeration element deletions to perform (default is 10)."
        )
    private int numEnumDeletions = 10;
    
    @ConfigurationProperty(
        desc="The number of mutation iterations to perform (default is 1)."
        )
    private int numIterations = 1;
    
    
    private int maxRenamePrefixSize = 2;//max # of random chars to prepend to a mutated name
    private double renameCharacterMutationRate = 0.2;//rate of mutation for original name chars
    private int maxRenameSuffixSize = 10;//max # of random chars to append to a mutated name
    
    
    
    public XsdMutatorConfig(){
        
    }

    
    public String getInputXsd() {
        return inputXsd;
    }

    
    public void setInputXsd(String inputXsd) {
        this.inputXsd = inputXsd;
    }

    
    public String getOutputDir() {
        return outputDir;
    }

    
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    
    public Long getRngSeed() {
        return rngSeed;
    }

    
    public void setRngSeed(Long rngSeed) {
        this.rngSeed = rngSeed;
    }

    
    public int getNumSequenceShuffles() {
        return numSequenceShuffles;
    }

    
    public void setNumSequenceShuffles(int numSequenceShuffles) {
        this.numSequenceShuffles = numSequenceShuffles;
    }

    
    public int getNumSequenceElementDeletions() {
        return numSequenceElementDeletions;
    }

    
    public void setNumSequenceElementDeletions(int numSequenceElementDeletions) {
        this.numSequenceElementDeletions = numSequenceElementDeletions;
    }

    
    public int getNumSequenceElementRenames() {
        return numSequenceElementRenames;
    }

    
    public void setNumSequenceElementRenames(int numSequenceElementRenames) {
        this.numSequenceElementRenames = numSequenceElementRenames;
    }

    
    public int getNumTypeRenames() {
        return numTypeRenames;
    }

    
    public void setNumTypeRenames(int numTypeRenames) {
        this.numTypeRenames = numTypeRenames;
    }

    
    public int getNumElementTypeChanges() {
        return numElementTypeChanges;
    }

    
    public void setNumElementTypeChanges(int numElementTypeChanges) {
        this.numElementTypeChanges = numElementTypeChanges;
    }

    
    public int getNumMinMaxOccursChanges() {
        return numMinMaxOccursChanges;
    }

    
    public void setNumMinMaxOccursChanges(int numMinMaxOccursChanges) {
        this.numMinMaxOccursChanges = numMinMaxOccursChanges;
    }

    
    public int getNumEnumDeletions() {
        return numEnumDeletions;
    }

    
    public void setNumEnumDeletions(int numEnumDeletions) {
        this.numEnumDeletions = numEnumDeletions;
    }

    
    public int getMaxRenamePrefixSize() {
        return maxRenamePrefixSize;
    }

    
    public void setMaxRenamePrefixSize(int maxRenamePrefixSize) {
        this.maxRenamePrefixSize = maxRenamePrefixSize;
    }

    
    public double getRenameCharacterMutationRate() {
        return renameCharacterMutationRate;
    }

    
    public void setRenameCharacterMutationRate(double renameCharacterMutationRate) {
        this.renameCharacterMutationRate = renameCharacterMutationRate;
    }

    
    public int getMaxRenameSuffixSize() {
        return maxRenameSuffixSize;
    }

    
    public void setMaxRenameSuffixSize(int maxRenameSuffixSize) {
        this.maxRenameSuffixSize = maxRenameSuffixSize;
    }

    
    public int getNumIterations() {
        return numIterations;
    }

    
    public void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }
    
    
    

}
