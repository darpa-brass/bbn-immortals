package mil.darpa.immortals.core.das;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import mil.darpa.immortals.das.configuration.DfuCompositionConfiguration;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.jena.query.ARQ;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.darpa.immortals.core.das.sparql.AbstractResources;
import mil.darpa.immortals.core.das.sparql.AggregateResourceProfiles;
import mil.darpa.immortals.core.das.sparql.CandidateDFUList;
import mil.darpa.immortals.core.das.sparql.ConfigurationAspects;
import mil.darpa.immortals.core.das.sparql.ControlPoints;
import mil.darpa.immortals.core.das.sparql.DFUByClassName;
import mil.darpa.immortals.core.das.sparql.DFULifecycleMethods;
import mil.darpa.immortals.core.das.sparql.DataflowProducer;
import mil.darpa.immortals.core.das.sparql.DataflowQuery;
import mil.darpa.immortals.core.das.sparql.FunctionalitySpecs;
import mil.darpa.immortals.core.das.sparql.ImageScalingCodeMetrics;
import mil.darpa.immortals.core.das.sparql.MetricQuery;
import mil.darpa.immortals.core.das.sparql.MissionMetrics;
import mil.darpa.immortals.core.das.sparql.MissionMetricsMap;
import mil.darpa.immortals.core.das.sparql.PropertyImpact;
import mil.darpa.immortals.core.das.sparql.SessionIdentifier;
import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.sourcecomposer.SourceComposer;
import java.nio.file.Path;

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
			
	/**
	 * This method is the start point for the DAS adaptation process (technically, the action starts from the DASEndpoint.triggerAdaptation class, 
	 * but that is merely the REST endpoint for this method, plus a small amount of logic).
	 * 
	 * @param deploymentModelRdf String containing an rdf description of the mission requirements and target resources
	 * 
	 * @return AdaptationStatus instance that describes the outcome
	 * 
	 * @throws IOException
	 */
	public AdaptationStatus triggerAdaptation(String deploymentModelRdf) throws IOException {
		
		AdaptationStatus result = new AdaptationStatus();
		
		Optional<DFU> selectedDFU = null;
		boolean queueBuild = false;
		
		String deploymentGraphUri = null;
		
		//Push the deployment model to the repository service
		try {
			deploymentGraphUri = pushDeploymentModel(deploymentModelRdf);
			result.addAudit("Successfully added deployment model to triple store.");
		} catch (Exception e) {
			result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
			result.setDetails("Unexpected error loading deployment model.");

			return result;
		}

		String sessionIdentifier = SessionIdentifier.select(deploymentGraphUri);
		
		result.setSessionIdentifier(sessionIdentifier);
		
		// Load the SourceComposer with the initialized environment configuration
		SourceComposer sourceComposer = new SourceComposer(sessionIdentifier);

		// Construct an application instance
		SourceComposer.ApplicationInstance applicationInstance = 
				sourceComposer.initializeApplicationInstance(EnvironmentConfiguration.CompositionTarget.Client_ATAKLite);

		//Retrieve the deployment's functionality requirements and properties
		List<FunctionalitySpecification> functionalitySpecifications = FunctionalitySpecs.select(deploymentGraphUri);
		
		//Resolve deployment-level resources to dfu abstractions
		List<String> abstractResourceUris = AbstractResources.select(deploymentGraphUri);
		
		if (abstractResourceUris == null || abstractResourceUris.isEmpty()) {
			//This currently won't likely happen as the default deployment model always includes
			//several non-perturbable resources (e.g., network, server, etc.).
			result.setAdaptationStatusValue(AdaptationStatusValue.UNSUCCESSFUL);
			result.setDetails("No resources were provided in the target environment.");
			
			return result;
		}

		abstractResourceUris.stream().forEach(t -> result.addAudit("Mission Resource URI: " + t));
		String resourceDigest = generateResourceDigest(abstractResourceUris);
		
		if (resourceDigest == null || resourceDigest.trim().length() == 0) {
			//This could happen if we were given some resources but none of the resources that 
			//were provided could be mapped to cp1 & cp2. This can occur for several reasons.
			result.setAdaptationStatusValue(AdaptationStatusValue.UNSUCCESSFUL);
			result.setDetails("None of the cp1 or cp2 resources were provided in the target environment.");
			
			return result;
		}

		boolean saasm = false;
		
		if (functionalitySpecifications == null || functionalitySpecifications.isEmpty()) {
			//No mission requirements; should not occur
			result.setAdaptationStatusValue(AdaptationStatusValue.INVALID_REQUEST);
			result.setDetails("Deployment model did not specify target functionality.");
			
			return result;
		}
		
		functionalitySpecifications.forEach(t -> result.addAudit("Mission Functionality: " + t));

		
		for (FunctionalitySpecification f : functionalitySpecifications) {

			saasm = (f.getPropertyUris().contains(SAASM_URI));
			
			DSLInvocationResultValue dslInputResult = generateDslLocationInput(resourceDigest, saasm);

			if (dslInputResult != DSLInvocationResultValue.MODEL_OK) {
				//This is an error at this point since we couldn't initialized the dsl with the resource environment
				//(i.e., we haven't gotten to an actual deployment test).
				result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
				result.setDetails("Could not initialize DSL input files for DSL check at DFU level:" + dslInputResult.description());
				
				return result;
			}
			
			result.addAudit("DSL input files initialized for DFU level checks with resource digest: " + resourceDigest);

			List<DFU> candidates = CandidateDFUList.select(bootstrapUri, f.getFunctionalityUri(), abstractResourceUris, f.getPropertyUris());
			
			if (candidates.isEmpty()) {
				//No DFUs to pass to the DSL; basic requirements could not be satisfied
				result.setAdaptationStatusValue(AdaptationStatusValue.UNSUCCESSFUL);
				result.setDetails("No DFUs found to match basic deployment model constraints.");
				return result;
			}
			
			candidates.forEach(t -> result.addAudit("Candidate DFU: " + t.getClassName()));
			
			//Invoke DSL for each DFU
			DSLInvocationResultValue dfuDSLReturn = DSLInvocationResultValue.MODEL_OK;
			
			boolean error = false;
			
			for (DFU d : candidates) {
				dfuDSLReturn = performDFUDSLCheck(d);
				if (dfuDSLReturn == DSLInvocationResultValue.ERROR) {
					//This branch detects unexpected error invoking the 
					//DSL (versus detecting that the provided dfu fails 
					//to satisfy the mission requirements and/or exceeds 
					//the resources in the target environment). I'm treating this as
					//a general no-go since it's likely caused by Haskel setup issue
					error = true;
					break;
				} else {
					//The return is treated as an index (for future use), but in practice the value is discrete
					//(the dfu works or doesn't); this is not an issue since only working DFUs
					//for the given model are chosen below
					d.setResourceIndex(dfuDSLReturn.value());
				}
			}
			
			if (error) {
				result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
				result.setDetails("Unexpected error invoking DSL for DFU level check.");
				return result;
			}
			
			selectedDFU = candidates.stream().sorted((DFU d1, DFU d2) -> 
											d1.getResourceIndex() - d2.getResourceIndex())
											.findFirst();
			if (selectedDFU.isPresent() && selectedDFU.get().getResourceIndex() == DSLInvocationResultValue.MODEL_OK.value()) {
				result.addAudit("Selected DFU for adaptation: " + selectedDFU.get().getClassName());
				
				//Adapt all control points for the functionality using the DFU
				try {
					adaptControlPoints(f, selectedDFU.get(), applicationInstance);
					queueBuild = true;
					result.setSelectedDfu(selectedDFU.get().getClassName());
				} catch (Exception e) {
					result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
					result.setDetails("Unexpected error adapting control point: " + e.getMessage());
					return result;
				}
			} else {
				//At least one functionalityUri not matched to a DFU; set error and exit.
				//We may change this practice depending on how we want to handle partially successful adaptations.
				result.setAdaptationStatusValue(AdaptationStatusValue.UNSUCCESSFUL);
				String temp =  "No suitable adaptation for " + f.getFunctionalityUri() + 
						" in deployment model.";
				result.setDetails(temp);
				result.addAudit(temp);
				return result;
			}

			//Check aggregate resources defined at control points
			Map<String, Metric> missionMap = MissionMetricsMap.select(deploymentGraphUri);
			missionMap.keySet().stream().sorted().forEach(k -> result.addAudit(k + ":" + missionMap.get(k).getValue()));

			//Begin ad-hoc block
			
			//The DAS attempts to drive the adaptation as generically as possible (driven by points of use with annotated
			//metadata and aggregate resource usage models). The DSL invocation is specific for now to cp2 details (even if we
			//combined the cp1 and cp2 interfaces, the parameters must be called out and are specific to challenge problems).
			//We will revisit for future drops. For now, we have a very specific block below for the baseline DSL check.
			long bandwidth = Math.round(Double.parseDouble(missionMap.get("TotalAvailableServerBandwidth").getValue()));
			long pliReportRate = Math.round(Double.parseDouble(missionMap.get("PLIReportRate").getValue()));
			long imageReportRate = Math.round(Double.parseDouble(missionMap.get("ImageReportRate").getValue()));
			int numberClients = Integer.parseInt(missionMap.get("NumberOfClients").getValue());										
			double scalingFactor = 1.0f;
			
			DSLInvocationResultValue dslCheckResult = performNetworkDSLCheck(bandwidth, numberClients, scalingFactor, pliReportRate, imageReportRate);
			//result.addAudit("DSL bandwidth check for initial model: " + dslCheckResult.description());
			//End ad-hoc block
			
			List<ControlPoint> controlPoints = ControlPoints.select(bootstrapUri, f);
			
			for (ControlPoint cp : controlPoints) {
				Dataflow dataflow = DataflowQuery.select(bootstrapUri, cp.getDataflowEntryPoint());
				
				List<ResourceProfile> resourceProfiles = AggregateResourceProfiles.select(bootstrapUri, cp.getUri());

				for (ResourceProfile rp : resourceProfiles) {
					
					Metric constrainingMetric = MetricQuery.select(rp.getConstrainingMetricLinkID(), deploymentGraphUri);

					String formula = rp.getFormula();
					Number formulaResult = resolveFormula(formula, deploymentGraphUri, null);
					
					result.addAudit("DAS computed bandwidth usage for initial environment: " + formulaResult.doubleValue());
					
					if (constrainingMetric.getAssertionCriterion() == AssertionCriterionValue.LESS_THAN_INCLUSIVE) {

						if (formulaResult.doubleValue() > Double.parseDouble(constrainingMetric.getValue()) && constrainingMetric.getUnit().equals(rp.getUnit())) {
							
							result.addAudit("Bandwidth usage exceeds available resources in target environment.");
							
							//Violation of constraining metric;begin domain analysis
							String violatedResource = constrainingMetric.getProperty();
							DFURemediation dfuRemediation = getDfuRemediation(violatedResource, "PROPERTY_DECREASES");
							if (dataflow.containsDataType(dfuRemediation.getApplicableDataType())) {
								List<DFU> dfuRemediations = CandidateDFUList.selectByFunctionalAspect(bootstrapUri, dfuRemediation.getFunctionalAspectUri(), 
										abstractResourceUris, new ArrayList<String>());
								if (dfuRemediations != null && !dfuRemediations.isEmpty()) {
									DFU dfu = dfuRemediations.get(0);
									String originalDFUClassName = DataflowProducer.select(bootstrapUri, dfuRemediation.getApplicableDataType(), cp.getDataflowEntryPoint());
									DFU originalDfu = DFUByClassName.select(bootstrapUri, originalDFUClassName.replace(".","/"));
									
									//#####Begin section to be reworked/abstracted#####
									ArrayList<String> params = new ArrayList<String>();

									List<String> configurationAspects = ConfigurationAspects.select(bootstrapUri, dfuRemediation.getStrategyUri());

									List<Map<String, String>> codeMetrics = ImageScalingCodeMetrics.select(bootstrapUri, dfu.getClassName(), 
											configurationAspects.get(0), 5.0); //this 5.0 value is in the deployment model so we can remove this hard-coded value
									
									for (Map<String, String> m : codeMetrics) {
										Map<String, String> replacements = new HashMap<String, String>();
										replacements.put("DefaultImageSize", m.get("outputMegapixels"));
										
										scalingFactor = Double.parseDouble(m.get("scalingFactor"));
										dslCheckResult = performNetworkDSLCheck(bandwidth, numberClients, scalingFactor, pliReportRate, imageReportRate);
										//result.addAudit("DSL check result for scaling factor " + scalingFactor + " : " + dslCheckResult.description());

										Number testFormulaResult = resolveFormula(formula, deploymentGraphUri, replacements);
										
										result.addAudit("DAS computed bandwidth for scaling factor " + scalingFactor + " : " + testFormulaResult.doubleValue());
										
										if (testFormulaResult.doubleValue() <= Double.parseDouble(constrainingMetric.getValue())) {
											params.add(m.get("scalingFactor"));
											result.setDetails("Scaling factor used during synthesis: " + m.get("scalingFactor"));
											result.addAudit("Scaling factor used during synthesis: " + m.get("scalingFactor"));
											break;
										}
									}
									//#####End section to be reworked/abstracted#####

									if (params.isEmpty()) {
										result.setAdaptationStatusValue(AdaptationStatusValue.UNSUCCESSFUL);
										String temp =  "No suitable scaler value for " + f.getFunctionalityUri() + 
												" in deployment model.";
										result.setDetails(temp);
										result.addAudit("Resource profile constraint: " + rp.getTargetResourceTypeUri() + " at control point: " + cp.getUri() +
												" could not be satisfied with adaptation.");
									} else {
										try {
											DfuCompositionConfiguration dfuCompositionConfiguration =
													sourceComposer.constructCP2ControlPointComposition(originalDfu.getDependencyCoordinate().toString(),
															originalDfu.getClassName().replace("/", "."),
															dfu.getDependencyCoordinate().toString(), dfu.getClassName().replace("/", "."), params);
											sourceComposer.executeCP2Composition(applicationInstance, dfuCompositionConfiguration);
											result.addAudit("Resource profile constraint: " + rp.getTargetResourceTypeUri() + " at control point: " + cp.getUri() +
													" was violated but could be satisfied with adaptation.");
											queueBuild = true;
										} catch (Exception e) {
											result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
											result.setDetails("Unexpected error invoking Dfu Synthesis: " + e.getMessage());
										}
									}
								}
							}
						} else {
							result.addAudit("Resource profile constraint: " + rp.getTargetResourceTypeUri() + " at control point: " + cp.getUri() +
									" was satisfied without adaptation.");
						}
					}
				}
				
				if (result.getAdaptationStatusValue() != AdaptationStatusValue.PENDING) {
					break;
				}

			}
		}

		if (queueBuild && result.getAdaptationStatusValue() == AdaptationStatusValue.PENDING) {
			try {
				result.addAudit("Adaptation queued a build.");
				buildAtakClient(applicationInstance);
				result.addAudit("Build completed successfully.");
			} catch (Exception e) {
				result.setAdaptationStatusValue(AdaptationStatusValue.ERROR);
				result.setDetails("Unexpected error building ATAKClient: " + e.getMessage());
			}
		}

		if (result.getAdaptationStatusValue() == AdaptationStatusValue.PENDING) {
			result.setAdaptationStatusValue(AdaptationStatusValue.SUCCESSFUL);
			result.addAudit("Adaptation was completed successfully.");
		}
		
		return result;
	}
	
	@SuppressWarnings("unused")
	private Number solveLinearEquation(String formula, String deploymentGraphUri, double constant) {
		
		Number result = null;
		
	    List<Metric> metrics = MissionMetrics.select(deploymentGraphUri);
	    
	    for (Metric m : metrics) {
		    if (formula.contains(m.getLinkId())) {
		    	formula = formula.replace(m.getLinkId(), m.getValue());
		    }
	    }

	    String resolvedEquation = constant + "=" + formula;
	    
		RealMatrix coefficients = new Array2DRowRealMatrix(new double[][] {{5}}, false);
		DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
		RealVector constants = new ArrayRealVector(new double[] {25}, false);
		RealVector solution = solver.solve(constants);
   
		return result;

	}
	
	private Number resolveFormula(String formula, String deploymentGraphUri, Map<String, String> replacements) {
		
		Number result = null;
		
		JexlEngine jexlEngine = new JexlEngine();
		jexlEngine.setLenient(false);
		jexlEngine.setSilent(false);
		 
		Expression e = jexlEngine.createExpression(formula);

	    JexlContext context = new MapContext();
	    
	    List<Metric> metrics = MissionMetrics.select(deploymentGraphUri);
	    
	    for (Metric m : metrics) {
	    	if (replacements != null && replacements.containsKey(m.getLinkId())) {
	    		context.set(m.getLinkId(), replacements.get(m.getLinkId()));
	    	} else {
			    context.set(m.getLinkId(), m.getValue());
	    	}
	    }
	    
	    result = (Number) e.evaluate(context);
		
		return result;
	}
	
	private DSLInvocationResultValue performDFUDSLCheck(DFU dfu) {

		DSLInvocationResultValue result = DSLInvocationResultValue.MODEL_OK;

		String dfuLabel = dfuLabels.get(dfu.getClassName());

		if (dfuLabel != null) {
			try {
				ProcessBuilder pb = new ProcessBuilder("stack", "exec", "resource-dsl", "--", "check", "--config",
									"[\"" + dfuLabel + "\"]");
				pb.directory(new File(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + DSL_FOLDER));
				pb.inheritIO();
				Process p = pb.start();
				
				if (p.waitFor(15, TimeUnit.SECONDS)) {
					result = DSLInvocationResultValue.fromValue(p.exitValue());
					if (result == null) {
						//Unexpected return code; error condition
						result = DSLInvocationResultValue.ERROR;
					}
				} else {
					result = DSLInvocationResultValue.ERROR;
				}
			} catch (Exception e) {
				result = DSLInvocationResultValue.ERROR;
			}
		} else {
			result = DSLInvocationResultValue.ERROR;
		}

		return result;
	}
	
	private DSLInvocationResultValue performNetworkDSLCheck(long bandwidth, int numberClients, double scalingFactor,
			long pliReportRate, long imageReportRate) {

		DSLInvocationResultValue result = DSLInvocationResultValue.MODEL_OK;
		
		try {
			result = generateDslNetworkInput(bandwidth, numberClients, scalingFactor,
					pliReportRate, imageReportRate);

			if (result == DSLInvocationResultValue.MODEL_OK) {
				ProcessBuilder pb = new ProcessBuilder("stack", "exec", "resource-dsl", "--", "check");
				
				pb.directory(new File(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + DSL_FOLDER));
				pb.inheritIO();
				Process p = pb.start();
				
				if (p.waitFor(15, TimeUnit.SECONDS)) {
					result = DSLInvocationResultValue.fromValue(p.exitValue());
					if (result == null) {
						//Unexpected return code; error condition
						result = DSLInvocationResultValue.ERROR;
					}
				} else {
					result = DSLInvocationResultValue.ERROR;
				}
			}
		} catch (Exception e) {
			result = DSLInvocationResultValue.ERROR;
		}
		
		return result;
	}
	
	private DSLInvocationResultValue generateDslLocationInput(String resourceDigest, boolean saasm) {

		DSLInvocationResultValue result = DSLInvocationResultValue.MODEL_OK;
		
		try {
			ProcessBuilder pb = null;
			if (saasm) {
				pb = new ProcessBuilder("stack", "exec", "resource-dsl", "--" , "example", "location",
					"--dict", "--model", "--saasm", "--init", resourceDigest);
			} else {
				pb = new ProcessBuilder("stack", "exec", "resource-dsl", "--" , "example", "location",
						"--dict", "--model", "--reqs", "--init", resourceDigest);				
			}
			pb.directory(new File(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + DSL_FOLDER));
			pb.inheritIO();
			Process p = pb.start();

			if (p.waitFor(15, TimeUnit.SECONDS)) {
				result = DSLInvocationResultValue.fromValue(p.exitValue());
				if (result == null) {
					//Unexpected return code; error condition
					result = DSLInvocationResultValue.ERROR;
				}
			} else {
				result = DSLInvocationResultValue.ERROR;
			}			
		} catch (IOException | InterruptedException e) {
			result = DSLInvocationResultValue.ERROR;
		}
		
		return result;
	}
	
	private DSLInvocationResultValue generateDslNetworkInput(long bandwidth, int numberClients, double scalingFactor,
			long pliReportRate, long imageReportRate) {

		DSLInvocationResultValue result = DSLInvocationResultValue.MODEL_OK;
		
		String config = "(" + String.valueOf(numberClients) + "," +
				String.valueOf(pliReportRate) + "," +
				String.valueOf(imageReportRate) + "," +
				String.valueOf(scalingFactor) + ")";
		
		try {
			ProcessBuilder pb = new ProcessBuilder("stack", "exec", "resource-dsl", "--" , "example", "network",
								"--dict", "--model", "--reqs", "--init", String.valueOf(bandwidth), 
								"--config", config);
			pb.directory(new File(DAS.getConfigurationValue(DAS.CFG_IMMORTALS_ROOT) + DSL_FOLDER));
			pb.inheritIO();
			Process p = pb.start(); //IOException will be thrown to caller
			
			if (p.waitFor(15, TimeUnit.SECONDS)) {
				result = DSLInvocationResultValue.fromValue(p.exitValue());
				if (result == null) {
					//Unexpected return code; error condition
					result = DSLInvocationResultValue.ERROR;
				}
			} else {
				result = DSLInvocationResultValue.ERROR;
			}
		} catch (IOException | InterruptedException e) {
			result = DSLInvocationResultValue.ERROR;
		}

		return result;
	}
	
	private String generateResourceDigest(List<String> abstractResourceUris) {
		
		String resourceDigest = abstractResourceUris
				.stream().filter(t -> resourceLabels.containsKey(t))
				.map(e -> resourceLabels.get(e))
				.sorted((ResourceLabel r1, ResourceLabel r2) -> r1.rank - r2.rank)
				.map(s -> s.label)
				.collect(Collectors.joining("+"));
		
		return resourceDigest;

	}

	private void buildAtakClient(SourceComposer.ApplicationInstance applicationInstance) throws Exception  {
		
        // Build the application
		applicationInstance.build();

	}
	
	private void adaptControlPoints(FunctionalitySpecification functionalitySpecification, DFU dfu, SourceComposer.ApplicationInstance applicationInstance) throws Exception {
		
        String dfuReference = null;
        Template cpTemplate = null;
        VelocityContext context = null;

		String dfuCanonicalClassName = null;

		// Get the path to the SACommunicationService file
		Path saCommunicationServiceTemplatePath = applicationInstance.getApplicationPath().resolve(SOURCE_TEMPLATE_FOLDER);

		// Get the path to the SACommunicationService file
		Path saCommunicationServiceSourcePath = applicationInstance.getApplicationPath().resolve(CONTROL_POINT_FOLDER);

		Properties p = new Properties();
		p.put("file.resource.loader.path", saCommunicationServiceTemplatePath.toString());
		Velocity.init(p);

        List<ControlPoint> controlPoints = ControlPoints.select(bootstrapUri, functionalitySpecification);
        
        for (ControlPoint cp : controlPoints) {
        	
        	cpTemplate = Velocity.getTemplate(cp.getClassName() + ".java");
        	
        	String declaration, init, cleanup, work;
        	declaration = init = cleanup = work = null;

        	Map<String, String> dfuMethods = null;
        	        	
        	try {
	        	dfuCanonicalClassName = dfu.getClassName().replace('/', '.');

        		dfuReference = "a" + UUID.randomUUID().toString().replace('-', '_');
	        	
	        	dfuMethods = DFULifecycleMethods.select(bootstrapUri, dfu.getUri());

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
				writer = new FileWriter(saCommunicationServiceSourcePath.resolve(cp.getClassName() + ".java").toFile());
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
        
		// Add the new dependencies from CP1 to the application instance
		applicationInstance.addDependency(dfu.getDependencyCoordinate().getGroupId()
				+ ":" + dfu.getDependencyCoordinate().getArtifactId() + ":+");
		
	}
	
	private DFURemediation getDfuRemediation(String resource, String action) {
		
		DFURemediation dfuRemediation = null;
		
		String onProperty = resource;
		String actionToTake = action;
		
		String invokedAspect = null;
		String applicableDataType = null;
		String strategyUri = null;
		
		String priorProperty = null;
		String priorAction = null;
		
		while (invokedAspect == null && actionToTake != null && onProperty != null) { 

			Map<String, String> data = PropertyImpact.select(bootstrapUri, actionToTake, onProperty);
			
			priorAction = actionToTake;
			priorProperty = onProperty;
			
			actionToTake = null;
			onProperty = null;
			
			if (data != null) {
				if (data.get("actionToTake") != null) {
					actionToTake = data.get("actionToTake");					
				}
				
				if (data.get("onProperty") != null) {
					onProperty = data.get("onProperty");
				}
				
				if (data.get("invokedAspect") != null) {
					invokedAspect = data.get("invokedAspect");
					
					if (data.get("applicableDataType") != null) {
						applicableDataType = data.get("applicableDataType");
					}
					
					if (data.get("strategy") != null) {
						strategyUri = data.get("strategy");
					}
				}
			}
		}
		
		if (invokedAspect != null && applicableDataType != null) {
			dfuRemediation = new DFURemediation(invokedAspect, applicableDataType, priorAction, priorProperty, strategyUri);
		}
		
        return dfuRemediation;
	}

	
	private String pushDeploymentModel(String deploymentModelRdf) throws Exception {
		
		String result = null;
		
		Response deploymentGraphUri = repositoryService.path("pushDeploymentModel").request(javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE)
				.post(Entity.entity(deploymentModelRdf, javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE));

		if (deploymentGraphUri.getStatus() != 200) {
			throw new Exception("Unexpected response code from repository service: " + deploymentGraphUri.getStatusInfo());
		} else {
			result = deploymentGraphUri.readEntity(String.class);
			if (result != null && result.trim().length() > 0) {
				result = "<" + SparqlQuery.FUSEKI_DATA_ENDPOINT + result + ">";
			}
		}
		
		return result;

	}
	
	private static void initialize() {

		String byteCodeURI = null;

		//SourceComposer init
		EnvironmentConfiguration.initializeDefaultEnvironmentConfiguration();

		try {
			repositoryService = ClientBuilder.newClient(new ClientConfig()
					.register(JacksonFeature.class))
					.target(REPOSITORY_SERVICE_CONTEXT_ROOT);
	        byteCodeURI = repositoryService.path("bootstrap").request().post(null, String.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
        if (byteCodeURI != null && byteCodeURI.trim().length() > 0) {
        	bootstrapUri = SparqlQuery.FUSEKI_DATA_ENDPOINT + byteCodeURI;
        }
        
    	resourceLabels = new HashMap<String, ResourceLabel>();
    	dfuLabels = new HashMap<String, String>();

    	resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources#BluetoothResource>", 
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources#BluetoothResource>", "Ext.BT", 4));
        resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources#UsbResource>", 
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources#UsbResource>", "Ext.USB", 3));
        resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources#UserInterface>", 
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources#UserInterface>", "UI", 5));
        resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsReceiverEmbedded>", 
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsReceiverEmbedded>", "GPS.Dev", 2));
        resourceLabels.put("<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsSatelliteConstellation>",
        		new ResourceLabel("<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#GpsSatelliteConstellation>", "GPS.SAT", 1));
        
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderManualSimulated", "dead-reckoning");
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderAndroidGpsBuiltIn", "gps-android");
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderBluetoothGpsSimulated", "gps-bluetooth");
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderSaasmSimulated", "gps-saasm");
        dfuLabels.put("mil/darpa/immortals/dfus/location/LocationProviderUsbGpsSimulated", "gps-usb");
	}
	
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

	private static WebTarget repositoryService;
	private static AdaptationManager instance;
	private static String bootstrapUri;
		
	//These are relative to the Immortals root path provided to the DAS as a startup parameter (see DAS.java)
	private static final String SOURCE_TEMPLATE_FOLDER = "src-templates/com/bbn/ataklite/service";
	private static final String CONTROL_POINT_FOLDER = "src/com/bbn/ataklite/service/";
	private static final String DSL_FOLDER = "/dsl/resource-dsl";

	//We can potentially change these so that these can be passed in to the DAS as well
	private static final String REPOSITORY_SERVICE_CONTEXT_ROOT = "http://localhost:9999/immortalsRepositoryService/";	

	private static final String LIFECYCLE_DECLARATION = "declaration";
	private static final String LIFECYCLE_INIT = "<http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#InitializeAspect>";
	private static final String LIFECYCLE_CLEANUP = "<http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#CleanupAspect>";
	private static final String LIFECYCLE_WORK = "<http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#GetCurrentLocationAspect>";
	private static final String SAASM_URI = "<http://darpa.mil/immortals/ontology/r2.0.0/resources/gps/properties#TrustedProperty>";
	
	private static Map<String, ResourceLabel> resourceLabels;
	private static Map<String, String> dfuLabels;

}
