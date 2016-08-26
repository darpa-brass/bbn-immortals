package mil.darpa.immortals.core.das;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptationManager {

	static final Logger logger = LoggerFactory.getLogger(AdaptationManager.class);

	static {
		instance = new AdaptationManager();
		initialize();
	}
		
	private AdaptationManager() {
		ARQ.init();  //force init
	}
	
	public static AdaptationManager getInstance() {
		return instance;
	}
		
	public AdaptationStatus triggerAdaptation(String deploymentModelJson) {
		
		AdaptationStatus result = new AdaptationStatus();
		
		print("Adaptation Driver Triggered: Processing New Deployment Model", true);
		
		pushDeploymentModel(deploymentModelJson);

		//Once the deploymentModel is defined and inserted into the triple store,
		//we can replace the following code with SPARQL
		JsonReader json = Json.createReader(new StringReader(deploymentModelJson));
		JsonObject deploymentModel = json.readObject();
		List<JsonObject> functionalityPointsJson = deploymentModel.getJsonArray("functionalityPoints").getValuesAs(JsonObject.class);
		List<JsonString> resourceUris = deploymentModel.getJsonArray("resourceUris").getValuesAs(JsonString.class);

		String resourceString = resourceUris.stream().map(JsonString::getString).collect(Collectors.joining(","));

		//We won't need this going forward
		String resourceDigest = "";
		
		/*String resourceDigest = Arrays.asList(resourceString.split(","))
				.stream().map(e -> resourceLabels.get(e))
				.sorted((ResourceLabel r1, ResourceLabel r2) -> r1.rank - r2.rank)
				.map(s -> s.label)
				.collect(Collectors.joining("+"));
		*/

		print("Resources found in target environment ... ", false);
		
		resourceUris.stream().forEach(r -> print("\t\t" + r.getString(), false));
		
		print("DSL Resource Digest ... ", false);
		print("\t\t" + resourceDigest, false);

		print("Mission Requirements ... ", false);
		
		List<FunctionalityPoint> functionalityPoints = new ArrayList<FunctionalityPoint>();
		
		for (JsonObject o : functionalityPointsJson) {
			List<JsonString> propertyUrisJson = o.getJsonArray("propertyUris").getValuesAs(JsonString.class);
			List<String> propertyUris = propertyUrisJson.stream().map(JsonString::getString).collect(Collectors.toList());

			functionalityPoints.add(new FunctionalityPoint(o.getString("missionFunctionalityUri"),
					o.getString("controlPointUuid"), propertyUris));
		}
		
		functionalityPoints.stream().forEach(f -> print("\t\tMission Target:" + f.getMissionFunctionalityUri(), false));
		functionalityPoints.stream().forEach(f -> print("\t\tControl Point Target:" + f.getControlPointUuid(), false));
		
		print("", true);
		print("", false);
		
		if (functionalityPoints == null || functionalityPoints.isEmpty()) {
			//No mission requirements; should not occur
			result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
			result.setDetails("Deployment model did not specify target functionality.");
		} else {
			for (FunctionalityPoint f : functionalityPoints) {
				print("Adapting Code & Build Process", true);

				print("Target Functionality ...", false);
				print("\t\t" + f.getMissionFunctionalityUri(), false);
				List<DFU> candidates = getCandidateDFUs(f.getMissionFunctionalityUri(), resourceString, f.getPropertyUris());
								
				if (candidates.isEmpty()) {
					//No DFUs to pass to the DSL; basic requirements could not be satisfied
					result.setAdaptationStatusValue(AdaptationStatusValue.UNSUCCESSFUL);
					result.setDetails("No DFUs found to match basic deployment model constraints.");
					print("No DFUs found to match basic deployment model constraints.", false);
					break;
				} else {
					print("Initial DFU candidates ...", false);
					
					candidates.stream().forEach(c -> print("\t\t" + c.getClassName(), false));
					
					print("", true);

					//Invoke DSL for each DFU
					String dslError = null;
					
					print("",false);
					print("Calculating resource indexes", true);
					print("", false);
					
					for (DFU d : candidates) {
						try {
							print("\n" + new String(new char[100]).replace('\0', '*'), false);
							print("DFU Class: " + d.getClassName() +
									"\nDFU Functionality Type: " + d.getFunctionalityTypeUri() +
									"\nDFU Instance URI: " + d.getUri(), false);
							//DSL integration with Haskell is work in progress; resource + property checking applied with SPARQL for now
							//d.setResourceIndex(calculateResourceIndex(d, f.getMissionFunctionalityUri(), resourceDigest));
							d.setResourceIndex(0);
							print("Resource Index: " + d.getResourceIndex() + " (" + dslReturnLabels.get(d.getResourceIndex()) + ")", false);
							print(new String(new char[100]).replace('\0', '*'), false);
						} catch (Exception e) {
							dslError = e.getMessage();
							break;
						}
					}
					
					if (dslError != null) {
						result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
						result.setDetails("Unexpected error invoking DSL: " + dslError);
						break;
					} else {
						Optional<DFU> selectedDFU = candidates.stream()
														.sorted((DFU d1, DFU d2) -> d1.getResourceIndex() - d2.getResourceIndex())
														.findFirst();
						if (selectedDFU.isPresent() && selectedDFU.get().getResourceIndex() == 0) {
							//Adapt all control points for the functionality using the DFU
							try {
								print("", false);
								print("Selected DFU", true);
								print("DFU Class: " + selectedDFU.get().getClassName(), false);
								print("DFU Functionality Type: " + selectedDFU.get().getFunctionalityTypeUri(), false);
								print("DFU Instance URI: " + selectedDFU.get().getUri(), false);
								print("", true);
								
								print("", false);
								print("Adapting Control Points for Selected DFU", true);
								
								adaptControlPoints(f, selectedDFU.get());
								
								print("", true);
								
								//This is ad-hoc for now, but we need some way to know what to build (i.e., which
								//build artifact is this control point associated with?). For now, just build the ATAKClient.
								//Going forward we may need to modify the control point vocabulary
								
								print("", false);
								print("Building ATAK Client", true);
								buildAtakClient(selectedDFU.get());
								print("", true);
								result.setSelectedDfu(selectedDFU.get().getClassName());
							} catch (Exception e) {
								result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
								result.setDetails("Unexpected error adapting control point: " + e.getMessage());
								break;
							}
						} else {
							//At least one functionalityUri not matched to a DFU; set error and exit.
							//We may change this practice depending on how we want to handle partially successful adaptations.
							result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
							String temp =  "No suitable adaptation for " + f.getMissionFunctionalityUri() + 
									" in deployment model.";
							result.setDetails(temp);
							print(temp, false);
							break;
						}
					}
				}
			}
		}

		if (result.getAdaptationStatusValue() == AdaptationStatusValue.PENDING) {
			print("", false);
			print("DAS completed adaptation process successfully.", true);
			result = new AdaptationStatus(AdaptationStatusValue.SUCCESSFUL);
		}
		
		return result;
	}
	
	@SuppressWarnings("unused")
	private int calculateResourceIndex(DFU dfu, String functionalityUri, String resourceDigest) throws Exception {

		//!!This method is not working at the moment since the interface between the DAS and Haskell's DSL is in flux
		
		String functionalityLabel = null;
		//String functionalityLabel = requirementLabels.get(functionalityUri);
		String dfuLabel = dfuLabels.get(dfu.getFunctionalityTypeUri());
		
		int result = 0;
		
		if (dfuLabel != null) {
			//DSL call:
			//Command: stack exec resource-dsl location [req-name] [dfu-name] [env-name]
			//Example: stack exec resource-dsl location location gps-android GPS-Sat+GPS-Dev
			ProcessBuilder pb = new ProcessBuilder("stack", "exec", "resource-dsl", "location",
							   functionalityLabel, dfuLabel, resourceDigest);
			pb.directory(new File(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + DSL_FOLDER));
			pb.inheritIO();
			Process p = pb.start(); //IOException will be thrown to caller
			
			//DSL return codes (we can put these into an enum later maybe):
			//		0: 	OK -- the DFU loads successfully and satisfies the requirements
			//		1: 	Miscellaneous Error -- see output for details (e.g. bad arguments, JSON
			//			decoding error, bug in interpreter)
			//		2: 	DFU cannot be loaded in the given environment.
			//		3:	DFU successfully loads but does not satisfy the requirements.
			if (p.waitFor(15, TimeUnit.SECONDS)) {
				result = p.exitValue();
			} else {
				throw new Exception("DSL invocation timed out.");
			}
		} else {
			result = 2;
		}

		if (result == 1) {
			throw new Exception("Unexpected DSL error.");
		}

		return result;
	}
	
	private void buildAtakClient(DFU selectedDFU) throws Exception  {
		
		//Rename existing dependency file
		File file = new File(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + ATAK_CLIENT + "dependencies.gradle");
		if (file.exists() && !file.isDirectory()) {
			file.renameTo(new File(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + ATAK_CLIENT + "dependencies.gradle" + ".old." + new Date().getTime()));
		}
		
		//Format of dependency file
		//apply plugin: 'com.android.application'
		//	dependencies {
		//	    compile 'mil.darpa.immortals.dfus:LocationProviderAndroidGPS:+'
		//	    compile 'mil.darpa.immortals.dfus:LocationProviderSimulated:+'
		//	}
		
		//Write dependency file
		FileWriter writer = new FileWriter(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + ATAK_CLIENT + "dependencies.gradle");
		String eol = System.getProperty("line.separator");
		
		writer.write("apply plugin: 'com.android.application' " + eol);
		writer.append("dependencies {" + eol + "compile '" + selectedDFU.getDependencyCoordinate().getGroupId()
				+ ":" + selectedDFU.getDependencyCoordinate().getArtifactId() + ":+'" + eol + "}");

		writer.flush();
		writer.close();
		
		//Build client
		ProcessBuilder pb = new ProcessBuilder("gradle", "build");
		pb.directory(new File(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + ATAK_CLIENT));
		pb.inheritIO();
		Process p = pb.start();
		
		try {
			if (p.waitFor(2, TimeUnit.MINUTES)) {
				if (p.exitValue() != 0) {
					throw new Exception("Unexpected exception executing ATAKClient build.");
				}
			} else {
				throw new Exception("Timed out waiting for ATAKClient to complete build.");
			}
		} catch (InterruptedException e) {
			throw new Exception("Unexpected interrupt waiting for ATAKClient build: " + e.getMessage());
		}

	}
	
	private void adaptControlPoints(FunctionalityPoint functionalityPoint, DFU dfu) throws Exception {
		
        String dfuReference = null;
        Template cpTemplate = null;
        VelocityContext context = null;

		Properties p = new Properties();
		p.put("file.resource.loader.path", DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + SOURCE_TEMPLATE_FOLDER);
		Velocity.init(p);
		String dfuCanonicalClassName = null;
			
        List<ControlPoint> controlPoints = getControlPoints(functionalityPoint);
        
		print(controlPoints.size() + " control point(s) found", false);

        for (ControlPoint cp : controlPoints) {
        	
        	print("Adapting control point: " + cp.getUri(), false);
        	
        	cpTemplate = Velocity.getTemplate(cp.getClassName() + ".java");
        	
        	String declaration, init, cleanup, work;
        	declaration = init = cleanup = work = null;

        	Map<String, String> dfuMethods = null;
        	        	
        	try {
	        	dfuCanonicalClassName = dfu.getClassName().replace('/', '.');

        		dfuReference = "a" + UUID.randomUUID().toString().replace('-', '_');
	        	
	        	dfuMethods = getDFULifecycleMethods(dfu.getUri());

	        	if (dfuMethods == null || dfuMethods.isEmpty()) {
	        		//DFU is not properly annotated and cannot be used; this is an error condition
	        		throw new Exception("DFU " + dfu.getClassName() + " missing lifecycle annotations.");
	        	}
	        	
	        	context = new VelocityContext();
	        	
	        	declaration = dfuCanonicalClassName + " " + dfuReference + " = new " + dfuCanonicalClassName + "();";
            	context.put(cp.getUuid() + "-" + LIFECYCLE_DECLARATION, declaration);

	        	if (dfuMethods.containsKey(LIFECYCLE_INIT)) {
	        		init = dfuReference + "." + dfuMethods.get(LIFECYCLE_INIT) + "(this);";
	        	} else {
	        		init = "//Init No-Op";
	        	}
	        	
            	context.put(cp.getUuid() + "-init", init);
	        	
	        	if (dfuMethods.containsKey(LIFECYCLE_CLEANUP)) {
		        	cleanup = dfuReference + "." + dfuMethods.get(LIFECYCLE_CLEANUP) + "();";	        		
	        	} else {
	        		cleanup = "//Cleanup No-Op";
	        	}
	        	
	        	context.put(cp.getUuid() + "-cleanup", cleanup);
	        	
	        	if (dfuMethods.containsKey(LIFECYCLE_WORK)) {
		        	work = dfuReference + "." + dfuMethods.get(LIFECYCLE_WORK) + "();";
	        	} else {
	        		work = "//Work No-Op";
	        	}
	        	
	        	context.put(cp.getUuid() + "-work", work);
			} catch (ClassNotFoundException e1) {
				//Log error and throw exception
				throw e1;
			}
        	        	
        	FileWriter writer = null;
        	
			try {
				String root = DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT);
				File file = new File(root + CONTROL_POINT_FOLDER + cp.getClassName() + ".java");
				if (file.exists() && !file.isDirectory()) {
					file.renameTo(new File(root + CONTROL_POINT_FOLDER + cp.getClassName() + ".old." + new Date().getTime()));
				}
				writer = new FileWriter(root + CONTROL_POINT_FOLDER + cp.getClassName() + ".java");
	        	cpTemplate.merge(context, writer);
	        	writer.flush();
			} catch (IOException e) {
				throw e;
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					//Log only
				}
			}
        }
	}
	
	private List<ControlPoint> getControlPoints(FunctionalityPoint functionalityPoint) {
		
		List<ControlPoint> results = new ArrayList<ControlPoint>();
		
		String query = "PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX f:  <http://darpa.mil/immortals/ontology/r2.0.0/functionality#> " +
			"	PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/cp#> " +
			"	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"	PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
			"	SELECT ?cp_uri ?uuid ?class_name ?class_url " +
			"	WHERE { " +
        	"  	GRAPH <" + bootstrapUri + "> { " +
			"		?cp_uri  a cp:ControlPoint . " +
			"	    ?cp_uri im:hasControlPointUuid_String '" + functionalityPoint.getControlPointUuid() + "' . " +
			"	    ?cp_uri im:hasControlPointUuid_String ?uuid. " +
			"	    ?cp_uri im:hasOwnerClass_AClass ?class . " +
			"	    ?class im:hasClassName_String ?class_name . " +
			"	    ?class im:hasClassUrl_String ?class_url . " + 
			"	  } " +
			"	} ";
		
        try (QueryExecution qe = QueryExecutionFactory.sparqlService(FUSEKI_QUERY_ENDPOINT, query)) {
	        ResultSet resultSet = qe.execSelect();
	        resultSet.forEachRemaining(t -> results.add(new ControlPoint(t.getResource("cp_uri").getURI(), 
	        		t.getLiteral("uuid").getString(),
	        		t.getLiteral("class_name").getString(),
	        		t.getLiteral("class_url").getString())));
        }

        return results;
	}
	
	private List<DFU> getCandidateDFUs(String functionalityUri, String resourceUris, List<String> propertyUris) {
		
		List<DFU> result = new ArrayList<DFU>();

		String query =
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> " +
				"PREFIX lp_func: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#> " +
				"PREFIX bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

				"SELECT DISTINCT ?dfu ?className ?groupId ?artifactId ?version ?functionalityUri " +
				"WHERE { " +
	    		"	  	GRAPH <" + bootstrapUri + "> { " +
				"			?dfu a dfu:DfuInstance . " +
				"			?dfu im:hasFunctionalityAbstraction_Class ?functionalityUri . " +
				"			?dfu im:hasClassPointer_String ?classPointer . " +
				"			?class a bytecode:AClass . " +
				"			?class im:hasBytecodePointer_String ?classPointer . " +
				"			?class im:hasClassName_String ?className . " +
				"			?jar a bytecode:JarArtifact . " +
				"			?jar im:hasClasses_AClass ?class . " +
				"			?jar im:hasCoordinate_BytecodeArtifactCoordinate ?coordinate . " +
				"			?coordinate im:hasVersion_String ?version . " +
				"			?coordinate im:hasArtifactId_String ?artifactId . " +
				"			?coordinate im:hasGroupId_String ?groupId . " +
				"			filter (?functionalityUri = " + functionalityUri + ") ";

				for (String property : propertyUris) {
					query = query.concat(
							"?dfu im:hasDfuProperties_Property ?propertyInstance . " +
							"?propertyInstance a " + property + ".");
				}

			    query = query.concat(
			    		"MINUS { " +
			    		"SELECT DISTINCT ?dfu " +
			    		"WHERE { " +
			    		"GRAPH <" + bootstrapUri + "> { " +
			            "?dfu a dfu:DfuInstance . " +
			            "?dfu im:hasFunctionalityAbstraction_Class ?functionalityUri . " +
			            "?dfu im:hasResourceDependencies_Class ?resource . " +
			            "FILTER(?resource NOT IN ( " + resourceUris +
			            "  ) " +
			            ") " +
			          "} " +
			        "} " +
			      "}");
	
			query = query.concat("}}");

        try (QueryExecution qe = QueryExecutionFactory.sparqlService(FUSEKI_QUERY_ENDPOINT, query)) {
	        ResultSet resultSet = qe.execSelect();
	        resultSet.forEachRemaining(t -> result.add(new DFU(t.getResource("dfu").getURI(), 
	        				t.getLiteral("className").getString(),
	        				new DependencyCoordinate(t.getLiteral("groupId").toString(),
	        						t.getLiteral("artifactId").toString(),
	        						t.getLiteral("version").toString()),
	        						"<" + t.getResource("functionalityUri").getURI() + ">")));
        }

        return result;
	}
	
	private void pushDeploymentModel(String deploymentModelJSON) {

		//String deploymentUri = repositoryService.path("pushDeploymentModel").request().post(Entity.entity(deploymentModel, 
		//	MediaType.APPLICATION_JSON_TYPE), String.class);
	}
	
	private static void initialize() {

		WebTarget repositoryService = null;
		String byteCodeURI = null;
		
		try {
			repositoryService = ClientBuilder.newClient(new ClientConfig()
					.register(JacksonFeature.class))
					.target(REPOSITORY_SERVICE_CONTEXT_ROOT);
	        byteCodeURI = repositoryService.path("bootstrap").request().post(null, String.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
        if (byteCodeURI != null && byteCodeURI.trim().length() > 0) {
        	bootstrapUri = FUSEKI_DATA_ENDPOINT + byteCodeURI;
        }
        
    	resourceLabels = new HashMap<String, ResourceLabel>();
    	dfuLabels = new HashMap<String, String>();
    	dslReturnLabels = new HashMap<Integer, String>();

    	//We won't need this going forward after the DSL changes
    	resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources#BluetoothResource>", 
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources#BluetoothResource>", "Ext-BT", 3));
        resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources#UsbResource>", 
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources#UsbResource>", "Ext-USB", 4));
        resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources#UserInterface>", 
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources#UserInterface>", "Has-UI", 5));
        resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsReceiver>", 
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsReceiver>", "GPS-Dev", 2));
        resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsSatellite>",
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsSatellite>", "GPS-Sat", 1));
        
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderManualSimulated", "dead-reckoning");
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderAndroidGpsBuiltIn", "gps-android");
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderBluetoothGpsSimulated", "gps-bluetooth");
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderSaasmSimulated", "gps-saasm");
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderUsbGpsSimulated", "gps-usb");
        
        dslReturnLabels.put(0, "The DFU loads successfully and satisfies the requirements");
        dslReturnLabels.put(1, "Miscellaneous Error -- see output for details (e.g. bad arguments, JSON decoding error, bug in interpreter)");
        dslReturnLabels.put(2, "The DFU cannot be loaded in the given environment.");
        dslReturnLabels.put(3, "The DFU successfully loads but does not satisfy the requirements.");
	}

	private Map<String, String> getDFULifecycleMethods(String dfuUri) {

		HashMap<String, String> result = new HashMap<String, String>();

		String query = 
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#>" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

			"SELECT DISTINCT ?dfu ?abstractAspect ?methodName " +
			"WHERE { " +
    		"	  	GRAPH <" + bootstrapUri + "> { " +
			"			?dfu a dfu:DfuInstance . " +
			"  			?dfu im:hasFunctionalAspects_FunctionalAspectInstance ?functionalAspectInstance . " +
			"  			?functionalAspectInstance im:hasAbstractAspect_Class ?abstractAspect . " +
			"  			?functionalAspectInstance im:hasMethodPointer_String ?methodPointer . " +
			"  			?method im:hasBytecodePointer_String ?methodPointer . " +
			"  			?method im:hasMethodName_String ?methodName . " +
			"			} " +
			"FILTER (?dfu = <" + dfuUri + ">)" + 
			"}";
        
        try (QueryExecution qe = QueryExecutionFactory.sparqlService(FUSEKI_QUERY_ENDPOINT, query)) {
	        ResultSet resultSet = qe.execSelect();
	        resultSet.forEachRemaining(t -> result.put(t.getResource("abstractAspect").toString(), 
	        				t.getLiteral("methodName").getString()));
        }
		
		return result;
	}
	
	private void print(String message, boolean heading) {
		
		if (heading) {
			if (message.length() < 100) {
				int lpad = (int) Math.floor((100 - message.length())/2f);
				int rpad = (int) Math.ceil((100 - message.length())/2f);
	
				System.out.println("\n" + new String(new char[lpad]).replace('\0', '*') + message + new String(new char[rpad]).replace('\0', '*'));				
			} else {
				System.out.println("\n" + message);
			}
		} else {
			System.out.println("\n" + message);
		}
	}

	private static AdaptationManager instance;
	private static String bootstrapUri;
		
	//These are relative to the Immortals root path provided to the DAS as a startup parameter (see DAS.java)
	private static final String SOURCE_TEMPLATE_FOLDER = "/client/ATAKLite/src-templates/";
	private static final String CONTROL_POINT_FOLDER = "/client/ATAKLite/src/";
	private static final String DSL_FOLDER = "/dsl/resource-dsl";
	private static final String ATAK_CLIENT = "/client/ATAKLite/";

	//We can potentially change these so that these can be passed in to the DAS as well
	private static final String REPOSITORY_SERVICE_CONTEXT_ROOT = "http://localhost:9999/immortalsRepositoryService/";	
	private static final String FUSEKI_DATA_ENDPOINT = "http://localhost:3030/ds/data/";
	private static final String FUSEKI_QUERY_ENDPOINT = "http://localhost:3030/ds/query";

	private static final String LIFECYCLE_DECLARATION = "declaration";
	private static final String LIFECYCLE_INIT = "http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#InitializeAspect~instance0";
	private static final String LIFECYCLE_CLEANUP = "http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#CleanupAspect~instance0";
	private static final String LIFECYCLE_WORK = "http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#GetCurrentLocationAspect~instance0";

	private static class ResourceLabel {
	    public ResourceLabel(String uri, String label, int rank) {
	      this.uri = uri;
	      this.label = label;
	      this.rank = rank;
	    }
	    
	    @SuppressWarnings("unused")
		public String uri;
		public String label;
	    public int rank;
	}

	private static Map<String, ResourceLabel> resourceLabels;
	private static Map<String, String> dfuLabels;
	private static Map<Integer, String> dslReturnLabels;

}
