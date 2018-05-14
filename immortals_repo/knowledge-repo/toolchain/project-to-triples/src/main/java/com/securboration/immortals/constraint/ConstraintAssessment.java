package com.securboration.immortals.constraint;

import com.securboration.immortals.aframes.AnalysisFrameAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.constraint.*;

import com.securboration.immortals.ontology.functionality.DesignPattern;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.securboration.immortals.utility.GradleTaskHelper.*;

public class ConstraintAssessment {

    public static ConstraintAssessmentReport constraintAnalysis(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config, Set<File> dependencies) throws Exception {

        ConstraintAssessmentReport assessmentReport = new ConstraintAssessmentReport();
        List<ConstraintViolation> constraintViolations = new ArrayList<>();
        
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
                        CONSTRAINT_TYPE,config);
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
                               constraintViolations.add(satisfyConstraint(taskHelper, 
                                       dataFlowUUID, assertion, dependencies, config));
                            }
                        }
                        AnalysisFrameAssessment.performWrapperCodeInsertions(taskHelper, config);
                    }
                    
                } else {
                    taskHelper.getPw().println("Constraint has no applicable data flow edges.");
                }
            }
        }
        
        taskHelper.getPw().println("All constraints have been analyzed.");
        
        taskHelper.getPw().flush();
        taskHelper.getPw().close();
        assessmentReport.setConstraintViolations(constraintViolations.toArray(
                new ConstraintViolation[constraintViolations.size()]));
        return assessmentReport;
    }
    
    private static ConstraintViolation satisfyConstraint(GradleTaskHelper taskHelper, String dataFlowUUID, ProscriptiveCauseEffectAssertion constraint, 
                                                         Set<File> dependencies, ObjectToTriplesConfiguration config) throws Exception {
        
        String constraintUUID = config.getNamingContext().getNameForObject(constraint);
        ConstraintViolation constraintViolation = new ConstraintViolation();
        constraintViolation.setConstraint(constraint);
        
        TriplesToPojo.SparqlPojoContext dataFlowObject = taskHelper.getObjectRepresentation(dataFlowUUID, DATAFLOW_INTER_METHOD_TYPE,
                config);

        DataflowEdge edge = null;
        for (Map<String, Object> mapping : dataFlowObject) {
            edge = (InterMethodDataflowEdge) mapping.get("obj");
        }

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

                AnalysisImpact[] analysisImpacts = new AnalysisImpact[2];
                switch(impactCriterionType) {
                    case "PROPERTY_ADDED":
                        modifyEdge(edge, property, config, taskHelper);
                        if (property instanceof DataProperty) {
                            // determining the scope of the repair process
                            if (((DataProperty) property).isHidden()) {
                                taskHelper.getPw().println("Adding this property will have inter-process impacts, will have to analyze both consumer and producer" +
                                        " systems...");
                                constraintViolation.setScopeOfRepairs(ScopeOfRepairs.INTER_PROCESS);
                                DesignPattern flowDesign = getDataflowDesignPattern(config.getNamingContext().getNameForObject(edge.getProducer()),
                                        taskHelper);
                                
                                if (flowDesign.equals(DesignPattern.FUNCTIONAL)) {
                                    taskHelper.getPw().println("Functional data flow detected, proceeding with repairs...");
                                } else if (flowDesign.equals(DesignPattern.STREAM)) {
                                    taskHelper.getPw().println("Streaming data flow detected, proceeding with repairs...");
                                }
                                
                                analysisImpacts[0] = AnalysisFrameAssessment.repairProducer(taskHelper, config, (InterMethodDataflowNode) edge.getProducer(),
                                        propertyImpact, dependencies, flowDesign);
                                
                                // contains inter-process impact, scope of repair process is large
                                propertyImpact.setImpactOnProperty(PropertyImpactType.REMOVES);
                                taskHelper.getPw().println("Starting repairs on consumer...");
                                analysisImpacts[1] = AnalysisFrameAssessment.repairConsumer(taskHelper, config, (InterMethodDataflowNode) edge.getConsumer(),
                                        propertyImpact, dependencies, flowDesign);
                                
                                constraintViolation.setAnalysisImpacts(analysisImpacts);
                            } else {
                                // doesn't contain inter-process impact, scope of repair process is (relatively) small
                            }

                        }
                        break;
                    default:
                        break;
                }
                return constraintViolation;
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
