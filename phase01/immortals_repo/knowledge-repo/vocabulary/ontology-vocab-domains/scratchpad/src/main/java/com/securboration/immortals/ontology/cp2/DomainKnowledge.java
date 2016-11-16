package com.securboration.immortals.ontology.cp2;

import com.securboration.immortals.ontology.connectivity.BandwidthKiloBitsPerSecond;
import com.securboration.immortals.ontology.constraint.ConstraintCriterionType;
import com.securboration.immortals.ontology.constraint.ConstraintImpactType;
import com.securboration.immortals.ontology.constraint.DirectionOfViolationType;
import com.securboration.immortals.ontology.constraint.InvocationCriterionType;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.constraint.PropertyImpactType;
import com.securboration.immortals.ontology.functionality.compression.LossyTransformation;
import com.securboration.immortals.ontology.functionality.dataproperties.MemoryFootprint;
import com.securboration.immortals.ontology.functionality.datatype.Bitmap;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.imagescaling.NumberOfPixels;
import com.securboration.immortals.ontology.functionality.imagescaling.ShrinkImage;
import com.securboration.immortals.ontology.measurement.CodeUnitPointer;
import com.securboration.immortals.ontology.measurement.MeasurementInstance;
import com.securboration.immortals.ontology.measurement.MeasurementProfile;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.AbstractPropertyCriterion;
import com.securboration.immortals.ontology.property.impact.ConstraintViolationCriterion;
import com.securboration.immortals.ontology.property.impact.ConstraintViolationImpact;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.InvocationCriterion;
import com.securboration.immortals.ontology.property.impact.PredictiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.PrescriptiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.PropertyCriterion;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;
import com.securboration.immortals.ontology.property.impact.ProscriptiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.RemediationImpact;
import com.securboration.immortals.ontology.resources.network.NetworkBandwidth;

@Ignore
public class DomainKnowledge {
    
    @ConceptInstance(name="BandwidthConstraint")
    public static class BandwidthConstraint extends ProscriptiveCauseEffectAssertion{
        
        public BandwidthConstraint(){
            
            this.setHumanReadableDescription(
                "The maximum observed bandwidth is 1000kbps"
                );
            
            PropertyCriterion criterion = new PropertyCriterion();{
                criterion.setHumanReadableDescription(
                    "when bandwidth > 1Mbps"
                    );
                criterion.setCriterion(PropertyCriterionType.PROPERTY_GREATER_THAN_EXCLUSIVE);
                criterion.setProperty(getBandwidthConsumption(1000));
            }this.setCriterion(criterion);
            
            ConstraintViolationImpact impact1 = new ConstraintViolationImpact();{
                impact1.setHumanReadableDescription(
                    "hard constraint violation"
                    );
                impact1.setConstraintViolationType(ConstraintImpactType.HARD_CONSTRAINT_VIOLATION);
                impact1.setDirectionOfViolation(DirectionOfViolationType.OVERSHOOT);
                impact1.setImpactedResource(NetworkBandwidth.class);
                impact1.setViolationMessage("exceeded a hard limit on the network bandwidth consumption");
            }
            
            this.setImpact(new ImpactStatement[]{impact1});
            
            this.setApplicableDataType(DataType.class);
        }
        
    }
    
    @ConceptInstance
    public static class BandwidthConsumptionExceededMitigationStrategy extends PrescriptiveCauseEffectAssertion{
        
        public BandwidthConsumptionExceededMitigationStrategy(){
            
            this.setHumanReadableDescription(
                "when bandwidth consumption limit is overshot, one remedy is " +
                "to reduce network bandwidth consumption by reducing the " +
                "memory footprint of the data "
                );
            
            ConstraintViolationCriterion constraint = new ConstraintViolationCriterion();{
                constraint.setHumanReadableDescription(
                    "when BandwidthConstraint is violated (ie when max " +
                    "bandwidth consumption is overshot)"
                    );
                constraint.setConstraint(new BandwidthConstraint());
                constraint.setConstraintCriterion(ConstraintCriterionType.WHEN_HARD_VIOLATED);
            }
            this.setCriterion(constraint);
            
            RemediationImpact impact = new RemediationImpact();{
                impact.setRemediationStrategy(new ImpactOfReducingMemoryFootprintOnBandwidth());
                impact.setHumanReadableDescription(
                    "one mitigation strategy is to reduce the memory " +
                    "footprint, which in turn reduces the bandwidth consumed"
                    );
            }
            this.setImpact(new ImpactStatement[]{impact});
            
            this.setImpact(new ImpactStatement[]{impact});
            
        }
        
    }
        
    
    @ConceptInstance(name="ImpactOfReducedMemoryOnBandwidth")
    public static class ImpactOfReducingMemoryFootprintOnBandwidth extends PredictiveCauseEffectAssertion{
        
        public ImpactOfReducingMemoryFootprintOnBandwidth(){
            
            this.setHumanReadableDescription(
                "Reducing the memory footprint of a datatype reduces the " +
                "bandwidth consumed when sending it across network"
                );
            
            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
                criterion.setHumanReadableDescription(
                    "reducing the memory footprint"
                    );
                criterion.setCriterion(PropertyCriterionType.PROPERTY_DECREASES);
                criterion.setProperty(MemoryFootprint.class);
            }this.setCriterion(criterion);
            
            PropertyImpact impact1 = new PropertyImpact();{
                impact1.setHumanReadableDescription(
                    "decreases the bandwidth consumed"
                    );
                impact1.setImpactOnProperty(PropertyImpactType.PROPERTY_DECREASES);
                impact1.setImpactedProperty(BandwidthKiloBitsPerSecond.class);
            }
            
            this.setImpact(new ImpactStatement[]{impact1});
            
            this.setApplicableDataType(DataType.class);
        }
    }
    
    @ConceptInstance(name="ImpactOfReducedPixelsOnMemory")
    public static class ImpactOfReducingNumPixelsOnMemoryFootprint extends PredictiveCauseEffectAssertion{
        
        public ImpactOfReducingNumPixelsOnMemoryFootprint(){
            
            this.setHumanReadableDescription(
                "Reducing the number of pixels in an image reduces its " +
                "memory footprint"
                );
            
            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
                criterion.setHumanReadableDescription(
                    "reducing the number of pixels"
                    );
                criterion.setCriterion(PropertyCriterionType.PROPERTY_DECREASES);
                criterion.setProperty(NumberOfPixels.class);
            }this.setCriterion(criterion);
            
            PropertyImpact impact1 = new PropertyImpact();{
                impact1.setHumanReadableDescription(
                    "decreases the memory footprint"
                    );
                impact1.setImpactOnProperty(PropertyImpactType.PROPERTY_DECREASES);
                impact1.setImpactedProperty(MemoryFootprint.class);
            }
            
            this.setImpact(new ImpactStatement[]{impact1});
            
            this.setApplicableDataType(Bitmap.class);
        }
    }
    
    @ConceptInstance(name="ImpactOfShrinkerOnNumPixels")
    public static class ImpactOfShrinkingOnNumPixels extends PredictiveCauseEffectAssertion{
        
        public ImpactOfShrinkingOnNumPixels(){
            
            this.setHumanReadableDescription(
                "Shrinking an image lossily reduces the number of pixels " +
                "it contains"
                );
            
            InvocationCriterion criterion = new InvocationCriterion();{
                criterion.setHumanReadableDescription(
                    "after invoking the ShrinkImage aspect of the " +
                    "ImageResizer functional spec"
                    );
                criterion.setCriterion(InvocationCriterionType.AFTER_INVOKING);
                criterion.setInvokedAspect(ShrinkImage.class);
            }this.setCriterion(criterion);
            
            PropertyImpact impact1 = new PropertyImpact();{
                impact1.setHumanReadableDescription(
                    "decreases the # of pixels in the image"
                    );
                impact1.setImpactOnProperty(PropertyImpactType.PROPERTY_DECREASES);
                impact1.setImpactedProperty(NumberOfPixels.class);
            }
            
            PropertyImpact impact2 = new PropertyImpact();{
                impact2.setHumanReadableDescription(
                    "at the cost of irreversible data loss in the image"
                    );
                impact2.setImpactOnProperty(PropertyImpactType.ADDS);
                impact2.setImpactedProperty(LossyTransformation.class);
            }
            
            this.setImpact(new ImpactStatement[]{impact1,impact2});
            
            this.setApplicableDataType(Bitmap.class);
        }
    }
    
    
    private static NumberOfPixels getNumPixels(int x,int y){
        NumberOfPixels n = new NumberOfPixels();
        
        n.setWidthPixels(x);
        n.setHeightPixels(y);
        n.setTotalPixels(x*y);
        
        return n;
    }
    
    private static MemoryFootprint getMemoryFootprint(int bytes){
        MemoryFootprint m = new MemoryFootprint();
        
        m.setSizeInBytes(bytes);
        
        return m;
    }
    
    private static BandwidthKiloBitsPerSecond getBandwidthConsumption(
            int bytesPerSecond
            ){
        BandwidthKiloBitsPerSecond c = new BandwidthKiloBitsPerSecond();
        
        c.setKiloBytesPerSecond(bytesPerSecond);
        
        return c;
    }
    
//    @Ignore
//    private static class Pointers{
//        private static final DfuPointer imageReaderDfu = new DfuPointer();static{
//            imageReaderDfu.setClassName(
//                "com.bbn.immortals.ImageReader"
//                );
//            imageReaderDfu.setMethodName("getImage(File imageFile)");
//            imageReaderDfu.setRelevantFunctionalAspect(AspectReadImage.class);
//            imageReaderDfu.setRelevantFunctionality(ImageFileIO.class);
//        }
//        
//        private static final CodeUnitPointer networkSender = new CodeUnitPointer();static{
//            networkSender.setClassName("com.bbn.immortals.ClientImpl");
//            networkSender.setMethodName("sendSaData(byte[])");
//        }
//    }
//    
//    @ConceptInstance(name="NetworkSenderMeasurement1")
//    public static class NetworkSenderMeasurement extends MeasurementProfile{
//        
//        public NetworkSenderMeasurement(){
//            super();
//            
//            getProfile(
//                this,
//                Pointers.networkSender,
//                getMeasurementInstance(
//                    getBandwidthConsumption(256*1024)
//                    )
//                );
//        }
//    }
//    
//    @ConceptInstance(name="ImageReaderMeasurement1")
//    public static class ImageReaderMeasurement1 extends MeasurementProfile{
//        
//        public ImageReaderMeasurement1(){
//            super();
//            
//            getProfile(
//                this,
//                Pointers.imageReaderDfu,
//                getMeasurementInstance(
//                    getMemoryFootprint(256),
//                    "input"
//                    ),
//                getMeasurementInstance(
//                    getMemoryFootprint(128),
//                    "output"
//                    ),
//                getMeasurementInstance(
//                    getNumPixels(8,8)
//                    )
//                );
//        }
//    }
//    
//    @ConceptInstance(name="ImageReaderMeasurement2")
//    public static class ImageReaderMeasurement2 extends MeasurementProfile{
//        
//        public ImageReaderMeasurement2(){
//            super();
//            
//            getProfile(
//                this,
//                Pointers.imageReaderDfu,
//                getMeasurementInstance(
//                    getMemoryFootprint(1024),
//                    "input"
//                    ),
//                getMeasurementInstance(
//                    getMemoryFootprint(512),
//                    "output"
//                    ),
//                getMeasurementInstance(
//                    getNumPixels(16,16)
//                    )
//                );
//        }
//    }
    
    
    private static MeasurementProfile getProfile(
            MeasurementProfile profile,
            CodeUnitPointer codeUnit,
            MeasurementInstance...measurements
            ){
        profile.setCodeUnit(codeUnit);
        profile.setMeasurement(measurements);
        
        return profile;
    }
    
    private static MeasurementInstance getMeasurementInstance(
            Property measuredProperty
            ){
        return getMeasurementInstance(measuredProperty,null);
    }
    
    private static MeasurementInstance getMeasurementInstance(
            Property measuredProperty,
            String qualifier
            ){
        MeasurementInstance m = new MeasurementInstance();
        
        m.setQualifier(qualifier);
        m.setMeasuredValue(measuredProperty);
        
        return m;
    }
    
}
