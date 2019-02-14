//package com.securboration.immortals.service.api;
//
//import java.util.HashMap;
//import java.util.UUID;
//
//import org.apache.commons.codec.binary.Base64;
//import org.apache.jena.rdf.model.Model;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.securboration.immortals.instantiation.bytecode.AnnotationsToTriples;
//import com.securboration.immortals.instantiation.bytecode.JarIngestor;
//import com.securboration.immortals.instantiation.bytecode.SourceFinder;
//import com.securboration.immortals.instantiation.bytecode.SourceFinderDummy;
//import com.securboration.immortals.instantiation.bytecode.UriMappings;
//import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
//import com.securboration.immortals.o2t.analysis.ObjectToTriples;
//import com.securboration.immortals.o2t.ontology.OntologyHelper;
//import com.securboration.immortals.ontology.bytecode.JarArtifact;
//import com.securboration.immortals.repo.api.RepositoryUnsafe;
//import com.securboration.immortals.repo.etc.ExceptionWrapper;
//import com.securboration.immortals.service.config.ImmortalsServiceProperties;
//
//@RestController
//public class DtserviceController {
//	
//	private static final Logger logger = 
//            LoggerFactory.getLogger(DtserviceController.class);
//    
//    /**
//     * Configuration properties for the service
//     */
//    @Autowired(required = true)
//    private ImmortalsServiceProperties properties;
//    
//    /**
//     * Used to interact with Fuseki
//     */
//    @Autowired(required = true)
//    private RepositoryUnsafe repository;
//    
//    /**
//     * Used to convert Java objects into triples
//     */
//    @Autowired(required = true)
//    private ObjectToTriplesConfiguration o2tc;
//
//	@RequestMapping(value="/jarsubmit",method=RequestMethod.GET)
//	public static String respondGet(){
//		return new String("Please use post");
//	}
//	
//	/**
//	 * 
//	 * @param data
//	 * @return
//	 * @throws Exception
//	 */
//	@RequestMapping(value="/jarsubmit",method=RequestMethod.POST)
//	public String respondPost(@RequestBody HashMap<String,Object> data) throws Exception{
//		byte[] bytes = Base64.decodeBase64((String) data.get("bytes"));
//		String name = (String) data.get("name");
//		String artifactId = (String) data.get("artifactId");
//		String groupId = (String) data.get("groupId");
//		String version = (String) data.get("version");
//		SourceFinder sourceFinder = new SourceFinderDummy(null,null,null); //DUMMY
//		final String graphName = Helper.getImmortalsUuid(properties);
//
//		if (properties == null) {
//			throw new Exception("properties is null");
//		}
//
//		if (o2tc == null) {
//			throw new Exception("o2tc is null");
//		}
//		
//		AnnotationsToTriples att = getAnnotationsToTriples(o2tc);
//		
//		JarArtifact ja = JarIngestor.ingest(bytes, name, groupId, artifactId, version, sourceFinder, att);
//		
//		Model model = ObjectToTriples.convert(o2tc, ja);
//		repository.pushGraph(model, graphName);
//		return graphName;
//	}
//	
//    private static AnnotationsToTriples getAnnotationsToTriples(ObjectToTriplesConfiguration config){
//        
//        UriMappings uriMappings = new UriMappings(config);
//        
//        return new AnnotationsToTriples(uriMappings);
//    }
//	
//	
//	
//private static class Helper{
//        
//        private static String getImmortalsUuid(ImmortalsServiceProperties p){
//            return UUID.randomUUID().toString() + 
//                    "-IMMoRTALS-" + 
//                    p.getImmortalsVersion();
//        }
//    
//        private static String printModel(String graphName,Model model){
//            
//            StringBuilder sb = new StringBuilder();
//            ExceptionWrapper.wrap(()->{
//                sb.append(String.format("dump of graph [%s]\n", graphName));
//                sb.append(String.format("----------------------------------\n"));
//                sb.append(String.format(OntologyHelper.serializeModel(model, "Turtle", false)));
//                sb.append(String.format("----------------------------------\n"));
//            });
//            
//            return sb.toString();
//        }
//        
//    }
//
//}
