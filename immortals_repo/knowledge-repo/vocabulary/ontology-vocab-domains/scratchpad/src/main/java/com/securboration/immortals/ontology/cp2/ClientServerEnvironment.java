package com.securboration.immortals.ontology.cp2;

import com.securboration.immortals.ontology.analysis.DataflowAnalysisFrame;
import com.securboration.immortals.ontology.analysis.DataflowEdge;
import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.analysis.InterMethodDataflowNode;
import com.securboration.immortals.ontology.constraint.ConstraintCriterionType;
import com.securboration.immortals.ontology.constraint.ConstraintImpactType;
import com.securboration.immortals.ontology.constraint.DirectionOfViolationType;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.constraint.PropertyImpactType;
import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.cp.jvm.AndroidRuntimeEnvironment;
import com.securboration.immortals.ontology.cp.jvm.JavaRuntimeEnvironment;
import com.securboration.immortals.ontology.functionality.ConfidentialProperty;
import com.securboration.immortals.ontology.functionality.alg.encryption.aes.AES_128;
import com.securboration.immortals.ontology.functionality.compression.AspectDeflate;
import com.securboration.immortals.ontology.functionality.compression.AspectInflate;
import com.securboration.immortals.ontology.functionality.compression.Compressor;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.dataproperties.CompressedLossless;
import com.securboration.immortals.ontology.functionality.dataproperties.Encrypted;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.imagecapture.AspectWriteImage;
import com.securboration.immortals.ontology.functionality.imagecapture.ImageFileIO;
import com.securboration.immortals.ontology.functionality.imagescaling.NumberOfPixels;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.AbstractDataflowBindingSite;
import com.securboration.immortals.ontology.property.impact.AbstractPropertyCriterion;
import com.securboration.immortals.ontology.property.impact.ConstraintViolationCriterion;
import com.securboration.immortals.ontology.property.impact.ConstraintViolationImpact;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.PredictiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.PrescriptiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;
import com.securboration.immortals.ontology.property.impact.ProscriptiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.RemediationImpact;
import com.securboration.immortals.ontology.resources.FileSystemResource;
import com.securboration.immortals.ontology.resources.MobileAndroidDevice;
import com.securboration.immortals.ontology.resources.PlatformResource;
import com.securboration.immortals.ontology.resources.Server;

/**
 * An example client/server environment. There are three mobile android devices that have data
 * flows to a Marti server. Additionally, there is a constraint included in this environment, specifying
 * that the data transmitted must be confidential. Two of the three data flows between client and server lack
 * the ability to do this, and will violate the constraint as a result. The third has the encrypted property, 
 * satisfying the constraint by providing a mechanism by which the data can be kept confidential.
 */
@Ignore
public class ClientServerEnvironment {
    
    public static MartiServer martiServer = new MartiServer();
    public static ClientDevice1 clientDevice1 = new ClientDevice1();
    public static ClientDevice2 clientDevice2 = new ClientDevice2();
    public static ClientDevice3 clientDevice3 = new ClientDevice3();
    public static FileSystem1 fileSystem1 = new FileSystem1();
    public static FileSystem2 fileSystem2 = new FileSystem2();
    public static DataflowNode4 serverNode = new DataflowNode4();
    
    
    @ConceptInstance(name = "DataSafetyConstraint")
    public static class DataSafetyConstraint extends ProscriptiveCauseEffectAssertion {
        public DataSafetyConstraint() {

            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
                criterion.setProperty(ConfidentialProperty.class);
                criterion.setCriterion(PropertyCriterionType.PROPERTY_ABSENT);
                criterion.setHumanReadableDescription("This criterion specifies a situation where a confidential property is absent.");
            }

            ConstraintViolationImpact impact = new ConstraintViolationImpact();{
                impact.setConstraintViolationType(ConstraintImpactType.HARD_CONSTRAINT_VIOLATION);
                impact.setDirectionOfViolation(DirectionOfViolationType.UNDERSHOOT);
            }

            AbstractDataflowBindingSite abstractDataflowBindingSite = new AbstractDataflowBindingSite();{
                abstractDataflowBindingSite.setSrc(MobileAndroidDevice.class);
                abstractDataflowBindingSite.setDest(Server.class);
                abstractDataflowBindingSite.setHumanReadableDescription("Any data between an android mobile device and server");
            }

            this.setCriterion(criterion);
            this.setImpact(new ImpactStatement[] {impact});
            this.setApplicableDataType(DataType.class);
            this.setHumanReadableDescription("All data being transmitted between MobileAndroidDevices and FileSystemResources " +
                    "must be confidential");
        }
    }
    
    @ConceptInstance(name = "ConfidentialDataImplementationStrategy")
    public static class ConfidentialDataImplementationStrategy extends PrescriptiveCauseEffectAssertion {
        public ConfidentialDataImplementationStrategy() {
            ConstraintViolationCriterion criterion = new ConstraintViolationCriterion();{
                criterion.setConstraint(new ClientServerEnvironment.DataSafetyConstraint());
                criterion.setTriggeringConstraintCriterion(ConstraintCriterionType.WHEN_HARD_VIOLATED);
            }
            this.setCriterion(criterion);

            RemediationImpact impact = new RemediationImpact();{
                impact.setRemediationStrategy(new ClientServerEnvironment.ImpactOfEncryptingData());
            }
            this.setImpact(new ImpactStatement[] {impact});
            this.setHumanReadableDescription("When the DataSafetyConstraint is \"hard\" violated, this strategy can " +
                    "mitigate the violation, while at the same time introducing ImpactOfEncryptingData.");
        }
    }

    @ConceptInstance(name = "ImpactOfEncryptingData")
    public static class ImpactOfEncryptingData extends PredictiveCauseEffectAssertion {
        public ImpactOfEncryptingData() {
            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
                criterion.setProperty(Encrypted.class);
                criterion.setCriterion(PropertyCriterionType.PROPERTY_ADDED);
            }
            this.setCriterion(criterion);

            PropertyImpact impact = new PropertyImpact();{
                impact.setImpactedProperty(ConfidentialProperty.class);
                impact.setImpactOnProperty(PropertyImpactType.ADDS);
            }
            this.setImpact(new ImpactStatement[] {impact});
        }
    }

    @ConceptInstance
    public static class ATAKMartiFunctionalitySpecDeflate extends FunctionalitySpec {
        public ATAKMartiFunctionalitySpecDeflate() {
            this.setFunctionalityPerformed(Compressor.class);
            this.setFunctionalityProvided(AspectDeflate.class);
        }
    }

    @ConceptInstance
    public static class ATAKMartiFunctionalitySpecInflate extends FunctionalitySpec {
        public ATAKMartiFunctionalitySpecInflate() {
            this.setFunctionalityPerformed(Compressor.class);
            this.setFunctionalityProvided(AspectInflate.class);
        }
    }

    @ConceptInstance
    public static class ATAKMartiFunctionalitySpecWriteImage extends FunctionalitySpec {
        public ATAKMartiFunctionalitySpecWriteImage() {
            this.setFunctionalityPerformed(ImageFileIO.class);
            this.setFunctionalityProvided(AspectWriteImage.class);
        }
    }
    
    @ConceptInstance
    public static class ATAKSoftware extends Analysis.Atak.AtakSoftware {
        public ATAKSoftware() {
            
        }
    }
    
    @ConceptInstance
    public static class MartiSoftware extends Analysis.Marti.MartiSoftware {
        public MartiSoftware() {
            
        }
    }
    
    @ConceptInstance
    public static class ClientDevice1 extends MobileAndroidDevice {
        public ClientDevice1() {
            this.setResources(
                new PlatformResource[]{
                        art("Oreo 8.0",false)
                    }
                );
        }
        
        @Override
        public void setResources(PlatformResource[] resources){
            super.setResources(resources);
        }
    }

    @ConceptInstance
    public static class ClientDevice2 extends MobileAndroidDevice {
        public ClientDevice2() {
            this.setResources(
                new PlatformResource[]{
                        art("Oreo 8.1",true)
                    }
                );
        }
    }

    @ConceptInstance
    public static class ClientDevice3 extends MobileAndroidDevice {
        public ClientDevice3() {
            this.setResources(
                new PlatformResource[]{
                        art("Nougat 7.1.2",false),
                        art("Nougat 7.1.3",false)
                    }
                );
        }
    }
    
    @ConceptInstance
    public static class MartiServer extends Server {
        public MartiServer() {
            this.setResources(
                new PlatformResource[]{
                        jvm("usr/local/jdk1.8.1_71","Oracle Java 8u171",false)
                    }
                );
        }
    }
    
    @ConceptInstance
    public static class FileSystem1 extends FileSystemResource {
        public FileSystem1() {
        }
    }
    
    @ConceptInstance
    public static class FileSystem2 extends FileSystemResource {
        public FileSystem2() {
        }
    }
    
    @ConceptInstance
    public static class DataflowNode1 extends InterMethodDataflowNode {
        public DataflowNode1() {
            this.setResourceTemplate(clientDevice1);
            this.setJavaClassName("com.sec.Client1");
            this.setJavaMethodName("M1");
            this.setJavaMethodPointer("12345");
        }
    }

    @ConceptInstance
    public static class DataflowNode2 extends InterMethodDataflowNode {
        public DataflowNode2() {
            this.setResourceTemplate(clientDevice2);
            this.setJavaClassName("com.sec.Client2");
            this.setJavaMethodName("M2");
            this.setJavaMethodPointer("67890");
        }
    }

    @ConceptInstance
    public static class DataflowNode3 extends InterMethodDataflowNode {
        public DataflowNode3() {
            this.setResourceTemplate(clientDevice3);
            this.setJavaClassName("com.sec.Client3");
            this.setJavaMethodName("M3");
            this.setJavaMethodPointer("13579");
        }
    }

    @ConceptInstance
    public static class DataflowNode4 extends InterMethodDataflowNode {
        public DataflowNode4() {
            this.setResourceTemplate(martiServer);
            this.setJavaClassName("com.sec.Server1");
            this.setJavaMethodName("M4");
            this.setJavaMethodPointer("24680");
        }
    }
    
    @ConceptInstance
    public static class DataflowNode5 extends DataflowNode {
        public DataflowNode5() {
            this.setResourceTemplate(fileSystem1);
        }
    }
    
    @ConceptInstance
    public static class DataflowNode6 extends DataflowNode {
        public DataflowNode6() {
            this.setResourceTemplate(fileSystem2);
        }
    }
    
    @ConceptInstance
    public static class DataflowEdge1 extends DataflowEdge {
        public DataflowEdge1() {
            Property property1 = new CompressedLossless();
            Property property2 = new Compressed();
            this.setEdgeProperties(new Property[] {property1, property2});

            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameDataType(BinaryData.class);
            frame1.setFrameProperties(new Property[] {new Encrypted()});
            DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
            frame2.setAnalysisFrameDataType(BinaryData.class);
            frame2.setFrameProperties(new Property[] {new Compressed()});
            frame1.setAnalysisFrameChild(frame2);
            this.setDataflowAnalysisFrame(frame1);
            
            this.setConsumer(serverNode);
            this.setProducer(new DataflowNode1());
        }
    }

    @ConceptInstance
    public static class DataflowEdge2 extends DataflowEdge {
        public DataflowEdge2() {
            Property property1 = new CompressedLossless();
            Encrypted property2 = new Encrypted();
            property2.setEncryptionAlgorithm(AES_128.class);
            this.setEdgeProperties(new Property[] {property1, property2});

            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameDataType(BinaryData.class);
            frame1.setFrameProperties(new Property[] {new CompressedLossless()});
            DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
            frame2.setAnalysisFrameDataType(BinaryData.class);
            frame2.setFrameProperties(new Property[] {new Compressed()});
            frame1.setAnalysisFrameChild(frame2);
            this.setDataflowAnalysisFrame(frame1);

            this.setConsumer(serverNode);
            this.setProducer(new DataflowNode2());
        }
    }

    @ConceptInstance
    public static class DataflowEdge3 extends DataflowEdge {
        public DataflowEdge3() {
            Property property1 = new Compressed();
            Property property2 = new NumberOfPixels();
            this.setEdgeProperties(new Property[] {property1, property2});

            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameDataType(BinaryData.class);
            frame1.setFrameProperties(new Property[] {new Compressed()});
            DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
            frame2.setAnalysisFrameDataType(BinaryData.class);
            frame2.setFrameProperties(new Property[] {new NumberOfPixels()});
            frame1.setAnalysisFrameChild(frame2);
            this.setDataflowAnalysisFrame(frame1);

            this.setConsumer(serverNode);
            this.setProducer(new DataflowNode3());
        }
    }

    @ConceptInstance
    public static class DataflowEdge4 extends DataflowEdge {
        public DataflowEdge4() {
            Property property1 = new Compressed();
            Property property2 = new Encrypted();
            this.setEdgeProperties(new Property[] {property1, property2});

            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameDataType(BinaryData.class);
            frame1.setFrameProperties(new Property[] {new Compressed()});
            DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
            frame2.setAnalysisFrameDataType(BinaryData.class);
            frame2.setFrameProperties(new Property[] {new NumberOfPixels()});
            frame1.setAnalysisFrameChild(frame2);
            this.setDataflowAnalysisFrame(frame1);

            this.setConsumer(new DataflowNode5());
            this.setProducer(new DataflowNode1());
        }
    }

    @ConceptInstance
    public static class DataflowEdge5 extends DataflowEdge {
        public DataflowEdge5() {
            Property property1 = new Compressed();
            Property property2 = new CompressedLossless();
            this.setEdgeProperties(new Property[] {property1, property2});

            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameDataType(BinaryData.class);
            frame1.setFrameProperties(new Property[] {new Compressed()});
            DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
            frame2.setAnalysisFrameDataType(BinaryData.class);
            frame2.setFrameProperties(new Property[] {new NumberOfPixels()});
            frame1.setAnalysisFrameChild(frame2);
            this.setDataflowAnalysisFrame(frame1);

            this.setConsumer(new DataflowNode6());
            this.setProducer(new DataflowNode3());
        }
    }
    
    
    
    private static AndroidRuntimeEnvironment art(
            String desc,
            boolean unlimitedCrypto
            ){
        AndroidRuntimeEnvironment art = new AndroidRuntimeEnvironment();
        
        art.setHumanReadableDescription(desc);
        art.setUnlimitedCryptoStrengh(unlimitedCrypto);
        
        return art;
    }
    
    private static JavaRuntimeEnvironment jvm(
            String path, 
            String desc,
            boolean unlimitedCrypto
            ){
        JavaRuntimeEnvironment jvm = new JavaRuntimeEnvironment();
        
        jvm.setHumanReadableDescription(desc);
        jvm.setJavaHomePath(path);
        jvm.setUnlimitedCryptoStrengh(unlimitedCrypto);
        
        return jvm;
    }
}
