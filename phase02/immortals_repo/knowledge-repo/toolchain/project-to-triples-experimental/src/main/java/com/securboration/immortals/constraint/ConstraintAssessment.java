package com.securboration.immortals.constraint;

import com.securboration.immortals.aframes.AnalysisFrameAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.constraint.*;

import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;
import com.securboration.immortals.ontology.functionality.ConfigurationBinding;
import com.securboration.immortals.ontology.functionality.DesignPattern;
import com.securboration.immortals.ontology.functionality.DfuConfigurationVariable;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureRequest;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureSolution;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.gmei.DeploymentModel;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.soot.CipherTransformPlatform;
import com.securboration.immortals.utility.ConfigurationCheckerRules;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.securboration.immortals.aframes.AnalysisFrameAssessment.getDfuInstanceStringMap;
import static com.securboration.immortals.aframes.AnalysisFrameAssessment.retrieveChosenDfuImpl;
import static com.securboration.immortals.utility.GradleTaskHelper.*;

public class ConstraintAssessment {

    public static void constraintAnalysis(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config) throws Exception {

        String getConstraints = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_impact:  <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                "\n" +
                "select ?constraints ?dataType where {\n" +
                "\t\n" +
                "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                " \n" +
                "\t\t  ?constraints a IMMoRTALS_impact:ProscriptiveCauseEffectAssertion \n" +
                "\t\t ; IMMoRTALS:hasApplicableDataType ?dataType .\n" +
                "\t}\n" +
                "\t\n" +
                "}";

        getConstraints = getConstraints.replace("???GRAPH_NAME???", taskHelper.getGraphName());

        taskHelper.getPw().println("Retrieving constraints using query:\n\n" + getConstraints + "\n\n");

        GradleTaskHelper.AssertableSolutionSet constraints = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getConstraints, constraints);
        if (constraints.getSolutions().size() != 0) {
            // For each constraint found...
            for (GradleTaskHelper.Solution constraint : constraints.getSolutions()) {
                boolean constraintSatisfied = true;
                String constraintUUID = constraint.get("constraints");
                String applicableDataType = constraint.get("dataType");
                taskHelper.getPw().println("Constraint found: ");

                TriplesToPojo.SparqlPojoContext results = taskHelper.getObjectRepresentation(constraintUUID,
                        CONSTRAINT_TYPE, config);
                ProscriptiveCauseEffectAssertion assertion = null;
                for (Map<String, Object> result : results) {
                    assertion = (ProscriptiveCauseEffectAssertion) result.get("obj");
                    taskHelper.getPw().println(result.get("obj$uri") + "\n");

                    Model assertModel = ObjectToTriples.convert(config.getCleanContext(true), assertion);
                    try {
                        taskHelper.getPw().println(OntologyHelper.serializeModel(assertModel, "TURTLE", false));
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }

                // check to see if constraint was already resolved
                String getResolvedConstraintUUIDs = "prefix IMMoRTALS_constraint: <http://darpa.mil/immortals/ontology/r2.0.0/constraint#> \n" +
                        "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                        "\n" +
                        "select ?constraintUUID where {\n" +
                        "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t?report a IMMoRTALS_constraint:ConstraintAssessmentReport\n" +
                        "\t\t; IMMoRTALS:hasConstraintViolations ?violations .\n" +
                        "\t\t\n" +
                        "\t\t?violations IMMoRTALS:hasConstraint ?constraintUUID .\n" +
                        "\t}\n" +
                        "}";
                getResolvedConstraintUUIDs = getResolvedConstraintUUIDs.replace("???GRAPH_NAME???", taskHelper.getGraphName());
                AssertableSolutionSet constraintUUIDSolutions = new AssertableSolutionSet();
                taskHelper.getClient().executeSelectQuery(getResolvedConstraintUUIDs, constraintUUIDSolutions);

                boolean alreadyResolved = false;
                for (Solution constraintUUIDSolution : constraintUUIDSolutions.getSolutions()) {
                    String resolvedConstraintUUID = constraintUUIDSolution.get("constraintUUID");
                    if (resolvedConstraintUUID.equals(constraintUUID)) {
                        //constraint has already been resolved
                        alreadyResolved = true;
                    }
                }

                if (alreadyResolved) {
                    continue;
                }

                // find any data flows that it applies to.
                String getApplicableDataflows = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                        "prefix IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> \n" +
                        "prefix IMMoRTALS_analysis:  <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> \n" +
                        "\n" +
                        "select distinct ?dataFlowEdge ?node2 where {\n" +
                        "\t\n" +
                        "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        " \n" +
                        "\t\t  <???CONSTRAINT???> IMMoRTALS:hasAssertionBindingSites ?bindingSite .\n" +
                        "\t\t  \n" +
                        "\t\t  ?bindingSite IMMoRTALS:hasDest ?dest\n" +
                        "\t\t  ; IMMoRTALS:hasSrc ?src .\n" +
                        "\t\t  \n" +
                        "\t\t  {?dataFlowEdge a* IMMoRTALS_analysis:DataflowEdge} \n" +
                        "\t\t   UNION\n" +
                        "\t\t  {?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge.\n" +
                        "    \t\t?dataFlowEdge a* ?tester }\n" +
                        "    \n" +
                        "          ?dataFlowEdge IMMoRTALS:hasConsumer ?node1\n" +
                        "\t\t  ; IMMoRTALS:hasProducer ?node2 .\n" +
                        "\t\t  \n" +
                        "\t\t  ?node1 IMMoRTALS:hasResourceTemplate ?resource1 .\n" +
                        "\t\t  ?resource1 a* ?dest .\n" +
                        "\t\t   ?node2 IMMoRTALS:hasResourceTemplate ?resource2 .\n" +
                        "\t\t  ?resource2 a* ?src .\n" +
                        "\t\t  " +
                        "\t\t  ?dataFlowEdge IMMoRTALS:hasDataTypeCommunicated <???DATA_TYPE???> ." +
                        "\n" +
                        "\t}\n" +
                        "  \n" +
                        "}";
                getApplicableDataflows = getApplicableDataflows.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                        .replace("???CONSTRAINT???", constraintUUID).replace("???DATA_TYPE???", applicableDataType);
                GradleTaskHelper.AssertableSolutionSet dataFlowEdges = new GradleTaskHelper.AssertableSolutionSet();
                taskHelper.getClient().executeSelectQuery(getApplicableDataflows, dataFlowEdges);

                if (dataFlowEdges.getSolutions().size() != 0) {
                    String getCriterion = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                            "select distinct ?criterion ?criterionRelation ?criterionProperty ?standardType ?violationType where {\n" +
                            "\t\n" +
                            "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                            " \n" +
                            "\t\t  <???CONSTRAINT???> IMMoRTALS:hasCriterion ?criterion\n" +
                            "\t\t  ; IMMoRTALS:hasImpact ?violationImpact .\n" +
                            "\t\t  \n" +
                            "          ?violationImpact IMMoRTALS:hasConstraintViolationType ?violationType .\n" +
                            "\n" +
                            "\t\t  ?criterion IMMoRTALS:hasCriterion ?criterionRelation\n" +
                            "\t\t  ;IMMoRTALS:hasProperty ?criterionProperty .\n" +
                            "\t\t   OPTIONAL {?criterion IMMoRTALS:hasStandardCriterionType ?standardType}\t}\n" +
                            "\t\n" +
                            "}";

                    getCriterion = getCriterion.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???CONSTRAINT???", constraintUUID);
                    GradleTaskHelper.AssertableSolutionSet criterionInfo = new GradleTaskHelper.AssertableSolutionSet();
                    taskHelper.getClient().executeSelectQuery(getCriterion, criterionInfo);

                    for (GradleTaskHelper.Solution criterion : new HashSet<>(criterionInfo.getSolutions())) {
                        boolean criterionSatisfied = false;
                        String criterionUUID = criterion.get("criterion");

                        TriplesToPojo.SparqlPojoContext criterionResults = taskHelper.getObjectRepresentation(criterionUUID, ABSTRACT_PROPERTY_CRIT_TYPE,
                                config);

                        taskHelper.getPw().println("Retrieving criteria for violating constraint using query:\n\n" + getCriterion + "\n\n");
                        taskHelper.getPw().println("Criteria found: ");

                        criterionResults.forEach(solution ->{
                            AbstractPropertyCriterion propertyCriterion = (AbstractPropertyCriterion) solution.get("obj");
                            taskHelper.getPw().println(solution.get("obj$uri") + "\n");
                            Model critModel = ObjectToTriples.convert(config.getCleanContext(true), propertyCriterion);
                            try {
                                taskHelper.getPw().println(OntologyHelper.serializeModel(critModel, "TURTLE", false));
                            } catch (IOException exc) {
                                exc.printStackTrace();
                            }

                        });

                        String criterionRelation = criterion.get("criterionRelation");
                        String criterionProperty = criterion.get("criterionProperty");
                        String standardType = criterion.get("standardType");

                        for (GradleTaskHelper.Solution dataFlowEdge : dataFlowEdges.getSolutions()) {
                            taskHelper.getPw().println("Retrieving data flows that fall under this constraint's authority" +
                                    " using query:\n\n" + getApplicableDataflows + "\n\n");

                            String dataFlowUUID = dataFlowEdge.get("dataFlowEdge");
                            taskHelper.getPw().println("Data flow found: ");
                            TriplesToPojo.SparqlPojoContext edgeResults = taskHelper.getObjectRepresentation(dataFlowUUID, GradleTaskHelper.DATAFLOW_INTER_METHOD_TYPE,
                                    config);


                            for (Map<String, Object> edgeResult : edgeResults) {

                                InterMethodDataflowEdge interEdge = (InterMethodDataflowEdge) edgeResult.get("obj");
                                taskHelper.getPw().println(edgeResult.get("obj$uri") + "\n");
                                Model edgeModel = ObjectToTriples.convert(config.getCleanContext(true), interEdge);
                                try {
                                    taskHelper.getPw().println(OntologyHelper.serializeModel(edgeModel, "TURTLE", false));
                                } catch (IOException exc) {
                                    exc.printStackTrace();
                                }
                            }

                            switch (criterionRelation) {
                                case "PROPERTY_ABSENT":
                                    String getEdgeProperties = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                            "select ?properties where {\n" +
                                            "\t\n" +
                                            "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                                            " \n" +
                                            "\t\t  <???DATA_FLOW???> IMMoRTALS:hasEdgeProperties ?properties .\n" +
                                            "\t}\n" +
                                            "\t\n" +
                                            "}";
                                    getEdgeProperties = getEdgeProperties.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???DATA_FLOW???", dataFlowUUID);
                                    taskHelper.getPw().println("Retrieving current edge properties using query:\n\n" + getEdgeProperties + "\n\n");

                                    GradleTaskHelper.AssertableSolutionSet edgeProperties = new GradleTaskHelper.AssertableSolutionSet();
                                    taskHelper.getClient().executeSelectQuery(getEdgeProperties, edgeProperties);
                                    if (edgeProperties.getSolutions().size() == 0) {
                                        taskHelper.getPw().println("DataflowEdge " + dataFlowUUID + " doesn't have any properties, violates constraint.");
                                        break;
                                    } else {
                                        for (Solution edgeProperty : edgeProperties.getSolutions()) {
                                            String edgePropertyUUID = edgeProperty.get("properties");

                                            String getPropertyClass = "select ?class where { graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                                                    "    <???PROPERTY_INSTANCE???> a ?class .\n" +
                                                    "}}";
                                            getPropertyClass = getPropertyClass.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                                                    .replace("???PROPERTY_INSTANCE???", edgePropertyUUID);
                                            taskHelper.getPw().println("Retrieving property class using query:\n\n" + getPropertyClass + "\n\n");
                                            GradleTaskHelper.AssertableSolutionSet propertyClassSolution = new GradleTaskHelper.AssertableSolutionSet();
                                            taskHelper.getClient().executeSelectQuery(getPropertyClass, propertyClassSolution);

                                            String propertyClass = propertyClassSolution.getSolutions().get(0).get("class");

                                            String getSpecifiedProperty = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                                                    "\n" +
                                                    "ask where { graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                                                    " <???PROPERTY_CLASS???> rdfs:subClassOf*  <???SPECIFIED_PROPERTY???> } }";
                                            getSpecifiedProperty = getSpecifiedProperty.replace("???PROPERTY_CLASS???", propertyClass)
                                                    .replace("???SPECIFIED_PROPERTY???", criterionProperty)
                                                    .replace("???GRAPH_NAME???", taskHelper.getGraphName());
                                            taskHelper.getPw().println("Asking whether the property class is an instance of the specified class using query:\n\n"
                                                    + getSpecifiedProperty + "\n\n");

                                            if (taskHelper.getClient().executeAskQuery(getSpecifiedProperty)) {
                                                if (standardType != null) {
                                                    criterionSatisfied = assertStandards(taskHelper, edgePropertyUUID, standardType);
                                                } else {
                                                    criterionSatisfied = true;
                                                }
                                                break;
                                            } else {
                                                criterionSatisfied = false;
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    System.out.println("Unable to handle specified property relation.");
                                    break;
                            }

                            constraintSatisfied = constraintSatisfied && criterionSatisfied;

                            // If the data flow satisfies the constraint, move on to the next one...
                            if (criterionSatisfied) {
                                taskHelper.getPw().println(dataFlowUUID + " satisfies criterion " + criterionUUID);
                            } else {
                                taskHelper.getPw().println("Edge fails constraint.");
                                satisfyConstraint(taskHelper, dataFlowUUID, assertion, config);
                            }
                        }
                    }

                } else {
                    taskHelper.getPw().println("Constraint has no applicable data flow edges.");
                }
            }
        }

        taskHelper.getPw().println("All constraints have been analyzed.");

        taskHelper.getPw().flush();
        taskHelper.getPw().close();
    }

    private static void satisfyConstraint(GradleTaskHelper taskHelper, String dataFlowUUID, ProscriptiveCauseEffectAssertion constraint,
                                          ObjectToTriplesConfiguration config) throws Exception {

        String constraintUUID = config.getNamingContext().getNameForObject(constraint);
        ConstraintViolation constraintViolation = new ConstraintViolation();
        constraintViolation.setConstraint(constraint);

        TriplesToPojo.SparqlPojoContext dataFlowObject = taskHelper.getObjectRepresentation(dataFlowUUID, DATAFLOW_INTER_METHOD_TYPE,
                config);

        DataflowEdge edge = null;
        for (Map<String, Object> mapping : dataFlowObject) {
            edge = (InterMethodDataflowEdge) mapping.get("obj");
        }

        constraintViolation.setEdgeInViolation(edge);
        // Get the impacts involved in applying this mitigation strategy...
        String getStrategyImpacts = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_cp2: <http://darpa.mil/immortals/ontology/r2.0.0/cp2#>\n" +
                "prefix IMMoRTALS_impact:  <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                "select distinct ?criterion3 ?property1 ?strategy\n" +
                "\t where { \n" +
                "\t graph <http://localhost:3030/ds/data/???GRAPH_NAME???> { \n" +
                " \n" +
                "  <???CONSTRAINT???> IMMoRTALS:hasImpact ?impact1 .\n" +
                "      ?impact1 IMMoRTALS:hasConstraintViolationType ?violation .\n" +
                "\n" +
                "      BIND (  IF ( ?violation = \"HARD_CONSTRAINT_VIOLATION\", \"WHEN_HARD_VIOLATED\", IF (?violation = \"SOFT_CONSTRAINT_VIOLATION\", \"WHEN_SOFT_VIOLATED\", \"\" )) AS ?violationResponse) \n" +
                "  \n" +
                "  ?strategy a IMMoRTALS_impact:PrescriptiveCauseEffectAssertion\n" +
                "  ; IMMoRTALS:hasCriterion ?criterion1 .\n" +
                "  \n" +
                "  ?criterion1 IMMoRTALS:hasConstraint <???CONSTRAINT???>\n" +
                "  ; IMMoRTALS:hasTriggeringConstraintCriterion ?violationResponse .\n" +
                "    \n" +
                "    ?strategy IMMoRTALS:hasImpact ?impact2 .\n" +
                "    ?impact2 IMMoRTALS:hasRemediationStrategy ?strategy2 .\n" +
                "    ?strategy2 IMMoRTALS:hasCriterion ?criterion2\n" +
                "    ; IMMoRTALS:hasImpact ?impact3 .\n" +
                "    \n" +
                "    ?criterion2 IMMoRTALS:hasCriterion ?criterion3\n" +
                "    ; IMMoRTALS:hasProperty ?property1 .\n" +
                "    \n" +
                "    ?impact3 IMMoRTALS:hasImpactOnProperty ?propertyImpact\n" +
                "    ; IMMoRTALS:hasImpactedProperty ?property2 .\n" +
                "  \n" +
                " \n" +
                "}\n" +
                "}";
        getStrategyImpacts = getStrategyImpacts.replace("???CONSTRAINT???", constraintUUID).replace("???GRAPH_NAME???", taskHelper.getGraphName());
        taskHelper.getPw().println("Retrieving impacts of mitigation strategies using query:\n\n" + getStrategyImpacts + "\n\n");
        AssertableSolutionSet strategyImpacts = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getStrategyImpacts, strategyImpacts);

        if (strategyImpacts.getSolutions().size() != 0) {
            for (Solution strategyImpact : strategyImpacts.getSolutions()) {

                String mitigationStrategyUUID = strategyImpact.get("strategy");
                TriplesToPojo.SparqlPojoContext mitigationStrategyPojos = taskHelper.getObjectRepresentation(mitigationStrategyUUID,
                        MITIGATION_STRATEGY_TYPE, config);

                PrescriptiveCauseEffectAssertion mitigationStrategy = null;
                for (Map<String, Object> mitigationStrategyPojo : mitigationStrategyPojos) {
                    mitigationStrategy = (PrescriptiveCauseEffectAssertion) mitigationStrategyPojo.get("obj");
                }
                constraintViolation.setMitigationStrategyUtilized(mitigationStrategy);

                String impactCriterionPropertyUUID = strategyImpact.get("property1");
                TriplesToPojo.SparqlPojoContext propertyObject = taskHelper.getObjectRepresentation(impactCriterionPropertyUUID,
                        PROPERTY_TYPE, config);
                Property property = null;
                for (Map<String, Object> mapping : propertyObject) {
                    property = (Property) mapping.get("obj");
                }

                String impactCriterionType = strategyImpact.get("criterion3");

                PropertyImpact propertyImpact = new PropertyImpact();
                propertyImpact.setImpactOnProperty(PropertyImpactType.ADDS);
                propertyImpact.setImpactedProperty(property.getClass());

                taskHelper.getPw().println("Mitigation strategy has impact: " + propertyImpact.getImpactOnProperty() + ", on property: " +
                        propertyImpact.getImpactedProperty() + "\nAnalyzing how this will affect data flows...");

                switch(impactCriterionType) {
                    case "PROPERTY_ADDED":
                        modifyEdge(edge, property, config, taskHelper);
                        if (property instanceof DataProperty) {
                            // determining the scope of the repair process
                            if (((DataProperty) property).isHidden()) {
                                taskHelper.getPw().println("Adding this property will have inter-process impacts, will have to analyze both consumer and producer" +
                                        " systems...");
                                DesignPattern flowDesign = getDataflowDesignPattern(config.getNamingContext().getNameForObject(edge.getProducer()),
                                        taskHelper);

                                if (flowDesign.equals(DesignPattern.FUNCTIONAL)) {
                                    taskHelper.getPw().println("Functional data flow detected, proceeding with repairs...");
                                } else if (flowDesign.equals(DesignPattern.STREAM)) {
                                    taskHelper.getPw().println("Streaming data flow detected, proceeding with repairs...");
                                }

                                FunctionalAspectInstance[] aspectInstances = taskHelper.getInstancesFromImpactStatement(propertyImpact, config);

                                if (aspectInstances.length == 0) {
                                    taskHelper.getPw().println("Unable to find instance(s) required for repairing system, implement instance and re-run analysis.");
                                    return;
                                }

                                List<AspectConfigureRequest> aspectConfigureRequests = new ArrayList<>();
                                for (FunctionalAspectInstance aspectInstance : aspectInstances) {
                                    aspectConfigureRequests.add(AnalysisFrameAssessment.generateConfigurationRequest(aspectInstance,
                                            taskHelper, config));
                                }

                                for (AspectConfigureRequest configureRequest : aspectConfigureRequests) {
                                    taskHelper.getClient().addToModel(ObjectToTriples.convert(config, configureRequest),
                                            taskHelper.getGraphName());
                                }

                                taskHelper.getClient().addToModel(ObjectToTriples.convert(config, constraintViolation), taskHelper.getGraphName());
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    
    private static boolean checkIfBootstrapClass(String className) throws IOException {
        
        String[] bootstrapJars = System.getProperty("sun.boot.class.path").split(File.pathSeparator);

        for (String bootstrapJar : bootstrapJars) {

            File file = new File(bootstrapJar);

            if (file.exists()) {
                ZipInputStream jar = new ZipInputStream(new FileInputStream(file));

                for (ZipEntry entry = jar.getNextEntry(); entry != null; entry = jar.getNextEntry()) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        String bootstrapClassName = entry.getName().substring(0, entry.getName().indexOf(".class"));
                        
                        if (className.equals(bootstrapClassName)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }


    public static String createAdaptationSurface(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config, Set<File> dependencies) throws Exception {

        ConstraintAssessmentReport assessmentReport = new ConstraintAssessmentReport();
        AspectConfigureSolution chosenSolution = getAspectConfigureSolution(taskHelper);

        List<ConstraintViolation> constraintViolations = retrieveConstraintViolations(taskHelper);
        for (ConstraintViolation constraintViolation : constraintViolations) {

            boolean classIsBootstrap = false;
            DataflowNode producerNode = constraintViolation.getEdgeInViolation().getProducer();
            String transmissionMethodSig = null;
            if (producerNode instanceof InterMethodDataflowNode) {

                InterMethodDataflowNode interProducerNode = (InterMethodDataflowNode) producerNode;

                transmissionMethodSig = getMethodSignatureFromPointer(interProducerNode.getJavaMethodPointer());
                String nodeOwnerName = interProducerNode.getJavaClassName();
                classIsBootstrap = checkIfBootstrapClass(nodeOwnerName);
            }
            
            AnalysisImpact[] analysisImpacts = new AnalysisImpact[2];
            PrescriptiveCauseEffectAssertion causeEffectAssertion = constraintViolation.getMitigationStrategyUtilized();
            ImpactStatement[] impactStatementsArr = causeEffectAssertion.getImpact();
            PropertyImpact propertyImpact = null;
            for (ImpactStatement impactStatement : impactStatementsArr) {
                if (impactStatement instanceof RemediationImpact) {
                    propertyImpact = (PropertyImpact) ((RemediationImpact) impactStatement).getRemediationStrategy().getImpact()[0];
                }
            }
            
            FunctionalAspectInstance aspectInstance = retrieveAspectFromChosenDfu(chosenSolution.getChosenInstance(),
                    propertyImpact);
            
            if (classIsBootstrap) {

                analysisImpacts[0] = AnalysisFrameAssessment.repairProducer(taskHelper, config, (InterMethodDataflowNode)
                                constraintViolation.getEdgeInViolation().getProducer(), dependencies, DesignPattern.STREAM,
                        chosenSolution, aspectInstance);

                propertyImpact.setImpactOnProperty(PropertyImpactType.REMOVES);
                aspectInstance = retrieveAspectFromChosenDfu(chosenSolution.getChosenInstance(),
                        propertyImpact);
                
                analysisImpacts[1] = AnalysisFrameAssessment.repairConsumer(taskHelper, config, (InterMethodDataflowNode)
                                constraintViolation.getEdgeInViolation().getConsumer(), dependencies, DesignPattern.STREAM,
                        aspectInstance, chosenSolution);
            } else {

                String getCipherImpl = retrieveChosenDfuImpl(aspectInstance, taskHelper, config);
                AssertableSolutionSet cipherImplSolutions = new AssertableSolutionSet();
                taskHelper.getClient().executeSelectQuery(getCipherImpl, cipherImplSolutions);
                Map<DfuInstance, Pair<String, String>> dfuInstanceStringMap = getDfuInstanceStringMap(taskHelper,
                        cipherImplSolutions);
                
                DfuInstance chosenInstance = chosenSolution.getChosenInstance();
                Pair<String, String> dfuUUIDToCipherImpl = null;
                for (DfuInstance dfuInstance : dfuInstanceStringMap.keySet()) {
                    if (chosenInstance.getClassPointer().equals(dfuInstance.getClassPointer())) {
                        dfuUUIDToCipherImpl = dfuInstanceStringMap.get(dfuInstance);
                    }
                }
                
                String pathToCipherJar = getCipherJarPath(dfuUUIDToCipherImpl.getLeft(), taskHelper);
                
                DataflowNode consumerNode = constraintViolation.getEdgeInViolation().getConsumer();
                String classToBeExpanded = null;
                if (consumerNode instanceof InterMethodDataflowNode) {
                    InterMethodDataflowNode interConsumerNode = (InterMethodDataflowNode) consumerNode;
                    classToBeExpanded = interConsumerNode.getJavaClassName();
                }
                
                String pathToJarToBeExpanded = getAugmentedJarPath(classToBeExpanded, taskHelper);
                classToBeExpanded = classToBeExpanded.replace("/", ".");
                CipherTransformPlatform cipherTransformPlatform = new CipherTransformPlatform(pathToCipherJar, pathToJarToBeExpanded,
                        Optional.of("C:/BBNImmortals/test.jar"));
                String cipherImpl = dfuUUIDToCipherImpl.getRight();
                cipherTransformPlatform.initializeFieldTransformer(classToBeExpanded, cipherImpl);
                
                transmissionMethodSig = convertToSootSubSignature(transmissionMethodSig);
                
                cipherTransformPlatform.initializeCheckTransformer(classToBeExpanded, cipherImpl, transmissionMethodSig,
                        Arrays.asList(chosenSolution.getConfigurationBindings()));
                cipherTransformPlatform.initializeWrapTransformer(classToBeExpanded, cipherImpl, transmissionMethodSig,
                        "java.io.FileOutputStream");
                //TODO need to infer this through data flows
                
                cipherTransformPlatform.addTransformsToPack();
                cipherTransformPlatform.runTransformers();
            }

            constraintViolation.setAnalysisImpacts(analysisImpacts);
        }

        AnalysisFrameAssessment.performWrapperCodeInsertions(taskHelper, config);

        ConstraintViolation[] constraintViolationsArr = new ConstraintViolation[constraintViolations.size()];
        int i = 0;
        for (ConstraintViolation constraintViolation : constraintViolations) {
            constraintViolationsArr[i] = constraintViolation;
        }
        assessmentReport.setConstraintViolations(constraintViolationsArr);
        taskHelper.getClient().addToModel(ObjectToTriples.convert(config, assessmentReport), taskHelper.getGraphName());
        return config.getNamingContext().getNameForObject(assessmentReport);
    }

    private static String convertToSootSubSignature(String transmissionMethodSig) {
        
        String methodName = transmissionMethodSig.substring(0, transmissionMethodSig.indexOf("("));
        
        String returnType = transmissionMethodSig.substring(transmissionMethodSig.lastIndexOf(")") + 1);
        
        switch (returnType) {
            case "V" :
                returnType = "void";
                break;
            default:
                break;
        }
        
        String paramsString = transmissionMethodSig.substring(transmissionMethodSig.indexOf("(") + 1, transmissionMethodSig.lastIndexOf(")"));
        String[] params = paramsString.split(";");
        paramsString = "";
        
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            if (param.startsWith("L")) {
                param = param.substring(1);
                param = param.replace("/", ".");
                paramsString+=param;
            } else if (param.startsWith("Z")) {
                param = "boolean";
                paramsString+=param;
            }
            
            if (i != params.length - 1) {
                paramsString+=",";
            }
        }
        
        String newSignature = returnType + " " + methodName + "(" + paramsString + ")";
        return newSignature;
    }

    private static String getMethodSignatureFromPointer(String javaMethodPointer) {
        return javaMethodPointer.substring(javaMethodPointer.lastIndexOf("/methods/") + 9);
    }

    private static String getAugmentedJarPath(String classToBeExpanded, GradleTaskHelper taskHelper) {
        
        String classJarPath = null;
        String getClassJarPath =  "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                "\n" +
                "select ?jarPath where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?jar a IMMoRTALS_bytecode:JarArtifact\n" +
                "\t\t; IMMoRTALS:hasFileSystemPath ?jarPath\n" +
                "\t\t; IMMoRTALS:hasJarContents ?classArtifacts .\n" +
                "\t\t\n" +
                "\t\t?classArtifacts IMMoRTALS:hasHash ?dfuHash " +
                "\t\t; IMMoRTALS:hasName \"???CLASS_NAME???\"\n" +
                "\t\t\n" +
                "\t}\n" +
                "}";
        getClassJarPath = getClassJarPath.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???CLASS_NAME???", classToBeExpanded + ".class");
        AssertableSolutionSet classJarPathSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getClassJarPath, classJarPathSolutions);
        
        if (!classJarPathSolutions.getSolutions().isEmpty()) {
            classJarPath = classJarPathSolutions.getSolutions().get(0).get("jarPath");
        }
        
        return classJarPath;
    }

    private static String getCipherJarPath(String dfuUUID, GradleTaskHelper taskHelper) {
        
        String cipherJarPath = null;
        String getCipherJarPath = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                "\n" +
                "select ?jarPath where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?jar a IMMoRTALS_bytecode:JarArtifact\n" +
                "\t\t; IMMoRTALS:hasFileSystemPath ?jarPath\n" +
                "\t\t; IMMoRTALS:hasJarContents ?classArtifacts .\n" +
                "\t\t\n" +
                "\t\t?classArtifacts IMMoRTALS:hasHash ?dfuHash .\n" +
                "\t\t\n" +
                "\t\t<???DFU_INSTANCE???> IMMoRTALS:hasClassPointer ?dfuHash .\n" +
                "\t}\n" +
                "}";
        getCipherJarPath = getCipherJarPath.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???DFU_INSTANCE???", dfuUUID);
        AssertableSolutionSet cipherJarPathSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getCipherJarPath, cipherJarPathSolutions);
        
        if (!cipherJarPathSolutions.getSolutions().isEmpty()) {
            cipherJarPath = cipherJarPathSolutions.getSolutions().get(0).get("jarPath");
        }
        
        return cipherJarPath;
    }

    private static AspectConfigureSolution getAspectConfigureSolution(GradleTaskHelper taskHelper) throws Exception {
        DeploymentModel deploymentModel = retrieveDeploymentModel(taskHelper);
        List<AspectConfigureSolution> configureSolutions = AnalysisFrameAssessment.retrieveConfigurationSolutions(taskHelper);
        AspectConfigureSolution chosenSolution = null;
        for (AspectConfigureSolution configureSolution : configureSolutions) {
            if (ConfigurationCheckerRules.checkConfiguration(deploymentModel, configureSolution) != null) {
                chosenSolution = configureSolution;
                break;
            }
        }
        return chosenSolution;
    }

    private static DeploymentModel retrieveDeploymentModel(GradleTaskHelper taskHelper) throws ClassNotFoundException, NoSuchFieldException, InstantiationException, IllegalAccessException {

        String getDeploymentModel = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                " prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>\n" +
                "\n" +
                "select ?deploymentModel where {\n" +
                "\t\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t\t?deploymentModel a IMMoRTALS_gmei:DeploymentModel .\n" +
                "\t\n" +
                "\t}\n" +
                "}";
        getDeploymentModel = getDeploymentModel.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        AssertableSolutionSet deploymentSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getDeploymentModel, deploymentSolutions);

        if (!deploymentSolutions.getSolutions().isEmpty()) {
            String modelUUID = deploymentSolutions.getSolutions().get(0).get("deploymentModel");
            DeploymentModel deploymentModel = (DeploymentModel) TriplesToPojo.convert(taskHelper.getGraphName(), modelUUID, taskHelper.getClient());
            return deploymentModel;
        }

        return null;
    }

    private static List<ConstraintViolation> retrieveConstraintViolations(GradleTaskHelper taskHelper) throws Exception {
        List<ConstraintViolation> constraintViolations = new ArrayList<>();
        String getConstraintViolations = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_property_impact: <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                "\n" +
                "select ?constraintViolation where {\n" +
                "\t\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?constraintViolation a IMMoRTALS_property_impact:ConstraintViolation .\n" +
                "\t}\n" +
                "}";
        getConstraintViolations = getConstraintViolations.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        AssertableSolutionSet constraintViolationSolutionSet= new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getConstraintViolations, constraintViolationSolutionSet);

        if (!constraintViolationSolutionSet.getSolutions().isEmpty()) {
            List<Solution> constraintViolationSolutions = constraintViolationSolutionSet.getSolutions();
            for (Solution constraintViolationSolution : constraintViolationSolutions) {
                String constraintViolationUUID = constraintViolationSolution.get("constraintViolation");
                constraintViolations.add((ConstraintViolation) TriplesToPojo.convert(taskHelper.getGraphName(), constraintViolationUUID, taskHelper.getClient()));
            }
        }
        return constraintViolations;
    }

    private static FunctionalAspectInstance retrieveAspectFromChosenDfu(DfuInstance chosenInstance, PropertyImpact propertyImpact) throws IllegalAccessException, InstantiationException {

        PropertyImpactType propertyImpactType = propertyImpact.getImpactOnProperty();
        for (FunctionalAspectInstance aspectInstance : chosenInstance.getFunctionalAspects()) {
            FunctionalAspect functionalAspect = aspectInstance.getAbstractAspect().newInstance();
            if (functionalAspect instanceof DefaultAspectBase) {
                for (ImpactStatement impactStatement : functionalAspect.getImpactStatements()) {
                    if (impactStatement instanceof PropertyImpact) {
                        PropertyImpact propertyImpact1 = (PropertyImpact) impactStatement;
                        if (propertyImpact.getImpactedProperty().isInstance(propertyImpact.getImpactedProperty().newInstance()) &&
                                propertyImpact1.getImpactOnProperty().equals(propertyImpactType)) {
                            return aspectInstance;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static DesignPattern getDataflowDesignPattern(String producerNodeUUID, GradleTaskHelper taskHelper) {

        GradleTaskHelper.AssertableSolutionSet traceSolutions = new GradleTaskHelper.AssertableSolutionSet();
        final String getCalledNodes = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> \n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "\n" +
                "select ?nodes ?edge where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t{?edge a*  IMMoRTALS_analysis:DataflowEdge}\n" +
                "\t\tUNION\n" +
                "\t\t{?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge .\n" +
                "\t\t ?edge a* ?tester .}\n" +
                "\t\t ?edge IMMoRTALS:hasConsumer <???NODE???>\n" +
                "\t\t; IMMoRTALS:hasDataTypeCommunicated <???DATA???>" +
                "\t\t; IMMoRTALS:hasProducer ?nodes .\n" +
                "\t}\n" +
                "}";

        taskHelper.getClient().executeSelectQuery(getCalledNodes.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                replace("???NODE???", producerNodeUUID)
                .replace("???DATA???", "http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#OutputStream"), traceSolutions);

        if (traceSolutions.getSolutions().isEmpty()) {
            return DesignPattern.FUNCTIONAL;
        } else {
            return DesignPattern.STREAM;
        }
    }


    private static void modifyEdge(DataflowEdge edge, Property property, ObjectToTriplesConfiguration config, GradleTaskHelper taskHelper) {

        DataflowAnalysisFrame affectedFrame = edge.getDataflowAnalysisFrame();

        List<Property> currentPropsArr = new ArrayList<>();
        if (affectedFrame.getFrameProperties() != null) {
            for (Property currentProperty : affectedFrame.getFrameProperties()) {
                currentPropsArr.add(currentProperty);
            }
        }
        currentPropsArr.add(property);
        affectedFrame.setFrameProperties(currentPropsArr.toArray(new Property[currentPropsArr.size()]));

        currentPropsArr = new ArrayList<>();
        if (edge.getEdgeProperties() != null) {
            for (Property currentProperty : edge.getEdgeProperties()) {
                currentPropsArr.add(currentProperty);
            }
        }
        currentPropsArr.add(property);
        edge.setEdgeProperties(currentPropsArr.toArray(new Property[currentPropsArr.size()]));

        taskHelper.getClient().addToModel(ObjectToTriples.convert(config, edge), taskHelper.getGraphName());
    }

    private static boolean assertStandards(GradleTaskHelper taskHelper, String propertyUUID, String standardType) throws Exception {
        switch (standardType) {
            case "STANDARD_CURRENT_BEST":
                String checkIfCurrentBest = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "select ?standardProps where {\n" +
                        "\t\n" +
                        "    graph <???GRAPH_NAME???> {\n" +
                        " \n" +
                        "\t\t  <???PROPERTY???> IMMoRTALS:hasEncryptionAlgorithm ?algorithm .\n" +
                        "    ?algorithm IMMoRTALS:hasProperties ?standardProps .\n" +
                        "    ?standardProps a IMMoRTALS_functionality:CurrentBestPractice.\n" +
                        "    \n" +
                        "\t}\n" +
                        "\t\n" +
                        "}";
                checkIfCurrentBest = checkIfCurrentBest.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                        .replace("???PROPERTY???", propertyUUID);
                System.out.println("Criterion specifies standardized property; determining what standard the given property is" +
                        "using query " + checkIfCurrentBest);
                AssertableSolutionSet solutionSet = new AssertableSolutionSet();
                taskHelper.getClient().executeSelectQuery(checkIfCurrentBest, solutionSet);

                if (solutionSet.getSolutions().size() != 0) {
                    System.out.println("Property is current best, fulfills constraint.");
                    return true;
                } else {
                    System.out.println("Property is not current best standard, fails constraint.");
                    return false;
                }
            default:
                System.out.println("Unable to handle specified standard type.");
                return false;
        }
    }
}
