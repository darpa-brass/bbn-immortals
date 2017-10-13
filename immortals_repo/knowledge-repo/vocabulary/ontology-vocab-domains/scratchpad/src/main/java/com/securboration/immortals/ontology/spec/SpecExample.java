package com.securboration.immortals.ontology.spec;

import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.bytecode.ClasspathElement;
import com.securboration.immortals.ontology.bytecode.JarArtifact;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.Cipher;
import com.securboration.immortals.ontology.pattern.spec.AbstractUsageParadigm;
import com.securboration.immortals.ontology.pattern.spec.LibraryFunctionalAspectSpec;
import com.securboration.immortals.ontology.pattern.spec.ParadigmComponent;
import com.securboration.immortals.ontology.pattern.spec.SpecComponent;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;

@Ignore
public class SpecExample {
    
//    private static FunctionalAspect aspect = getCipherEncryptAspect();
//    
//    private static FunctionalAspect getCipherEncryptAspect(){
//        FunctionalAspect f = new FunctionalAspect();
//        
//        f.setAspectId("cipher-encrypt");
//        f.setAspectProperties(new Property[]{});
//        f.setAspectSpecificResourceDependencies(new Class[]{});
//        f.setInputs(new Input[]{});
//        f.setOutputs(new Output[]{});
//        
//        return f;
//    }
    
    @ConceptInstance
    public static class SpecExample1 extends LibraryFunctionalAspectSpec{
        public SpecExample1(){
            this.setDurableId("SpecExample-1");
            this.setFunctionality(Cipher.class);
            this.setAspect(AspectCipherEncrypt.class);
            this.setLibraryCoordinateTag(
                "org.third.party:lib-cipher:1.2.3"
                );
            this.setComponent(getExample1Spec());
            this.setUsageParadigm(new ParadigmExample());
        }
    }
    
    @ConceptInstance
    public static class ParadigmExample extends AbstractUsageParadigm{
        
        public ParadigmExample(){
            this.setDurableId("ParadigmExample-declare-init-doWork-cleanup");
            this.setComponent(new ParadigmComponent[]{
                    new Declare(),
                    new Init(),
                    new DoWork(),
                    new Cleanup()
            });
        }
        
    }
    
    @ConceptInstance
    public static class Declare extends ParadigmComponent{
        public Declare(){
            this.setDurableId("declare");
            this.setMultiplicityOperator("1");
            this.setOrdering(0);
        }
    }
    
    @ConceptInstance
    public static class Init extends ParadigmComponent{
        public Init(){
            this.setDurableId("init");
            this.setMultiplicityOperator("1");
            this.setOrdering(1);
        }
    }
    
    @ConceptInstance
    public static class DoWork extends ParadigmComponent{
        public DoWork(){
            this.setDurableId("doWork");
            this.setMultiplicityOperator("[0,inf]");
            this.setOrdering(2);
        }
    }
    
    @ConceptInstance
    public static class Cleanup extends ParadigmComponent{
        public Cleanup(){
            this.setDurableId("cleanup");
            this.setMultiplicityOperator("1");
            this.setOrdering(3);
        }
    }
    
    
    
    private static SpecComponent[] getExample1Spec(){
        List<SpecComponent> components = new ArrayList<>();
        
        components.add(new SpecComponent(){{
            this.setDurableId("example1-declare");
            this.setSpec("{com.securboration.example.Cipher cipher$2215879;}");
            this.setAbstractComponentLinkage(new Declare());
        }});
        
        components.add(new SpecComponent(){{
            this.setDurableId("example1-init");
            this.setSpec("cipher$2215879 = new com.securboration.example.Cipher();cipher$2215879.setMode(\"ENCRYPT\");");
            this.setAbstractComponentLinkage(new Init());
        }});
        
        components.add(new SpecComponent(){{
            this.setDurableId("example1-doWork");
            this.setSpec("output$127997 = cipher$2215879.encrypt(input$0876887);");
            this.setAbstractComponentLinkage(new DoWork());
        }});
        
        components.add(new SpecComponent(){{
            this.setDurableId("example1-cleanup");
            this.setSpec("cipher$2215879.cleanup();");
            this.setAbstractComponentLinkage(new Cleanup());
        }});
        
        return components.toArray(new SpecComponent[]{});
    }
    
    private static SpecComponent[] getExample2Spec(){
        List<SpecComponent> components = new ArrayList<>();
        
//        {
//            SpecComponent c = new SpecComponent();
//            c.setDurableId("example1-init");
//            c.setAbstractComponentLinkage(abstractComponentLinkage);
//        }
        
//      this.setSpec(
//      "instance = constructor(key)," +
//      "instance.encryptMode()," +
//      "byte[] chunk = put(byte[] dataChunk)*," +
//      "byte[] chunk = finish()"
//      );
        
        return components.toArray(new SpecComponent[]{});
    }
    
    @ConceptInstance
    public static class SpecExample2 extends LibraryFunctionalAspectSpec{
        public SpecExample2(){
            this.setDurableId("SpecExample-2 (incomplete)");
            this.setFunctionality(Cipher.class);
            this.setAspect(AspectCipherEncrypt.class);
            this.setLibraryCoordinateTag(
                "com.securboration:immortals-examples-cipher:0.0.1"
                );
            this.setComponent(getExample2Spec());

        }
    }
    
    @ConceptInstance
    public static class CipherLibrary1 extends JarArtifact{
        
        public CipherLibrary1(){
            this.setHash("hashValueGoesHere");
            this.setName(this.getClass().getSimpleName());
            this.setJarContents(new ClasspathElement[]{});
            this.setCoordinate(acquire(
                "org.third.party","lib-cipher","1.2.3"
                ));
            this.setBinaryForm(new byte[]{});
        }
    }
    
    @ConceptInstance
    public static class CipherLibrary2 extends JarArtifact{
        
        public CipherLibrary2(){
            this.setHash("hashValueGoesHere");
            this.setName(this.getClass().getSimpleName());
            this.setJarContents(new ClasspathElement[]{});
            this.setCoordinate(acquire(
                "com.securboration","immortals-examples-cipher","0.0.1"
                ));
            this.setBinaryForm(new byte[]{});
        }
    }
    
    private static BytecodeArtifactCoordinate acquire(
            String groupId,
            String artifactId,
            String version
            ){
        BytecodeArtifactCoordinate c = new BytecodeArtifactCoordinate();
        c.setGroupId(groupId);
        c.setArtifactId(artifactId);
        c.setVersion(version);
        c.setCoordinateTag(groupId + ":" + artifactId + ":" + version);
        
        return c;
    }

}
