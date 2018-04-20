package com.securboration.immortals.ontology.cp2;

import com.securboration.immortals.ontology.analysis.DataflowEdge;
import com.securboration.immortals.ontology.analysis.DataflowGraph;
import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.analysis.DiskIODataflowNode;
import com.securboration.immortals.ontology.analysis.FunctionalAspectInvocationDataflowNode;
import com.securboration.immortals.ontology.analysis.MethodInvocationDataflowNode;
import com.securboration.immortals.ontology.analysis.NetworkIODataflowNode;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.functionality.imagecapture.AspectReadImage;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.resources.AndroidPhone;
import com.securboration.immortals.ontology.resources.CompiledSoftware;
import com.securboration.immortals.ontology.resources.FileSystemResource;
import com.securboration.immortals.ontology.resources.NetworkResource;
import com.securboration.immortals.ontology.resources.PlatformResource;
import com.securboration.immortals.ontology.resources.Server;

@Ignore
public class Analysis {
    
    @ConceptInstance
    public static class DataflowGraph1 extends DataflowGraph{
        public DataflowGraph1(){
            this.setEdges(edges());
            
            this.setHumanReadableDescription(
                "Describes a dataflow in which ATAK reads an image from a " +
                "local device file and transmits it to a remote MARTI " +
                "server process"
                );
        }
    }
    
    private static DataflowEdge[] edges(){
        return new DataflowEdge[]{
                getEdge(
                    new AtakImageSourceNode(),
                    new AtakReadImageNode(),
                    Image.class,
                    new Atak.AtakPhoneFileSystem(),
                    "ATAK reads an image via local disk IO.  The image is " +
                    "passed to a ReadImage functional aspect."
                    ),
                getEdge(
                    new AtakReadImageNode(),
                    new AtakSendSaDataNode(),
                    Image.class,
                    new Atak.AtakSoftware(),
                    "ATAK passes the image to a non-DFU method that " +
                    "transmits the image to MARTI"
                    ),
                getEdge(
                    new AtakSendSaDataNode(),
                    new RemoteMartiSoftwareNode(),
                    Image.class,
                    new Shared.SaNetwork(),
                    "ATAK transmits the image to a MARTI server via network IO"
                    ),
        };//TODO
    }
    
    private static DataflowEdge getEdge(
            DataflowNode producer, 
            DataflowNode consumer,
            Class<? extends DataType> datatype,
            Resource communicationChannelTemplate,
            final String description
            ){
        DataflowEdge e = new DataflowEdge();
        
        e.setProducer(producer);
        e.setConsumer(consumer);
        e.setDataTypeCommunicated(datatype);
        e.setCommunicationChannelTemplate(communicationChannelTemplate);
        e.setHumanReadableDescription(description);
        
        return e;
    }
    
    @ConceptInstance
    public static class AtakImageSourceNode extends DiskIODataflowNode{
        public AtakImageSourceNode(){
            this.setContextTemplate(new Atak.AtakPhone());
            this.setResourceTemplate(new Atak.AtakPhoneFileSystem());
        }
    }
    
    @ConceptInstance
    public static class RemoteMartiSoftwareNode extends NetworkIODataflowNode{
        public RemoteMartiSoftwareNode(){
            this.setContextTemplate(new Marti.MartiServer());
            this.setResourceTemplate(new Marti.MartiSoftware());
        }
    }
    
    @ConceptInstance
    public static class AtakReadImageNode extends FunctionalAspectInvocationDataflowNode{
        public AtakReadImageNode(){
            this.setJavaClassName("com.bbn.ImageGetter");
            this.setJavaMethodName("readImage(File f)");
            this.setAspectImplemented(new AspectReadImage());
            
            this.setContextTemplate(new Atak.AtakPhone());
            this.setResourceTemplate(new Atak.AtakPhoneFileSystem());
        }
    }
    
    @ConceptInstance
    public static class AtakSendSaDataNode extends MethodInvocationDataflowNode{
        public AtakSendSaDataNode(){
            this.setJavaClassName("com.bbn.SaDataTransmitter");
            this.setJavaMethodName("writeSaDataToNetwork(byte[] data)");
            
            this.setContextTemplate(new Atak.AtakPhone());
            this.setResourceTemplate(new Atak.AtakSoftware());
        }
    }
    
    @Ignore
    public static class Atak{
    
        @ConceptInstance
        public static class AtakPhone extends AndroidPhone{
            
            public AtakPhone(){
                this.setHumanReadableDescription(
                    "A phone with a file system connected to a network and running ATAK.");
                
                this.setResources(new PlatformResource[]{
                        new AtakPhoneFileSystem(),
                        new Shared.SaNetwork(),
                        new AtakSoftware()
                        });
            }
        }
        
        @ConceptInstance
        public static class AtakSoftware extends CompiledSoftware{
            public AtakSoftware(){
                this.setApplicationName("ATAK");
                this.setSoftwareCoordinate("com.bbn.ataklite");
                this.setVersionControlUrl("https://dsl-external.bbn.com/svn/immortals/trunk/applications/client/ATAKLite");
                
                this.setHumanReadableDescription("An instance of the ATAK project");
            }
        }
        
        @ConceptInstance
        public static class AtakPhoneFileSystem extends FileSystemResource{
            
            public AtakPhoneFileSystem(){
                this.setHumanReadableDescription("a file system");
            }
            
        }
    }
    
    @Ignore
    public static class Shared{

        @ConceptInstance
        public static class SaNetwork extends NetworkResource{
            public SaNetwork(){
                this.setHumanReadableDescription(
                    "a network for communicating SA data");
            }
        }
        
    }
    
    @Ignore
    public static class Marti{
        
        @ConceptInstance
        public static class MartiFileSystem extends FileSystemResource{
            
            public MartiFileSystem(){
                this.setHumanReadableDescription("a file system");
            }
            
        }
        
        @ConceptInstance
        public static class MartiSoftware extends CompiledSoftware{
            public MartiSoftware(){
                this.setApplicationName("MARTI");
                
                this.setSoftwareCoordinate("mil.darpa.immortals:Marti-immortals:2.0-LOCAL");
                this.setVersionControlUrl("https://dsl-external.bbn.com/svn/immortals/trunk/applications/server/Marti");
                
                this.setHumanReadableDescription("An instance of the MARTI project");
            }
        }
        
        @ConceptInstance
        public static class MartiServer extends Server{
            public MartiServer(){
                this.setHumanReadableDescription(
                    "a server running MARTI");
                
                this.setResources(new PlatformResource[]{
                        new Marti.MartiSoftware(),
                        new Marti.MartiFileSystem(),
                        new Shared.SaNetwork()
                });
            }
        }
        
    }
    
}
