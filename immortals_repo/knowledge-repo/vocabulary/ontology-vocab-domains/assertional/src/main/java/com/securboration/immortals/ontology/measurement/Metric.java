//package com.securboration.immortals.ontology.measurement;
//
//import com.securboration.immortals.ontology.core.Resource;
//
///**
// * An instance of a measurement
// * 
// * @author jstaples
// *
// */
//@com.securboration.immortals.ontology.annotations.RdfsComment(
//    "An instance of a measurement  @author jstaples ")
//public class Metric {
//    
//    /**
//     * The type of measurement being made
//     */
//    @com.securboration.immortals.ontology.annotations.RdfsComment(
//        "The type of measurement being made")
//    private MeasurementType measurementType;
//    
//    /**
//     * The value of the measurement (e.g., "10", "false", "PARKED")
//     */
//    @com.securboration.immortals.ontology.annotations.RdfsComment(
//        "The value of the measurement (e.g., \"10\", \"false\", \"PARKED\")")
//    private String value;
//    
//    /**
//     * The unit of measurement (e.g., "km/s", "messages/min", "thread state")
//     */
//    @com.securboration.immortals.ontology.annotations.RdfsComment(
//        "The unit of measurement (e.g., \"km/s\", \"messages/min\"," +
//        " \"thread state\")")
//    private String unit;
//    
//    /**
//     * A tag used to link measurements to expressions operating on them
//     */
//    @com.securboration.immortals.ontology.annotations.RdfsComment(
//        "A tag used to link measurements to expressions operating on them")
//    private String linkId;
//    
//    /**
//     * The resource for which the measurement was captured.  
//     * 
//     * Either this is specified OR an applicableResourceType is specified--not 
//     * both!
//     */
//    @com.securboration.immortals.ontology.annotations.RdfsComment(
//        "The resource for which the measurement was captured.    Either" +
//        " this is specified OR an applicableResourceType is specified--not " +
//        " both!")
//    private Resource applicableResourceInstance;
//    
//    /**
//     * The template types to which this measurement applies
//     * 
//     * Either this is specified OR an applicableResourceInstance is specified--
//     * not both!
//     */
//    @com.securboration.immortals.ontology.annotations.RdfsComment(
//        "The template types to which this measurement applies  Either this" +
//        " is specified OR an applicableResourceInstance is specified-- not" +
//        " both!")
//    private Class<? extends Resource> applicableResourceType;
//    
//    public String getValue() {
//        return value;
//    }
//    
//    public void setValue(String value) {
//        this.value = value;
//    }
//    
//    public String getUnit() {
//        return unit;
//    }
//    
//    public void setUnit(String unit) {
//        this.unit = unit;
//    }
//
//    
//    public MeasurementType getMeasurementType() {
//        return measurementType;
//    }
//
//    
//    public void setMeasurementType(MeasurementType type) {
//        this.measurementType = type;
//    }
//
//    
//    public Resource getApplicableResourceInstance() {
//        return applicableResourceInstance;
//    }
//
//    
//    public void setApplicableResourceInstance(Resource applicableResourceInstance) {
//        this.applicableResourceInstance = applicableResourceInstance;
//    }
//
//    
//    public Class<? extends Resource> getApplicableResourceType() {
//        return applicableResourceType;
//    }
//
//    
//    public void setApplicableResourceType(
//            Class<? extends Resource> applicableResourceType) {
//        this.applicableResourceType = applicableResourceType;
//    }
//
//    
//    public String getLinkId() {
//        return linkId;
//    }
//
//    
//    public void setLinkId(String linkId) {
//        this.linkId = linkId;
//    }
//
//}
