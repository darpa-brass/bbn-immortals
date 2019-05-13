package com.securboration.immortals.constraint;

import static com.securboration.immortals.utility.GradleTaskHelper.DATAFLOW_INTER_METHOD_TYPE;
import static com.securboration.immortals.utility.GradleTaskHelper.MITIGATION_STRATEGY_TYPE;
import static com.securboration.immortals.utility.GradleTaskHelper.PROPERTY_TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.soot.DfuDependency;
import com.securboration.immortals.soot.ProjectInfo;
import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.aframes.AnalysisFrameAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.ontology.analysis.DataflowAnalysisFrame;
import com.securboration.immortals.ontology.analysis.DataflowEdge;
import com.securboration.immortals.ontology.analysis.DataflowGraphComponent;
import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.analysis.InterMethodDataflowEdge;
import com.securboration.immortals.ontology.analysis.InterMethodDataflowNode;
import com.securboration.immortals.ontology.analysis.MethodInvocationDataflowEdge;
import com.securboration.immortals.ontology.analysis.MethodInvocationDataflowNode;
import com.securboration.immortals.ontology.constraint.ConstraintAssessmentReport;
import com.securboration.immortals.ontology.constraint.InjectionImpact;
import com.securboration.immortals.ontology.constraint.PropertyImpactType;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;
import com.securboration.immortals.ontology.functionality.DesignPattern;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureRequest;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureSolution;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.gmei.DeploymentModel;
import com.securboration.immortals.ontology.java.project.AnalysisMetrics;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.AbstractDataflowBindingSite;
import com.securboration.immortals.ontology.property.impact.AbstractPropertyCriterion;
import com.securboration.immortals.ontology.property.impact.AbstractResourceBindingSite;
import com.securboration.immortals.ontology.property.impact.AnalysisImpact;
import com.securboration.immortals.ontology.property.impact.AssertionBindingSite;
import com.securboration.immortals.ontology.property.impact.ConstraintViolation;
import com.securboration.immortals.ontology.property.impact.ConstraintViolationCriterion;
import com.securboration.immortals.ontology.property.impact.CriterionStatement;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.PredictiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.PrescriptiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;
import com.securboration.immortals.ontology.property.impact.ProscriptiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.RemediationImpact;
import com.securboration.immortals.ontology.property.impact.ResourceImpact;
import com.securboration.immortals.ontology.property.impact.XmlResourceImpact;
import com.securboration.immortals.ontology.property.impact.StructuredDocumentImpact;
import com.securboration.immortals.ontology.property.impact.StructuredDocumentVersionCriterion;
import com.securboration.immortals.ontology.resources.Device;
import com.securboration.immortals.ontology.resources.Software;
import com.securboration.immortals.ontology.resources.logical.XMLSchema;
import com.securboration.immortals.ontology.resources.xml.StructuredDocument;
import com.securboration.immortals.ontology.resources.xml.XmlDocument;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.soot.SootXmlTransformer;
import com.securboration.immortals.utility.CannedEssSchemaTranslator;
import com.securboration.immortals.utility.ConfigurationCheckerRules;
import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.utility.GradleTaskHelper.AssertableSolutionSet;
import com.securboration.immortals.utility.GradleTaskHelper.Solution;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.util.Chain;
import soot.util.JasminOutputStream;

public class ConstraintAssessment {

    private List<Resource> aspectConfigResources = new ArrayList<>();
    private Set<File> newDependencyFiles = new HashSet<>();
    private Set<File> xsltFileHandles = new HashSet<>();

    public Set<File> getXsltFileHandles() {return xsltFileHandles;}

    public List<Resource> getAspectConfigResources() {return aspectConfigResources;}

    public Set<File> getNewDependencyFiles() {
        return newDependencyFiles;
    }

    public static void architectureAnalysis(GradleTaskHelper taskHelper, int inferenceTriples, int domainTriples, int baseVocabTriples,
                                            ObjectToTriplesConfiguration config) throws ClassNotFoundException, NoSuchFieldException, InstantiationException, IllegalAccessException {

        Long beginTime = System.currentTimeMillis();

        String getApplicationArchitectures = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>\n" +
                "\n" +
                "select ?arch where {\n" +
                "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "     ?arch a IMMoRTALS_gmei:ApplicationArchitecture .\n" +
                "\t \n" +
                "\t}\n" +
                " }";
        getApplicationArchitectures = getApplicationArchitectures.replace("???GRAPH_NAME???", taskHelper.getGraphName());

        GradleTaskHelper.AssertableSolutionSet architectureSolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getApplicationArchitectures, architectureSolutions);

        List<Resource> resources = new ArrayList<>();
        List<Pair<String, ProscriptiveCauseEffectAssertion>> constraintsWithUUID = new ArrayList<>();

        if (architectureSolutions.getSolutions().size() != 0) {
            for (GradleTaskHelper.Solution architectureSolution : architectureSolutions.getSolutions()) {

                String architectureUUID = architectureSolution.get("arch");

                String getArchResources = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "select ?resources where {\n" +
                        "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\n" +
                        "\t<???ARCH_UUID???> IMMoRTALS:hasAvailableResources ?resources .\n" +
                        "\t \n" +
                        "\t}\n" +
                        " }";
                getArchResources = getArchResources.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                        .replace("???ARCH_UUID???", architectureUUID);

                GradleTaskHelper.AssertableSolutionSet resourceSolutions = new GradleTaskHelper.AssertableSolutionSet();
                taskHelper.getClient().executeSelectQuery(getArchResources, resourceSolutions);

                for (GradleTaskHelper.Solution resourceSolution : resourceSolutions.getSolutions()) {

                    String resourceUUID = resourceSolution.get("resources");
                    try {
                        resources.add((Resource) TriplesToPojo.convert(taskHelper.getGraphName(),
                                resourceUUID, taskHelper.getClient()));
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }

                String getArchConstraints = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "select ?constraints where {\n" +
                        "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\n" +
                        "\t<???ARCH_UUID???> IMMoRTALS:hasCauseEffectAssertions ?constraints .\n" +
                        "\t \n" +
                        "\t}\n" +
                        " }";
                getArchConstraints = getArchConstraints.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                        .replace("???ARCH_UUID???", architectureUUID);
                GradleTaskHelper.AssertableSolutionSet constraintSolutions = new GradleTaskHelper.AssertableSolutionSet();
                taskHelper.getClient().executeSelectQuery(getArchConstraints, constraintSolutions);

                for (GradleTaskHelper.Solution constraintSolution : constraintSolutions.getSolutions()) {
                    String constraintUUID = constraintSolution.get("constraints");
                    try {
                        if (!isConstraintAlreadyResolved(taskHelper, constraintUUID)) {
                            constraintsWithUUID.add(new Pair<>(constraintUUID,
                                    (ProscriptiveCauseEffectAssertion) TriplesToPojo.convert(taskHelper.getGraphName(),
                                            constraintUUID, taskHelper.getClient())));
                        }
                    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (Pair<String, ProscriptiveCauseEffectAssertion> constraintWithUUID : constraintsWithUUID) {
                boolean constraintSatisfied = true;

                ProscriptiveCauseEffectAssertion constraint = constraintWithUUID.getRight();
                String constraintUUID = constraintWithUUID.getLeft();
                //TODO first get binding sites
                List<Resource> constrainedResources = new ArrayList<>();
                for (AssertionBindingSite assertionBindingSite : constraint.getAssertionBindingSites()) {
                    if (assertionBindingSite instanceof AbstractResourceBindingSite) {
                        AbstractResourceBindingSite abstractResourceBindingSite = (AbstractResourceBindingSite) assertionBindingSite;
                        for (Resource archResource : resources) {
                            if (abstractResourceBindingSite.getResourceType().isInstance(archResource)) {
                                constrainedResources.add(archResource);
                            }
                        }
                    } else if (assertionBindingSite instanceof AbstractDataflowBindingSite) {
                        AbstractDataflowBindingSite abstractDataflowBindingSite = (AbstractDataflowBindingSite) assertionBindingSite;
                        //TODO
                    }
                }

                //TODO then, get criterion statement
                CriterionStatement constraintCriterion = constraint.getCriterion();

                if (constraintCriterion instanceof StructuredDocumentVersionCriterion) {
                    StructuredDocumentVersionCriterion structuredDocumentVersionCriterion = (StructuredDocumentVersionCriterion) constraintCriterion;

                    switch (structuredDocumentVersionCriterion.getStructuredDocumentCriterionType()) {
                        case VERSION_DIFFERENT:

                            List<Device> devices = new ArrayList<>();
                            for (Resource constrainedResource : constrainedResources) {
                                if (constrainedResource instanceof Device) {
                                    devices.add((Device) constrainedResource);
                                }
                            }

                            boolean versionCollision = false;
                            String currentVersion = null;
                            for (Device device : devices) {
                                for (Resource deviceResource : device.getResources()) {

                                    if (deviceResource instanceof XMLSchema) {
                                        XMLSchema xmlSchema = (XMLSchema) deviceResource;
                                        if (currentVersion != null) {
                                            if (!currentVersion.equals(xmlSchema.getVersion())) {
                                                versionCollision = true;
                                                constraintSatisfied = false;
                                                break;
                                            }
                                        } else {
                                            currentVersion = xmlSchema.getVersion();
                                        }
                                    } else if (deviceResource instanceof Software) {

                                        Software software = (Software) deviceResource;
                                        for (DataType softwareData : software.getDataInSoftware()) {
                                            if (softwareData instanceof XmlDocument) {
                                                XmlDocument xmlDocument = (XmlDocument) softwareData;
                                                if (!xmlDocument.getXmlVersion().equals(currentVersion)) {
                                                    versionCollision = true;
                                                    constraintSatisfied = false;
                                                    break;
                                                }
                                            }
                                        }

                                    }
                                }
                            }

                            if (versionCollision) {
                                //TODO multiple versions of XML found in the same architecture...
                                //satisfyConstraint(taskHelper);
                                ConstraintViolation constraintViolation = new ConstraintViolation();
                                ProscriptiveCauseEffectAssertion dummyConstraint = new ProscriptiveCauseEffectAssertion();
                                config.getNamingContext().setNameForObject(dummyConstraint, constraintUUID);
                                constraintViolation.setConstraint(dummyConstraint);

                                String getStrategyImpacts = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                        "prefix IMMoRTALS_cp2: <http://darpa.mil/immortals/ontology/r2.0.0/cp2#>\n" +
                                        "prefix IMMoRTALS_impact:  <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                                        "select ?strategy\n" +
                                        "\t where { \n" +
                                        "\t graph <http://localhost:3030/ds/data/???GRAPH_NAME???> { \n" +
                                        "  ?strategy a IMMoRTALS_impact:PrescriptiveCauseEffectAssertion .\n" +
                                        "  \n" +
                                        " \n" +
                                        "}\n" +
                                        "}";
                                getStrategyImpacts = getStrategyImpacts.replace("???GRAPH_NAME???", taskHelper.getGraphName());
                                taskHelper.getPw().println("Retrieving impacts of mitigation strategies using query:\n\n" + getStrategyImpacts + "\n\n");
                                AssertableSolutionSet strategySolutions = new AssertableSolutionSet();
                                taskHelper.getClient().executeSelectQuery(getStrategyImpacts, strategySolutions);

                                Optional<Pair<String, PrescriptiveCauseEffectAssertion>> strategyWithUUIDOption = Optional.empty();
                                for (Solution strategySolution : strategySolutions.getSolutions()) {

                                    String strategyUUID = strategySolution.get("strategy");
                                    PrescriptiveCauseEffectAssertion strategy = (PrescriptiveCauseEffectAssertion) TriplesToPojo.convert(taskHelper.getGraphName(),
                                            strategyUUID, taskHelper.getClient());

                                    ConstraintViolationCriterion criterion = (ConstraintViolationCriterion) strategy.getCriterion();
                                    if (compareConstraints(criterion.getConstraint(), constraint)) {
                                        strategyWithUUIDOption = Optional.of(new Pair<>(strategyUUID, strategy));
                                        break;
                                    }
                                }

                                if (strategyWithUUIDOption.isPresent()) {
                                    Pair<String, PrescriptiveCauseEffectAssertion> strategyWithUUID = strategyWithUUIDOption.get();
                                    PrescriptiveCauseEffectAssertion dummyStrategy = new PrescriptiveCauseEffectAssertion();
                                    config.getNamingContext().setNameForObject(dummyStrategy, strategyWithUUID.getLeft());
                                    constraintViolation.setMitigationStrategyUtilized(dummyStrategy);
                                    Model violationModel = ObjectToTriples.convert(config, constraintViolation);
                                    inferenceTriples+=violationModel.getGraph().size();

                                    Long endTime = System.currentTimeMillis() - beginTime;

                                    String getMetrics = "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#>\n" +
                                            "\n" +
                                            "select ?metrics where {\n" +
                                            "\n" +
                                            "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                                            "\t\n" +
                                            "\t\t?metrics a IMMoRTALS_java_project:AnalysisMetrics .\n" +
                                            "\t}\n" +
                                            "\t\n" +
                                            "}\n" +
                                            "\t";
                                    getMetrics = getMetrics.replace("???GRAPH_NAME???", taskHelper.getGraphName());
                                    AssertableSolutionSet metricSolutions = new AssertableSolutionSet();
                                    taskHelper.getClient().executeSelectQuery(getMetrics, metricSolutions);

                                    if (!metricSolutions.getSolutions().isEmpty()) {
                                        String metricUUID = metricSolutions.getSolutions().get(0).get("metrics");
                                        AnalysisMetrics analysisMetrics = new AnalysisMetrics();
                                        analysisMetrics.setIngestExec(endTime);
                                        analysisMetrics.setInferenceTriples(inferenceTriples);
                                        analysisMetrics.setDomainTriples(domainTriples);
                                        analysisMetrics.setBaseVocabTriples(baseVocabTriples);
                                        config.getNamingContext().setNameForObject(analysisMetrics, metricUUID);
                                        Model metricModel = ObjectToTriples.convert(config, analysisMetrics);
                                        taskHelper.getClient().addToModel(metricModel, taskHelper.getGraphName());
                                    }
                                    taskHelper.getClient().addToModel(violationModel, taskHelper.getGraphName());
                                }

                            } else {
                                //TODO all versions of XML found are compliant...
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public static void constraintAnalysis(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config) throws Exception {

        // retrieve all constraints
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

                ////
                DataType dataType = (DataType) TriplesToPojo.convert(taskHelper.getGraphName(),
                        applicableDataType, taskHelper.getClient());
                ////

                taskHelper.getPw().println("Constraint found: ");

                //TriplesToPojo.SparqlPojoContext results = taskHelper.getObjectRepresentation(constraintUUID,
                       // CONSTRAINT_TYPE, config);
                ProscriptiveCauseEffectAssertion assertion = (ProscriptiveCauseEffectAssertion) TriplesToPojo.convert(taskHelper.getGraphName(),
                        constraintUUID, taskHelper.getClient());
               /* for (Map<String, Object> result : results) {
                    assertion = (ProscriptiveCauseEffectAssertion) result.get("obj");
                    taskHelper.getPw().println(result.get("obj$uri") + "\n");

                    Model assertModel = ObjectToTriples.convert(config.getCleanContext(true), assertion);
                    try {
                        taskHelper.getPw().println(OntologyHelper.serializeModel(assertModel, "TURTLE", false));
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }*/

                // check to see if constraint was already resolved
                if (isConstraintAlreadyResolved(taskHelper, constraintUUID)) continue;

                // find any data flows that it applies to.
               /* String getApplicableDataflows = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
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
                taskHelper.getClient().executeSelectQuery(getApplicableDataflows, dataFlowEdges);*/


               ///////////////


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
                        "\n" +
                        "\t}\n" +
                        "  \n" +
                        "}";

                getApplicableDataflows = getApplicableDataflows.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                        .replace("???CONSTRAINT???", constraintUUID);
                GradleTaskHelper.AssertableSolutionSet dataFlowEdges = new GradleTaskHelper.AssertableSolutionSet();
                taskHelper.getClient().executeSelectQuery(getApplicableDataflows, dataFlowEdges);

                List<DataflowEdge> applicableEdges = new ArrayList<>();
                for (Solution dataFlowEdgeSolution : dataFlowEdges.getSolutions()) {

                    String dataFlowUUID = dataFlowEdgeSolution.get("dataFlowEdge");
                    DataflowEdge dataflowEdge = (DataflowEdge) TriplesToPojo.convert(taskHelper.getGraphName(), dataFlowUUID, taskHelper.getClient());

                    if (dataflowEdge instanceof InterMethodDataflowEdge) {

                        InterMethodDataflowEdge interMethodDataflowEdge = (InterMethodDataflowEdge) dataflowEdge;
                        DataflowAnalysisFrame frame = interMethodDataflowEdge.getDataflowAnalysisFrame();

                        while (frame != null) {
                            if (frame.getAnalysisFrameDataType().isInstance(dataType)) {
                                applicableEdges.add(interMethodDataflowEdge);
                             break;
                            }
                            frame = frame.getAnalysisFrameChild();
                        }
                    }
                }

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

                for (DataflowEdge dataflowEdge : applicableEdges) {

                    if (!criterionInfo.getSolutions().isEmpty()) {
                        Solution criterionSolution = criterionInfo.getSolutions().get(0);

                        boolean criterionSatisfied = false;

                        String criterionUUID = criterionSolution.get("criterion");

                        String criterionRelation = criterionSolution.get("criterionRelation");
                        String criterionPropertyUUID = criterionSolution.get("criterionProperty");

                        Property criterionProperty = (Property) TriplesToPojo.convert(taskHelper.getGraphName(),
                                criterionPropertyUUID, taskHelper.getClient());

                        switch (criterionRelation) {

                            case "PROPERTY_ABSENT":

                                if (dataflowEdge.getEdgeProperties() != null) {
                                    for (Property property : dataflowEdge.getEdgeProperties()) {
                                        if (criterionProperty.getClass().isInstance(property)) {
                                            criterionSatisfied = true;
                                        }
                                    }
                                }
                                break;

                            case "PROPERTY_PRESENT":

                                if (dataflowEdge.getEdgeProperties() == null) {
                                    criterionSatisfied = true;
                                } else {
                                    boolean propertyPresent = false;
                                    for (Property property : dataflowEdge.getEdgeProperties()) {
                                        if (criterionProperty.getClass().isInstance(property)) {
                                            propertyPresent = true;
                                        }
                                    }
                                    criterionSatisfied = propertyPresent;
                                }
                                break;
                            default:
                                System.err.println("This should never be called... something is VERY wrong.");
                        }

                        constraintSatisfied = constraintSatisfied && criterionSatisfied;

                        if (!constraintSatisfied) {
                            satisfyConstraint(taskHelper, dataflowEdge, assertion, config);
                        }
                    }
                }

                //////////////////

               /* if (dataFlowEdges.getSolutions().size() != 0) {
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
                }*/
            }
        }

        taskHelper.getPw().println("All constraints have been analyzed.");

        taskHelper.getPw().flush();
        taskHelper.getPw().close();
    }

    private static boolean isConstraintAlreadyResolved(GradleTaskHelper taskHelper, String constraintUUID) {
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
            return true;
        }
        return false;
    }

    private static void satisfyConstraint(GradleTaskHelper taskHelper, DataflowEdge dataflowEdge, ProscriptiveCauseEffectAssertion constraint,
                                          ObjectToTriplesConfiguration config) throws Exception {

        ConstraintViolation constraintViolation = new ConstraintViolation();
        constraintViolation.setConstraint(constraint);
        constraintViolation.setEdgeInViolation(dataflowEdge);

        String getStrategyImpacts = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_cp2: <http://darpa.mil/immortals/ontology/r2.0.0/cp2#>\n" +
                "prefix IMMoRTALS_impact:  <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                "select ?strategy\n" +
                "\t where { \n" +
                "\t graph <http://localhost:3030/ds/data/???GRAPH_NAME???> { \n" +
                "  ?strategy a IMMoRTALS_impact:PrescriptiveCauseEffectAssertion .\n" +
                "  \n" +
                " \n" +
                "}\n" +
                "}";
        getStrategyImpacts = getStrategyImpacts.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        taskHelper.getPw().println("Retrieving impacts of mitigation strategies using query:\n\n" + getStrategyImpacts + "\n\n");
        AssertableSolutionSet strategySolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getStrategyImpacts, strategySolutions);


        Optional<PrescriptiveCauseEffectAssertion> strategyOption = Optional.empty();
        for (Solution strategySolution : strategySolutions.getSolutions()) {

            String strategyUUID = strategySolution.get("strategy");
            PrescriptiveCauseEffectAssertion strategy = (PrescriptiveCauseEffectAssertion) TriplesToPojo.convert(taskHelper.getGraphName(),
                    strategyUUID, taskHelper.getClient());

            ConstraintViolationCriterion criterion = (ConstraintViolationCriterion) strategy.getCriterion();
            if (compareConstraints(criterion.getConstraint(), constraint)) {
                strategyOption = Optional.of(strategy);
                break;
            }
        }

        if (strategyOption.isPresent()) {

            PrescriptiveCauseEffectAssertion strategy = strategyOption.get();
            constraintViolation.setMitigationStrategyUtilized(strategy);

            for (ImpactStatement strategyImpact : strategy.getImpact()) {

                if (strategyImpact instanceof RemediationImpact) {

                    RemediationImpact remediationImpact = (RemediationImpact) strategyImpact;
                    PredictiveCauseEffectAssertion remediationStrategy = remediationImpact.getRemediationStrategy();
                    AbstractPropertyCriterion remediationCriterion = (AbstractPropertyCriterion) remediationStrategy.getCriterion();
                    PropertyImpact propertyImpact = new PropertyImpact();
                    //TODO temp
                    for (ImpactStatement impactStatement : remediationStrategy.getImpact()) {
                        if (impactStatement instanceof PropertyImpact) {
                            propertyImpact.setImpactOnProperty(((PropertyImpact) impactStatement).getImpactOnProperty());
                            break;
                        }
                    }
                    //TODO temp
                    propertyImpact.setImpactedProperty(remediationCriterion.getProperty());
                    Property property = remediationCriterion.getProperty().newInstance();


                    switch (remediationCriterion.getCriterion()) {

                        case PROPERTY_ADDED:

                            modifyEdge(dataflowEdge, property, config, taskHelper);

                            if (property instanceof DataProperty) {
                                // determining the scope of the repair process
                                if (((DataProperty) property).isHidden()) {
                                    taskHelper.getPw().println("Adding this property will have inter-process impacts, will have to analyze both consumer and producer" +
                                            " systems...");
                                    DesignPattern flowDesign = getDataflowDesignPattern(config.getNamingContext().getNameForObject(dataflowEdge.getProducer()),
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

                                    // generate configuration requests for each aspect instance found
                                    List<AspectConfigureRequest> aspectConfigureRequests = new ArrayList<>();
                                    for (FunctionalAspectInstance aspectInstance : aspectInstances) {
                                        aspectConfigureRequests.add(AnalysisFrameAssessment.generateConfigurationRequest(aspectInstance,
                                                taskHelper, config));
                                    }

                                    // add them to the ontology for the DAS to process
                                    for (AspectConfigureRequest configureRequest : aspectConfigureRequests) {
                                        taskHelper.getClient().addToModel(ObjectToTriples.convert(config, configureRequest),
                                                taskHelper.getGraphName());
                                    }

                                    // add constraint violation to ontology should the user decide to create an adaptation surface
                                    taskHelper.getClient().addToModel(ObjectToTriples.convert(config, constraintViolation), taskHelper.getGraphName());
                                }
                            }
                        break;
                    }

                }

            }

        }

    }

    private static boolean compareConstraints(ProscriptiveCauseEffectAssertion constraint1, ProscriptiveCauseEffectAssertion constraint2) {
        if (!constraint1.getHumanReadableDescription().equals(constraint2.getHumanReadableDescription())) {
            return false;
        } else if (!constraint1.getApplicableDataType().equals(constraint2.getApplicableDataType())) {
            return false;
        } else {
            return true;
        }
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

                                // generate configuration requests for each aspect instance found
                                List<AspectConfigureRequest> aspectConfigureRequests = new ArrayList<>();
                                for (FunctionalAspectInstance aspectInstance : aspectInstances) {
                                    aspectConfigureRequests.add(AnalysisFrameAssessment.generateConfigurationRequest(aspectInstance,
                                            taskHelper, config));
                                }

                                // add them to the ontology for the DAS to process
                                for (AspectConfigureRequest configureRequest : aspectConfigureRequests) {
                                    taskHelper.getClient().addToModel(ObjectToTriples.convert(config, configureRequest),
                                            taskHelper.getGraphName());
                                }

                                // add constraint violation to ontology should the user decide to create an adaptation surface
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

    // check if class poised to be expanded belongs to a bootstrap jvm jar
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

    public AnalysisMetrics injectAdaptationSurface(ProjectInfo projectInfo, String xsdTranslationEndpoint, GradleTaskHelper taskHelper,
                                                   int adaptTriples, Long beginTime, ObjectToTriplesConfiguration config) throws Exception {

        ConstraintAssessmentReport assessmentReport = new ConstraintAssessmentReport();

        List<ConstraintViolation> constraintViolations = retrieveConstraintViolations(taskHelper);
        assessmentReport.setConstraintViolations(constraintViolations.toArray(new ConstraintViolation[0]));

        Set<String> modifiedClasses = new HashSet<>();
        Set<String> modifiedMethods = new HashSet<>();
        List<AnalysisImpact> analysisImpacts = new ArrayList<>();
        Set<DfuDependency> newDependencies = new HashSet<>();
        for (ConstraintViolation constraintViolation : constraintViolations) {

            ObjectToTriples.convert(config, constraintViolation);

            PrescriptiveCauseEffectAssertion causeEffectAssertion = constraintViolation.getMitigationStrategyUtilized();
            ImpactStatement[] impactStatementsArr = causeEffectAssertion.getImpact();

            for (ImpactStatement impactStatement : impactStatementsArr) {
                if (impactStatement instanceof RemediationImpact) {

                    RemediationImpact remediationImpact = (RemediationImpact) impactStatement;
                    PredictiveCauseEffectAssertion predictiveCauseEffectAssertion = remediationImpact.getRemediationStrategy();

                    for (ImpactStatement strategyImpact : predictiveCauseEffectAssertion.getImpact()) {
                        if (strategyImpact instanceof StructuredDocumentImpact) {
                            StructuredDocumentImpact structuredDocumentImpact = (StructuredDocumentImpact) strategyImpact;

                            switch (structuredDocumentImpact.getImpactType()) {

                                case FORMAT_CHANGE:
                                    ResourceImpact applicableResourceImpact = structuredDocumentImpact.getApplicableResource();
                                    if (applicableResourceImpact instanceof XmlResourceImpact) {

                                        XmlResourceImpact xmlResourceImpact = (XmlResourceImpact) applicableResourceImpact;
                                        Class<? extends Resource> affectedResource = xmlResourceImpact.getImpactedResource();
                                        String resourceUUID = config.getNamingContext().getNameForObject(affectedResource);
                                        Set<DataflowEdge> dataflowEdges = getEdgesBelongingToResourceInProj(resourceUUID, projectInfo.getProjectUUID(), taskHelper);

                                        switch (xmlResourceImpact.getXmlResourceImpactType()) {

                                            case XML_SCHEMA_CHANGE:
                                                //TODO placeholder...need to get from other group
                                                final boolean shouldMinimizeSchemasDuringTranslationSynthesis = false;//TODO: this should come from a configuration property
                                                Pair<String, String> currentToTargetAndBack = new Pair<>(
                                                        CannedEssSchemaTranslator.translateClientToServer(
                                                            shouldMinimizeSchemasDuringTranslationSynthesis,
                                                            projectInfo.getBaseProjectFile(),
                                                            xsdTranslationEndpoint
                                                            ), 
                                                        CannedEssSchemaTranslator.translateServerToClient(
                                                            shouldMinimizeSchemasDuringTranslationSynthesis,
                                                            xsdTranslationEndpoint,
                                                            projectInfo.getBaseProjectFile()
                                                            )
                                                        );

                                                InjectionImpact injectionImpactOutgoing = new InjectionImpact();
                                                injectionImpactOutgoing.setProjectUUID(projectInfo.getProjectUUID());

                                                String outgoingXslt = currentToTargetAndBack.getLeft();
                                                InjectionSite injectionSiteOutward = getOutgoingInjectionSite(dataflowEdges, injectionImpactOutgoing);
                                                //TODO currently here... just was able to retrieve injectionSiteOutward successfully, continue testing
                                                SootMethod outgoingTransformMethod = SootXmlTransformer.generateXmlTransformMethod(taskHelper, injectionImpactOutgoing,
                                                        projectInfo.getProjectCoordinate(), this, projectInfo.getProjectUUID());
                                                injectionSiteOutward.setProjectBaseDir(projectInfo.getBaseProjectFile());

                                                String xsltFileResourcePath = injectionSiteOutward.getProjectBaseDir().getAbsolutePath() + File.separator;
                                                String identifier = "outgoingXslt-" + injectionSiteOutward.getMethodName() + "-" + injectionSiteOutward.getClassName();
                                                String identifierFileSafe = identifier.replaceAll("[\\\\/:*?\"<>|]", "");
                                                File outgoingXsltFile = new File(xsltFileResourcePath + identifierFileSafe);
                                                boolean fileCreated = outgoingXsltFile.createNewFile();
                                                if (fileCreated) {
                                                    FileUtils.writeStringToFile(outgoingXsltFile, outgoingXslt, Charset.defaultCharset());
                                                }
                                                xsltFileHandles.add(outgoingXsltFile);

                                                DfuDependency xsltRetrieverDependency = SootXmlTransformer.searchForXsltRetriever(taskHelper,
                                                        projectInfo.getProjectUUID(), aspectConfigResources, projectInfo.getProjectDependencies());
                                                newDependencies.add(xsltRetrieverDependency);
                                                newDependencies.addAll(addAllNewDependencies(xsltRetrieverDependency.getDependenciesOfDfu()));

                                                modifiedClasses.add(injectionSiteOutward.getClassName());
                                                modifiedMethods.add(injectionSiteOutward.getMethodName());
                                                injectMethodCallOutgoing(outgoingTransformMethod, injectionSiteOutward, outgoingXslt,
                                                        xsltRetrieverDependency.getInvokedDfuMethod(), identifierFileSafe);
                                                analysisImpacts.add(injectionImpactOutgoing);

                                                InjectionImpact injectionImpactIncoming = new InjectionImpact();
                                                injectionImpactIncoming.setProjectUUID(projectInfo.getProjectUUID());

                                                String incomingXslt = currentToTargetAndBack.getRight();
                                                InjectionSite injectionSiteInward = getIncomingInjectionSite(dataflowEdges, injectionImpactIncoming);
                                                SootMethod incomingTransformMethod = SootXmlTransformer.generateXmlTransformMethod(taskHelper, injectionImpactIncoming,
                                                        projectInfo.getProjectCoordinate(), this, projectInfo.getProjectUUID());
                                                injectionSiteInward.setProjectBaseDir(projectInfo.getBaseProjectFile());

                                                xsltFileResourcePath = injectionSiteInward.getProjectBaseDir().getAbsolutePath() + File.separator;
                                                identifier = "incomingXslt-" + injectionSiteInward.getMethodName() + "-" + injectionSiteInward.getClassName();
                                                identifierFileSafe = identifier.replaceAll("[\\\\/:*?\"<>|]", "");
                                                File incomingXsltFile = new File(xsltFileResourcePath + identifierFileSafe);
                                                fileCreated = incomingXsltFile.createNewFile();
                                                if (fileCreated) {
                                                    FileUtils.writeStringToFile(incomingXsltFile, incomingXslt, Charset.defaultCharset());
                                                }
                                                xsltFileHandles.add(incomingXsltFile);

                                                modifiedClasses.add(injectionSiteInward.getClassName());
                                                modifiedMethods.add(injectionSiteInward.getMethodName());
                                                injectMethodCallIncoming(incomingTransformMethod, injectionSiteInward, incomingXslt,
                                                        xsltRetrieverDependency.getInvokedDfuMethod(), identifierFileSafe);
                                                analysisImpacts.add(injectionImpactIncoming);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    break;
                                default:
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        Long endTime = System.currentTimeMillis() - beginTime;
        assessmentReport.setAnalysisImpacts(analysisImpacts.toArray(new AnalysisImpact[0]));
        Model m = ModelFactory.createDefaultModel();
        m.add(ObjectToTriples.convert(config, assessmentReport));
        adaptTriples+=m.getGraph().size();

        String getMetricUUID = "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#>\n" +
                "\n" +
                "select ?metrics where {\n" +
                "\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t\t?metrics a IMMoRTALS_java_project:AnalysisMetrics .\n" +
                "\t}\n" +
                "\t\n" +
                "}\n" +
                "\t";
        getMetricUUID = getMetricUUID.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        AssertableSolutionSet metricUUIDSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getMetricUUID, metricUUIDSolutions);

        String metricUUID = null;
        if (!metricUUIDSolutions.getSolutions().isEmpty()) {
            metricUUID = metricUUIDSolutions.getSolutions().get(0).get("metrics");
        }

        String getSpecificMetrics = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#>\n" +
                "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "select ?ingestExec ?inferenceTriples ?mineTriples ?mineExec ?bytecodeTriples ?bytecodeExec ?baseVocab ?domainTriples where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t<???ANALYSIS_METRICS???> a IMMoRTALS_java_project:AnalysisMetrics\n" +
                "\t\t; IMMoRTALS:hasIngestExec ?ingestExec\n" +
                "\t\t; IMMoRTALS:hasInferenceTriples ?inferenceTriples\n" +
                "\t\t; IMMoRTALS:hasMineTriples ?mineTriples\n" +
                "\t\t; IMMoRTALS:hasMineExec ?mineExec\n" +
                "\t\t; IMMoRTALS:hasBytecodeTriples ?bytecodeTriples\n" +
                "\t\t; IMMoRTALS:hasBytecodeExec ?bytecodeExec\n" +
                "\t\t; IMMoRTALS:hasBaseVocabTriples ?baseVocab\n" +
                "\t\t; IMMoRTALS:hasDomainTriples ?domainTriples .\n" +
                "\t}\n" +
                "}";
        getSpecificMetrics = getSpecificMetrics.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ANALYSIS_METRICS???", metricUUID);
        AssertableSolutionSet metricSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getSpecificMetrics, metricSolutions);

        AnalysisMetrics analysisMetrics = new AnalysisMetrics();
        if (!metricSolutions.getSolutions().isEmpty()) {

            Long ingestExec = -1L;
            int inferenceTriples = -1;
            int mineTriples = -1;
            Long mineExec = -1L;
            int bytecodeTriples = -1;
            Long bytecodeExec = -1L;
            int baseVocabTriples = -1;
            int domainTriples = -1;
            for (Solution metricSolution : metricSolutions.getSolutions()) {

                if (ingestExec == -1L) {
                    String ingestExecString = metricSolution.get("ingestExec");
                    Long ingestExecTemp = Long.parseLong(ingestExecString);
                    if (ingestExecTemp != 0L) {
                        ingestExec = ingestExecTemp;
                    }
                }

                if (inferenceTriples == -1) {
                    String inferenceTriplesString = metricSolution.get("inferenceTriples");
                    int inferenceTriplesTemp = Integer.parseInt(inferenceTriplesString);
                    if (inferenceTriplesTemp != 0) {
                        inferenceTriples = inferenceTriplesTemp;
                    }
                }

                if (mineTriples == -1) {
                    String mineTriplesString = metricSolution.get("mineTriples");
                    int mineTriplesTemp = Integer.parseInt(mineTriplesString);
                    if (mineTriplesTemp != 0) {
                        mineTriples = mineTriplesTemp;
                    }
                }

                if (mineExec == -1L) {
                    String mineExecString = metricSolution.get("mineExec");
                    Long mineExecTemp = Long.parseLong(mineExecString);
                    if (mineExecTemp != 0L) {
                        mineExec = mineExecTemp;
                    }
                }

                if (bytecodeTriples == -1) {
                    String bytecodeTriplesString = metricSolution.get("bytecodeTriples");
                    int bytecodeTriplesTemp = Integer.parseInt(bytecodeTriplesString);
                    if (bytecodeTriplesTemp != 0) {
                        bytecodeTriples = bytecodeTriplesTemp;
                    }
                }

                if (bytecodeExec == -1L) {
                    String bytecodeExecString = metricSolution.get("bytecodeExec");
                    Long bytecodeExecTemp = Long.parseLong(bytecodeExecString);
                    if (bytecodeExecTemp != 0L) {
                        bytecodeExec = bytecodeExecTemp;
                    }
                }

                if (baseVocabTriples == -1) {
                    String baseVocabTriplesString = metricSolution.get("baseVocab");
                    int baseVocabTriplesTemp = Integer.parseInt(baseVocabTriplesString);
                    if (baseVocabTriplesTemp != 0) {
                        baseVocabTriples = baseVocabTriplesTemp;
                    }
                }

                if (domainTriples == -1) {
                    String domainTriplesString = metricSolution.get("domainTriples");
                    int domainTriplesTemp = Integer.parseInt(domainTriplesString);
                    if (domainTriplesTemp != 0) {
                        domainTriples = domainTriplesTemp;
                    }
                }
            }

            analysisMetrics.setAdaptationTriples(adaptTriples);
            analysisMetrics.setAdaptExec(endTime);
            analysisMetrics.setInferenceTriples(inferenceTriples);
            analysisMetrics.setIngestExec(ingestExec);
            analysisMetrics.setMineTriples(mineTriples);
            analysisMetrics.setMineExec(mineExec);
            analysisMetrics.setDomainTriples(domainTriples);
            analysisMetrics.setBaseVocabTriples(baseVocabTriples);
            analysisMetrics.setBytecodeTriples(bytecodeTriples);
            analysisMetrics.setBytecodeExec(bytecodeExec);

            if (modifiedClasses.isEmpty()) {
                analysisMetrics.setClassesModified(new String[]{});
            } else {
                analysisMetrics.setClassesModified(modifiedClasses.toArray(new String[0]));
            }

            if (modifiedMethods.isEmpty()) {
                analysisMetrics.setMethodsModified(new String[]{});
            } else {
                analysisMetrics.setMethodsModified(modifiedMethods.toArray(new String[0]));
            }

            //TODO need to infer this data somehow... for now just fake it
            analysisMetrics.setNewClasses(new String[]{});
            analysisMetrics.setNewMethods(new String[]{});
            List<BytecodeArtifactCoordinate> coordinates = new ArrayList<>();
            for (DfuDependency dfuDependency : newDependencies) {
                coordinates.add(dfuDependency.getDependencyCoordinate());
            }
            analysisMetrics.setNewDependencies(coordinates.toArray(new BytecodeArtifactCoordinate[0]));
        }

        for (DfuDependency dfuDependency : newDependencies) {
            if (dfuDependency.getDependencyFile() != null) {
                this.newDependencyFiles.add(dfuDependency.getDependencyFile());
            }
        }

        copyReport(taskHelper.getResultsDir(), analysisMetrics);

        taskHelper.getClient().addToModel(m, taskHelper.getGraphName());
        System.out.println(config.getNamingContext().getNameForObject(assessmentReport));
        return analysisMetrics;
    }

    private Collection<? extends DfuDependency> addAllNewDependencies(List<DfuDependency> dfuDependencies) {

        List<DfuDependency> newDependencies = new ArrayList<>();
        for (DfuDependency dfuDependency : dfuDependencies) {
            newDependencies.add(dfuDependency);
            List<DfuDependency> dependencyDependencies = dfuDependency.getDependenciesOfDfu();
            if (!dependencyDependencies.isEmpty()) {
                newDependencies.addAll(addAllNewDependencies(dependencyDependencies));
            }
        }

        return newDependencies;
    }

    private void copyReport(String pluginOutput, AnalysisMetrics analysisMetrics) throws IOException {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("AnalysisReport.txt");
        if (is == null) {
            throw new RuntimeException("ERROR LOADING SOOT RESOURCES");
        }

        File sootResources = new File(pluginOutput + File.separator + "AnalysisReport.txt");
        sootResources.createNewFile();

        try (FileOutputStream out = new FileOutputStream(sootResources)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        String reportString = FileUtils.readFileToString(sootResources);
        StringBuilder sb = new StringBuilder();

        sb.append(analysisMetrics.getBaseVocabTriples());
        addWhiteSpace(sb, 16);
        reportString = reportString.replace("???BASE_VOCAB???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getAdaptationTriples());
        addWhiteSpace(sb, 16);
        reportString = reportString.replace("???ADAPTATION???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getDomainTriples());
        addWhiteSpace(sb, 12);
        reportString = reportString.replace("???DOMAIN???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getBytecodeTriples());
        addWhiteSpace(sb, 14);
        reportString = reportString.replace("???BYTECODE???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getMineTriples());
        addWhiteSpace(sb, 10);
        reportString = reportString.replace("???MINE???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getInferenceTriples());
        addWhiteSpace(sb, 15);
        reportString = reportString.replace("???INFERENCE???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getBytecodeExec());
        addWhiteSpace(sb, 19);
        reportString = reportString.replace("???BYTECODE_TIME???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getMineExec());
        addWhiteSpace(sb, 15);
        reportString = reportString.replace("???MINE_TIME???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getIngestExec());
        addWhiteSpace(sb, 17);
        reportString = reportString.replace("???INGEST_TIME???", sb.toString());

        sb = new StringBuilder();
        sb.append(analysisMetrics.getAdaptExec());
        addWhiteSpace(sb, 16);
        reportString = reportString.replace("???ADAPT_TIME???", sb.toString());

        sb = new StringBuilder();
        for (String modifiedClass : analysisMetrics.getClassesModified()) {
            sb.append(modifiedClass + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        addWhiteSpace(sb, 20);
        reportString = reportString.replace("???MODDED_CLASSES???", sb.toString());

        sb = new StringBuilder();
        for (String modifiedMethod : analysisMetrics.getMethodsModified()) {
            sb.append(modifiedMethod + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        addWhiteSpace(sb, 20);
        reportString = reportString.replace("???MODDED_METHODS???", sb.toString());

        sb = new StringBuilder();
        for (String generatedClass : analysisMetrics.getNewClasses()) {
            sb.append(generatedClass + ",");
        }
        addWhiteSpace(sb, 17);
        reportString = reportString.replace("???GEN_CLASSES???", sb.toString());

        sb = new StringBuilder();
        for (String generatedMethod : analysisMetrics.getNewMethods()) {
            sb.append(generatedMethod + ",");
        }
        addWhiteSpace(sb, 17);
        reportString = reportString.replace("???GEN_METHODS???", sb.toString());

        sb = new StringBuilder();
        for (BytecodeArtifactCoordinate newDependency : analysisMetrics.getNewDependencies()) {
            sb.append(newDependency.getGroupId() + ":"+ newDependency.getArtifactId() + ":" + newDependency.getVersion() + ",");
        }
        addWhiteSpace(sb, 18);
        reportString = reportString.replace("???DEPENDENCIES???", sb.toString());

        FileUtils.writeStringToFile(sootResources, reportString);
    }

    private void addWhiteSpace(StringBuilder sb, int lengthOfWildCard) {
        if (sb.length() <= lengthOfWildCard) {
            int numOfWhiteSpace = lengthOfWildCard - sb.length();
            while (numOfWhiteSpace > 0) {
                sb.append(" ");
                numOfWhiteSpace--;
            }
        } else {
            //TODO
        }
    }

    private static void injectMethodCallOutgoing(SootMethod outgoingTransformMethod, InjectionSite injectionSiteOutward, String outgoingXslt,
                                                 SootMethod xsltRetriever, String identifierFileSafe) throws IOException {

        SootClass classToInjectInto = Scene.v().loadClassAndSupport(injectionSiteOutward.getClassName());
        Scene.v().loadNecessaryClasses();
        for (SootMethod sootMethod : classToInjectInto.getMethods()) {

            Body methodBody = sootMethod.retrieveActiveBody();
            Chain<Unit> units = methodBody.getUnits();
            for (Unit unit : units) {
                for (Tag tag : unit.getTags()) {
                    if (tag instanceof LineNumberTag) {
                        LineNumberTag lineNumberTag = (LineNumberTag) tag;
                        if (lineNumberTag.getLineNumber() == injectionSiteOutward.getLineNumber()) {

                            if (unit instanceof AssignStmt) {

                                Value rightValue = ((AssignStmt) unit).getRightOp();
                                Value leftValue = ((AssignStmt) unit).getLeftOp();

                                //TODO need to implement special invoke here, not virtual
                                if (rightValue instanceof VirtualInvokeExpr) {
                                    VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) rightValue;

                                    if (virtualInvokeExpr.getMethod().getName().equals(injectionSiteOutward.getMethodName())) {
                                        //TODO need to make sure param and return types are compatible somehow
                                        Type type = virtualInvokeExpr.getType();
                                        if (!type.equals(outgoingTransformMethod.getReturnType())) {
                                            throw new RuntimeException("INCOMPATIBLE TYPES...");
                                        }

                                        Local localOfInterest = null;
                                        if (leftValue instanceof Local) {
                                            localOfInterest = (Local) leftValue;
                                        }

                                        Local xsltFileLocal = Jimple.v().newLocal("xsltFileName", Scene.v().getType("java.lang.String"));
                                        methodBody.getLocals().add(xsltFileLocal);
                                        AssignStmt assignStmt = Jimple.v().newAssignStmt(xsltFileLocal, StringConstant.v(identifierFileSafe));
                                        units.insertBefore(assignStmt, unit);
                                        StaticInvokeExpr invokeFileReader = Jimple.v().newStaticInvokeExpr(xsltRetriever.makeRef(), xsltFileLocal);

                                        Local xsltLocal = Jimple.v().newLocal("xsltLocal", Scene.v().getType("java.lang.String"));
                                        methodBody.getLocals().add(xsltLocal);
                                        AssignStmt assignXslt = Jimple.v().newAssignStmt(xsltLocal, invokeFileReader);
                                        units.insertAfter(assignXslt, assignStmt);
                                        //TODO

                                        //classToInjectInto.addMethod(outgoingTransformMethod);
                                        StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(outgoingTransformMethod.makeRef());
                                        staticInvokeExpr.setArg(0, xsltLocal);
                                        staticInvokeExpr.setArg(1, localOfInterest);
                                        Local tempLocal = Jimple.v().newLocal("tempLocal", outgoingTransformMethod.getReturnType());
                                        methodBody.getLocals().add(tempLocal);
                                        AssignStmt newAssign = Jimple.v().newAssignStmt(tempLocal, staticInvokeExpr);
                                        units.insertAfter(newAssign, unit);
                                        AssignStmt assignToOldLocal = Jimple.v().newAssignStmt(localOfInterest, tempLocal);
                                        units.insertAfter(assignToOldLocal, newAssign);
                                        produceWrapperClassFile(classToInjectInto);
                                        return;
                                    }
                                } else if (rightValue instanceof StaticInvokeExpr) {

                                } else if (rightValue instanceof SpecialInvokeExpr) {
                                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;

                                    if (specialInvokeExpr.getMethod().getName().equals(injectionSiteOutward.getMethodName())) {
                                        Type type = specialInvokeExpr.getType();
                                        if (!type.equals(outgoingTransformMethod.getReturnType())) {
                                            throw new RuntimeException("INCOMPATIBLE TYPES...");
                                        }

                                        Local localOfInterest = null;
                                        if (leftValue instanceof Local) {
                                            localOfInterest = (Local) leftValue;
                                        }

                                        Local xsltFileLocal = Jimple.v().newLocal("xsltFileName", Scene.v().getType("java.lang.String"));
                                        methodBody.getLocals().add(xsltFileLocal);
                                        AssignStmt assignStmt = Jimple.v().newAssignStmt(xsltFileLocal, StringConstant.v(identifierFileSafe));
                                        units.insertBefore(assignStmt, unit);
                                        StaticInvokeExpr invokeFileReader = Jimple.v().newStaticInvokeExpr(xsltRetriever.makeRef(), xsltFileLocal);

                                        Local xsltLocal = Jimple.v().newLocal("xsltLocal", Scene.v().getType("java.lang.String"));
                                        methodBody.getLocals().add(xsltLocal);
                                        AssignStmt assignXslt = Jimple.v().newAssignStmt(xsltLocal, invokeFileReader);
                                        units.insertAfter(assignXslt, assignStmt);
                                        //TODO

                                        StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(outgoingTransformMethod.makeRef(), xsltLocal, localOfInterest);
                                        Local tempLocal = Jimple.v().newLocal("tempLocal", outgoingTransformMethod.getReturnType());
                                        methodBody.getLocals().add(tempLocal);
                                        AssignStmt newAssign = Jimple.v().newAssignStmt(tempLocal, staticInvokeExpr);
                                        units.insertAfter(newAssign, unit);
                                        AssignStmt assignToOldLocal = Jimple.v().newAssignStmt(localOfInterest, tempLocal);
                                        units.insertAfter(assignToOldLocal, newAssign);
                                        produceWrapperClassFile(classToInjectInto);
                                        return;
                                    }
                                } else if (rightValue instanceof Constant) {
                                    Constant constant = (Constant) rightValue;
                                    Type type = constant.getType();
                                    if (!type.equals(outgoingTransformMethod.getReturnType())) {
                                        throw new RuntimeException("INCOMPATIBLE TYPES...");
                                    }

                                    Local localOfInterest = null;
                                    if (leftValue instanceof Local) {
                                        localOfInterest = (Local) leftValue;
                                    }

                                    Local xsltFileLocal = Jimple.v().newLocal("xsltFileName", Scene.v().getType("java.lang.String"));
                                    methodBody.getLocals().add(xsltFileLocal);
                                    AssignStmt assignStmt = Jimple.v().newAssignStmt(xsltFileLocal, StringConstant.v(identifierFileSafe));
                                    units.insertBefore(assignStmt, unit);
                                    StaticInvokeExpr invokeFileReader = Jimple.v().newStaticInvokeExpr(xsltRetriever.makeRef(), xsltFileLocal);

                                    Local xsltLocal = Jimple.v().newLocal("xsltLocal", Scene.v().getType("java.lang.String"));
                                    methodBody.getLocals().add(xsltLocal);
                                    AssignStmt assignXslt = Jimple.v().newAssignStmt(xsltLocal, invokeFileReader);
                                    units.insertAfter(assignXslt, assignStmt);

                                    StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(outgoingTransformMethod.makeRef(), xsltLocal, localOfInterest);
                                    Local tempLocal = Jimple.v().newLocal("tempLocal", outgoingTransformMethod.getReturnType());
                                    methodBody.getLocals().add(tempLocal);
                                    AssignStmt newAssign = Jimple.v().newAssignStmt(tempLocal, staticInvokeExpr);
                                    units.insertAfter(newAssign, unit);
                                    AssignStmt assignToOldLocal = Jimple.v().newAssignStmt(localOfInterest, tempLocal);
                                    units.insertAfter(assignToOldLocal, newAssign);
                                    produceWrapperClassFile(classToInjectInto);
                                    return;
                                }
                            }

                            if (unit instanceof VirtualInvokeExpr) {

                                VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) unit;

                                if (virtualInvokeExpr.getMethod().getName().equals(injectionSiteOutward.getMethodName())) {
                                    //TODO need to make sure param and return types are compatible somehow
                                    Type type = virtualInvokeExpr.getType();
                                    if (!type.equals(outgoingTransformMethod.getReturnType())) {
                                        throw new RuntimeException("INCOMPATIBLE TYPES...");
                                    }

                                    classToInjectInto.addMethod(outgoingTransformMethod);
                                    StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(outgoingTransformMethod.makeRef());
                                    staticInvokeExpr.setArg(0, virtualInvokeExpr);
                                    InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(staticInvokeExpr);
                                    units.insertBefore(invokeStmt, unit);
                                    produceWrapperClassFile(classToInjectInto);
                                    return;
                                }
                            } else if (unit instanceof StaticInvokeExpr) {

                            } else if (unit instanceof SpecialInvokeExpr) {

                            } else if (unit instanceof InvokeStmt) {
                                InvokeStmt invokeStmt = (InvokeStmt) unit;
                                InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();

                                if (invokeExpr instanceof VirtualInvokeExpr) {

                                } else if (unit instanceof StaticInvokeExpr) {

                                } else if (unit instanceof SpecialInvokeExpr) {

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void injectMethodCallIncoming(SootMethod incomingTransformMethod, InjectionSite injectionSiteInward, String incomingXslt,
                                                 SootMethod xsltRetriever, String identifierFileSafe) throws IOException, ClassNotFoundException {

        SootClass classToInjectInto = Scene.v().loadClassAndSupport(injectionSiteInward.getClassName());
        for (SootMethod sootMethod : classToInjectInto.getMethods()) {
            Body methodBody = sootMethod.retrieveActiveBody();
            Chain<Unit> units = methodBody.getUnits();
            Iterator<Unit> unitIter = units.iterator();
            while (unitIter.hasNext()) {
                Unit unit = unitIter.next();
                for (Tag tag : unit.getTags()) {
                    if (tag instanceof LineNumberTag) {
                        LineNumberTag lineNumberTag = (LineNumberTag) tag;
                        if (lineNumberTag.getLineNumber() == injectionSiteInward.getLineNumber()) {

                            if (unit instanceof AssignStmt) {

                                Value rightValue = ((AssignStmt) unit).getRightOp();
                                Value leftValue = ((AssignStmt) unit).getLeftOp();

                                if (rightValue instanceof VirtualInvokeExpr) {
                                    VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) rightValue;

                                    if (virtualInvokeExpr.getMethod().getName().equals(injectionSiteInward.getMethodName())) {
                                        //TODO need to make sure param and return types are compatible somehow
                                        Type type = virtualInvokeExpr.getType();
                                        if (!type.equals(incomingTransformMethod.getReturnType())) {
                                            throw new RuntimeException("INCOMPATIBLE TYPES...");
                                        }

                                        Local localOfInterest = null;
                                        if (leftValue instanceof Local) {
                                            localOfInterest = (Local) leftValue;
                                        }

                                        Local xsltFileLocal = Jimple.v().newLocal("xsltFileName", Scene.v().getType("java.lang.String"));
                                        methodBody.getLocals().add(xsltFileLocal);
                                        AssignStmt assignStmt = Jimple.v().newAssignStmt(xsltFileLocal, StringConstant.v(identifierFileSafe));
                                        units.insertAfter(assignStmt, unit);
                                        StaticInvokeExpr invokeFileReader = Jimple.v().newStaticInvokeExpr(xsltRetriever.makeRef(), xsltFileLocal);
                                        //TODO

                                        Local xsltLocal = Jimple.v().newLocal("xsltLocal", Scene.v().getType("java.lang.String"));
                                        methodBody.getLocals().add(xsltLocal);
                                        AssignStmt assignXslt = Jimple.v().newAssignStmt(xsltLocal, invokeFileReader);
                                        units.insertAfter(assignXslt, assignStmt);

                                        //classToInjectInto.addMethod(outgoingTransformMethod);
                                        StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(incomingTransformMethod.makeRef());
                                        staticInvokeExpr.setArg(0, xsltLocal);
                                        staticInvokeExpr.setArg(1, localOfInterest);
                                        Local tempLocal = Jimple.v().newLocal("tempLocal", incomingTransformMethod.getReturnType());
                                        methodBody.getLocals().add(tempLocal);
                                        AssignStmt newAssign = Jimple.v().newAssignStmt(tempLocal, staticInvokeExpr);
                                        units.insertAfter(newAssign, assignXslt);
                                        AssignStmt assignToOldLocal = Jimple.v().newAssignStmt(localOfInterest, tempLocal);
                                        units.insertAfter(assignToOldLocal, newAssign);
                                        produceWrapperClassFile(classToInjectInto);
                                        return;
                                    }
                                } else if (rightValue instanceof StaticInvokeExpr) {

                                } else if (rightValue instanceof SpecialInvokeExpr) {
                                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) rightValue;

                                    if (specialInvokeExpr.getMethod().getName().equals(injectionSiteInward.getMethodName())) {
                                        Type type = specialInvokeExpr.getType();
                                        if (!type.equals(incomingTransformMethod.getReturnType())) {

                                            Class<?> existingReturn = Class.forName(type.toString());
                                            Class<?> newReturn = Class.forName(incomingTransformMethod.getReturnType().toString());

                                            if (!existingReturn.isAssignableFrom(newReturn)) {
                                                throw new RuntimeException("INCOMPATIBLE TYPES...");
                                            }
                                        }

                                        Local localOfInterest = null;
                                        if (leftValue instanceof Local) {
                                            localOfInterest = (Local) leftValue;
                                        }

                                        Local xsltFileLocal = Jimple.v().newLocal("xsltFileName", Scene.v().getType("java.lang.String"));
                                        methodBody.getLocals().add(xsltFileLocal);
                                        AssignStmt assignStmt = Jimple.v().newAssignStmt(xsltFileLocal, StringConstant.v(identifierFileSafe));
                                        units.insertAfter(assignStmt, unit);
                                        StaticInvokeExpr invokeFileReader = Jimple.v().newStaticInvokeExpr(xsltRetriever.makeRef(), xsltFileLocal);
                                        //TODO

                                        Local xsltLocal = Jimple.v().newLocal("xsltLocal", Scene.v().getType("java.lang.String"));
                                        methodBody.getLocals().add(xsltLocal);
                                        AssignStmt assignXslt = Jimple.v().newAssignStmt(xsltLocal, invokeFileReader);
                                        units.insertAfter(assignXslt, assignStmt);

                                        StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(incomingTransformMethod.makeRef(), xsltLocal, localOfInterest);
                                        Local tempLocal = Jimple.v().newLocal("tempLocal", incomingTransformMethod.getReturnType());
                                        methodBody.getLocals().add(tempLocal);
                                        AssignStmt newAssign = Jimple.v().newAssignStmt(tempLocal, staticInvokeExpr);
                                        units.insertAfter(newAssign, assignXslt);
                                        AssignStmt assignToOldLocal = Jimple.v().newAssignStmt(localOfInterest, tempLocal);
                                        units.insertAfter(assignToOldLocal, newAssign);
                                        produceWrapperClassFile(classToInjectInto);
                                        return;
                                    }
                                } else if (rightValue instanceof NewExpr) {

                                    NewExpr newExpr = (NewExpr) rightValue;
                                    //new expressions always have an invoke after them...trying to split the two won't end well
                                    unit = unitIter.next();

                                    if (!injectionSiteInward.getMethodName().equals("<init>")) {
                                        throw new RuntimeException("Soot expects constructor call, but injection site says otherwise");
                                    }

                                    Type type = newExpr.getType();
                                    if (!type.equals(incomingTransformMethod.getReturnType())) {
                                        throw new RuntimeException("INCOMPATIBLE TYPES...");
                                    }

                                    Local localOfInterest = null;
                                    if (leftValue instanceof Local) {
                                        localOfInterest = (Local) leftValue;
                                    }

                                    Local xsltFileLocal = Jimple.v().newLocal("xsltFileName", Scene.v().getType("java.lang.String"));
                                    methodBody.getLocals().add(xsltFileLocal);
                                    AssignStmt assignStmt = Jimple.v().newAssignStmt(xsltFileLocal, StringConstant.v(identifierFileSafe));
                                    units.insertAfter(assignStmt, unit);
                                    StaticInvokeExpr invokeFileReader = Jimple.v().newStaticInvokeExpr(xsltRetriever.makeRef(), xsltFileLocal);
                                    //TODO

                                    Local xsltLocal = Jimple.v().newLocal("xsltLocal", Scene.v().getType("java.lang.String"));
                                    methodBody.getLocals().add(xsltLocal);
                                    AssignStmt assignXslt = Jimple.v().newAssignStmt(xsltLocal, invokeFileReader);
                                    units.insertAfter(assignXslt, assignStmt);

                                    StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(incomingTransformMethod.makeRef(), xsltLocal, localOfInterest);
                                    Local tempLocal = Jimple.v().newLocal("tempLocal", incomingTransformMethod.getReturnType());
                                    methodBody.getLocals().add(tempLocal);
                                    AssignStmt newAssign = Jimple.v().newAssignStmt(tempLocal, staticInvokeExpr);
                                    units.insertAfter(newAssign, assignXslt);
                                    AssignStmt assignToOldLocal = Jimple.v().newAssignStmt(localOfInterest, tempLocal);
                                    units.insertAfter(assignToOldLocal, newAssign);
                                    produceWrapperClassFile(classToInjectInto);
                                    return;
                                }
                            }

                            if (unit instanceof VirtualInvokeExpr) {

                                VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) unit;

                                if (virtualInvokeExpr.getMethod().getName().equals(injectionSiteInward.getMethodName())) {
                                    //TODO need to make sure param and return types are compatible somehow
                                    Type type = virtualInvokeExpr.getType();
                                    if (!type.equals(incomingTransformMethod.getReturnType())) {
                                        throw new RuntimeException("INCOMPATIBLE TYPES...");
                                    }

                                    classToInjectInto.addMethod(incomingTransformMethod);
                                    StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(incomingTransformMethod.makeRef());
                                    staticInvokeExpr.setArg(0, virtualInvokeExpr);
                                    InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(staticInvokeExpr);
                                    units.insertBefore(invokeStmt, unit);
                                }
                            } else if (unit instanceof StaticInvokeExpr) {

                            } else if (unit instanceof SpecialInvokeExpr) {

                            } else if (unit instanceof InvokeStmt) {
                                InvokeStmt invokeStmt = (InvokeStmt) unit;
                                InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();

                                if (invokeExpr instanceof VirtualInvokeExpr) {

                                } else if (unit instanceof StaticInvokeExpr) {

                                } else if (unit instanceof SpecialInvokeExpr) {

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static String produceWrapperClassFile(SootClass classToProduce) throws IOException {

        classToProduce.setApplicationClass();

        for (SootMethod sootMethod : classToProduce.getMethods()) {
            sootMethod.retrieveActiveBody();
        }

        //PackManager.v().writeOutput();
        String fileName = SourceLocator.v().getFileNameFor(classToProduce, Options.output_format_class);
        (new File(fileName)).getParentFile().mkdirs();
        File classFile = new File(fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(classFile.getAbsolutePath());
            OutputStream streamOut = new JasminOutputStream(fileOutputStream);
            PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
            JasminClass jasminClass = new JasminClass(classToProduce);
            jasminClass.print(writerOut);
            writerOut.flush();
            streamOut.close();

            return fileName.replace("\\", "/");
        } catch (FileNotFoundException exc) {
            exc.printStackTrace();
        }
        return null;
    }

    private static InjectionSite getIncomingInjectionSite(Set<DataflowEdge> dataflowEdges, InjectionImpact injectionImpact) {

        List<DataflowEdge> tempEdges = new ArrayList<>(dataflowEdges);
        DataflowEdge starterEdge = null;
        for (int i = 0; i < tempEdges.size(); i++) {
            DataflowEdge dataflowEdge = tempEdges.get(i);

            if (dataflowEdge.getProducer() != null && dataflowEdge.getConsumer() != null) {
                starterEdge = tempEdges.remove(i);
                break;
            }
        }
        DataflowNode starterProducer = starterEdge.getProducer();
        DataflowNode starterConsumer = starterEdge.getConsumer();

        List<List<DataflowGraphComponent>> linearFlows = new ArrayList<>();
        while (!tempEdges.isEmpty()) {
            List<DataflowGraphComponent> linearFlow = new ArrayList<>();

            linearFlow.add(starterProducer);
            linearFlow.add(starterEdge);
            linearFlow.add(starterConsumer);

            while ((linearFlow.get(0) != null) || (linearFlow.get(linearFlow.size() - 1) != null)) {
                for (DataflowEdge dataflowEdge : tempEdges) {

                    DataflowNode firstNode = (DataflowNode) linearFlow.get(0);
                    DataflowNode lastNode = (DataflowNode) linearFlow.get(linearFlow.size() - 1);

                    if (firstNode != null) {
                        if ((dataflowEdge.getConsumer() != null) && (compareMethodNodes(dataflowEdge.getConsumer(), firstNode))) {
                            linearFlow.add(0, dataflowEdge);
                            linearFlow.add(0, dataflowEdge.getProducer());
                            continue;
                        }
                    }

                    if (lastNode != null) {
                        if ((dataflowEdge.getProducer() != null) && (compareMethodNodes(dataflowEdge.getProducer(), lastNode))) {
                            linearFlow.add(dataflowEdge);
                            linearFlow.add(dataflowEdge.getConsumer());
                            continue;
                        }
                    }
                }
            }

            ListIterator<DataflowEdge> edgeIter = tempEdges.listIterator();
            while (edgeIter.hasNext()) {
                DataflowEdge dataflowEdge = edgeIter.next();
                for (DataflowGraphComponent graphComponent : linearFlow) {
                    if (graphComponent instanceof DataflowEdge) {
                        DataflowEdge analyzedEdge = (DataflowEdge) graphComponent;
                        if (compareEdges(dataflowEdge, analyzedEdge)) {
                            edgeIter.remove();
                            break;
                        }
                    }
                }
            }

            for (int i = 0; i < tempEdges.size(); i++) {
                DataflowEdge dataflowEdge = tempEdges.get(i);
                if (dataflowEdge.getProducer() != null && dataflowEdge.getConsumer() != null) {
                    starterEdge = tempEdges.remove(i);
                    break;
                }
            }
            starterProducer = starterEdge.getProducer();
            starterConsumer = starterEdge.getConsumer();

            linearFlows.add(linearFlow);
        }
        for (List<DataflowGraphComponent> linearFlow : linearFlows) {
            DataflowGraphComponent graphComponent = linearFlow.get(linearFlow.size() - 2);

            if (graphComponent instanceof MethodInvocationDataflowEdge) {
                //TODO inward flow
                dataflowEdges.removeAll(linearFlow.stream().filter(dataGraphComp -> dataGraphComp instanceof DataflowEdge).map(edge -> (DataflowEdge) edge)
                        .collect(Collectors.toList()));
                injectionImpact.setDataflows(linearFlow.toArray(new DataflowGraphComponent[linearFlow.size()]));
                return getInjectionInward(linearFlow);
            }
        }

        return null;
    }

    private static InjectionSite getInjectionInward(List<DataflowGraphComponent> linearFlow) {

        //TODO currently here, didn't need to to reverse incoming data flows, should work as intended, but need to test
        int indexOfTransformation = -1;
        TransformationState transformationState = TransformationState.PRE_ANALYSIS;

        while (!transformationState.equals(TransformationState.POST_TRANSFORMATION)) {
            for (int i = 0; i < linearFlow.size(); i++) {

                DataflowGraphComponent dataflowGraphComponent = linearFlow.get(i);

                if (dataflowGraphComponent instanceof DataflowEdge) {

                    DataflowEdge dataflowEdge = (DataflowEdge) dataflowGraphComponent;
                    if (dataflowEdge.getDataTypeCommunicated().equals(BinaryData.class)) {
                        if (transformationState.equals(TransformationState.PRE_ANALYSIS)) {
                            transformationState = transformationState.next();
                        }
                    } else if (StructuredDocument.class.isAssignableFrom(dataflowEdge.getDataTypeCommunicated())) {
                        if (transformationState.equals(TransformationState.PRE_ANALYSIS)) {
                            throw new RuntimeException("NEVER ENCOUNTERED BINARY DATA... CHECK DATA FLOWS FOR VALIDITY");
                        } else if (transformationState.equals(TransformationState.PRE_TRANSFORMATION)) {
                            transformationState = transformationState.next();
                            indexOfTransformation = i;
                        }
                    }
                }
            }
        }

        DataflowEdge dataflowEdge = (DataflowEdge) linearFlow.get(indexOfTransformation);
        DataflowNode dataflowNode = dataflowEdge.getProducer();
        if (dataflowNode instanceof InterMethodDataflowNode) {
            //TODO
        } else if (dataflowNode instanceof MethodInvocationDataflowNode) {
            MethodInvocationDataflowNode methodNode = (MethodInvocationDataflowNode) dataflowNode;
            return new InjectionSite(methodNode.getEnclosingClassName(), methodNode.getJavaMethodName(), methodNode.getLineNumber());
        }
        return null;
    }

    private static InjectionSite getOutgoingInjectionSite(Set<DataflowEdge> dataflowEdges, InjectionImpact injectionImpact) {

        List<DataflowEdge> tempEdges = new ArrayList<>(dataflowEdges);
        DataflowEdge starterEdge = null;
        for (int i = 0; i < tempEdges.size(); i++) {
            DataflowEdge dataflowEdge = tempEdges.get(i);

            if (dataflowEdge.getProducer() != null && dataflowEdge.getConsumer() != null) {
               starterEdge = tempEdges.remove(i);
               break;
            }
        }

        DataflowNode starterProducer = starterEdge.getProducer();
        DataflowNode starterConsumer = starterEdge.getConsumer();

        List<List<DataflowGraphComponent>> linearFlows = new ArrayList<>();
        while (!tempEdges.isEmpty()) {
            List<DataflowGraphComponent> linearFlow = new ArrayList<>();

            linearFlow.add(starterProducer);
            linearFlow.add(starterEdge);
            linearFlow.add(starterConsumer);

            int infiniteLoopCheck = -1;
            while ((linearFlow.get(0) != null) || (linearFlow.get(linearFlow.size() - 1) != null)) {

                if (infiniteLoopCheck != -1) {
                    if (infiniteLoopCheck == linearFlow.size()) {
                        throw new RuntimeException("UNABLE TO RECONSTRUCT DATA FLOWS");
                    }
                } else {
                    infiniteLoopCheck = linearFlow.size();
                }

                for (DataflowEdge dataflowEdge : tempEdges) {

                    DataflowNode firstNode = (DataflowNode) linearFlow.get(0);
                    DataflowNode lastNode = (DataflowNode) linearFlow.get(linearFlow.size() - 1);

                    if (firstNode != null) {
                        if ((dataflowEdge.getConsumer() != null) && (compareMethodNodes(dataflowEdge.getConsumer(), firstNode))) {
                            linearFlow.add(0, dataflowEdge);
                            linearFlow.add(0, dataflowEdge.getProducer());
                            continue;
                        }
                    }

                    if (lastNode != null) {
                        if ((dataflowEdge.getProducer() != null) && (compareMethodNodes(dataflowEdge.getProducer(),lastNode))) {
                            linearFlow.add(dataflowEdge);
                            linearFlow.add(dataflowEdge.getConsumer());
                            continue;
                        }
                    }
                }
            }

            ListIterator<DataflowEdge> edgeIter = tempEdges.listIterator();
            while (edgeIter.hasNext()) {
                DataflowEdge dataflowEdge = edgeIter.next();
                for (DataflowGraphComponent graphComponent : linearFlow) {
                    if (graphComponent instanceof DataflowEdge) {
                        DataflowEdge analyzedEdge = (DataflowEdge) graphComponent;
                        if (compareEdges(dataflowEdge, analyzedEdge)) {
                            edgeIter.remove();
                            break;
                        }
                    }
                }
            }

            for (int i = 0; i < tempEdges.size(); i++) {
                DataflowEdge dataflowEdge = tempEdges.get(i);
                if (dataflowEdge.getProducer() != null && dataflowEdge.getConsumer() != null) {
                    starterEdge = tempEdges.remove(i);
                    break;
                }
            }
            starterProducer = starterEdge.getProducer();
            starterConsumer = starterEdge.getConsumer();

            linearFlows.add(linearFlow);
        }

        for (List<DataflowGraphComponent> linearFlow : linearFlows) {
           DataflowGraphComponent graphComponent = linearFlow.get(linearFlow.size() - 2);

           if (graphComponent instanceof InterMethodDataflowEdge) {
               //TODO outward flow
               dataflowEdges.removeAll(linearFlow.stream().filter(dataGraphComp -> dataGraphComp instanceof DataflowEdge).map(edge -> (DataflowEdge) edge)
                       .collect(Collectors.toList()));
               injectionImpact.setDataflows(linearFlow.toArray(new DataflowGraphComponent[linearFlow.size()]));
               return getInjectionSiteOutward(linearFlow);
           }
        }

        return null;
    }

    public static boolean compareEdges(DataflowEdge edge1, DataflowEdge edge2) {
        if (!compareMethodNodes(edge1.getProducer(), edge2.getProducer())) {
            return false;
        }

        if (!compareMethodNodes(edge1.getConsumer(), edge2.getConsumer())) {
            return false;
        }
        return true;
    }

    public static boolean compareMethodNodes(DataflowNode dataflowNode1, DataflowNode dataflowNode2) {

        if (dataflowNode1 == null && dataflowNode2 == null) {
            return true;
        } else if (dataflowNode1 == null && dataflowNode2 != null) {
            return false;
        } else if (dataflowNode1 != null && dataflowNode2 == null) {
            return false;
        }

        String methodName1 = null;
        String methodClass1 = null;
        int methodLine1 = -1;

        if (dataflowNode1 instanceof MethodInvocationDataflowNode) {
            MethodInvocationDataflowNode methodNode1 = (MethodInvocationDataflowNode) dataflowNode1;
            methodName1 = methodNode1.getJavaMethodName();
            methodClass1 = methodNode1.getJavaClassName();
            methodLine1 = methodNode1.getLineNumber();
        } else if (dataflowNode1 instanceof InterMethodDataflowNode) {
            InterMethodDataflowNode methodNode1 = (InterMethodDataflowNode) dataflowNode1;
            methodName1 = methodNode1.getJavaMethodName();
            methodClass1 = methodNode1.getJavaClassName();
            methodLine1 = methodNode1.getLineNumber();
        }

        String methodName2 = null;
        String methodClass2 = null;
        int methodLine2 = -1;

        if (dataflowNode2 instanceof MethodInvocationDataflowNode) {
            MethodInvocationDataflowNode methodNode2 = (MethodInvocationDataflowNode) dataflowNode2;
            methodName2 = methodNode2.getJavaMethodName();
            methodClass2 = methodNode2.getJavaClassName();
            methodLine2 = methodNode2.getLineNumber();
        } else if (dataflowNode2 instanceof InterMethodDataflowNode) {
            InterMethodDataflowNode methodNode2 = (InterMethodDataflowNode) dataflowNode2;
            methodName2 = methodNode2.getJavaMethodName();
            methodClass2 = methodNode2.getJavaClassName();
            methodLine2 = methodNode2.getLineNumber();
        }

        if (!methodName1.equals(methodName2)) {
            return false;
        } else if (!methodClass1.equals(methodClass2)) {
            return false;
        } else if (methodLine1 != methodLine2) {
            return false;
        }
        return true;
    }



    private static InjectionSite getInjectionSiteOutward(List<DataflowGraphComponent> linearFlow) {

        Collections.reverse(linearFlow);

        int indexOfTransformation = -1;
        TransformationState transformationState = TransformationState.PRE_ANALYSIS;

        while (!transformationState.equals(TransformationState.POST_TRANSFORMATION)) {
            for (int i = 0; i < linearFlow.size(); i++) {

                DataflowGraphComponent dataflowGraphComponent = linearFlow.get(i);

                if (dataflowGraphComponent instanceof DataflowEdge) {

                    DataflowEdge dataflowEdge = (DataflowEdge) dataflowGraphComponent;
                    if (dataflowEdge.getDataTypeCommunicated().equals(BinaryData.class)) {
                        if (transformationState.equals(TransformationState.PRE_ANALYSIS)) {
                            transformationState = transformationState.next();
                        }
                    } else if (StructuredDocument.class.isAssignableFrom(dataflowEdge.getDataTypeCommunicated())) {
                        if (transformationState.equals(TransformationState.PRE_ANALYSIS)) {
                            throw new RuntimeException("NEVER ENCOUNTERED BINARY DATA... CHECK DATA FLOWS FOR VALIDITY");
                        } else if (transformationState.equals(TransformationState.PRE_TRANSFORMATION)) {
                            transformationState = transformationState.next();
                            indexOfTransformation = i;
                        }
                    }
                }
            }
        }

        DataflowEdge dataflowEdge = (DataflowEdge) linearFlow.get(indexOfTransformation);
        DataflowNode dataflowNode = dataflowEdge.getConsumer();
        if (dataflowNode instanceof InterMethodDataflowNode) {
            //TODO
        } else if (dataflowNode instanceof MethodInvocationDataflowNode) {
            MethodInvocationDataflowNode methodNode = (MethodInvocationDataflowNode) dataflowNode;
            return new InjectionSite(methodNode.getEnclosingClassName(), methodNode.getJavaMethodName(), methodNode.getLineNumber());
        }
        return null;
    }

    private static Pair<String, String> getXslt(String currentVersion, String targetVersion) {
        return null;
    }

    private static String getFormatVersionInResource(String resourceUUID, GradleTaskHelper taskHelper) {

        String getVersion = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_resources_logical: <http://darpa.mil/immortals/ontology/r2.0.0/resources/logical#> \n" +
                "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>\n" +
                "\n" +
                "select ?targetVersion where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t?arch a IMMoRTALS_gmei:ApplicationArchitecture\n" +
                "\t; IMMoRTALS:hasAvailableResources ?archResource .\n" +
                "\t\n" +
                "\t{?archResource a* <???TARGET_RESOURCE???> .}\n" +
                "\tUNION\n" +
                "\t{?tempArchResource rdfs:subClassOf* <???TARGET_RESOURCE???> .\n" +
                "\t ?archResource a* ?tempArchResource }\n" +
                "\t \n" +
                "\t ?archResource IMMoRTALS:hasResources ?formattedResource .\n" +
                "\t \n" +
                "\t{?formattedResource a* IMMoRTALS_resources_logical:Schema}\n" +
                "\tUNION\n" +
                "\t{?tempFormatetdResource rdfs:subClassOf* IMMoRTALS_resources_logical:Schema. \n" +
                "\t ?formattedResource a* ?tempArchResource }\n" +
                "\t \n" +
                "\t ?formattedResource IMMoRTALS:hasVersion ?targetVersion .\n" +
                "\t \n" +
                "    }\n" +
                "}";
        getVersion = getVersion.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???TARGET_RESOURCE???", resourceUUID);
        AssertableSolutionSet versionSolutions = new AssertableSolutionSet();

        taskHelper.getClient().executeSelectQuery(getVersion, versionSolutions);

        if (!versionSolutions.getSolutions().isEmpty()) {
            return versionSolutions.getSolutions().get(0).get("targetVersion");
        } else {
            throw new RuntimeException("UNABLE TO FIND TARGET VERSION");
        }
    }

    private static Set<DataflowEdge> getEdgesBelongingToResourceInProj(String resourceUUID,
                                                                        String projUUID, GradleTaskHelper taskHelper) throws ClassNotFoundException, NoSuchFieldException, InstantiationException, IllegalAccessException {

        Set<DataflowEdge> dataflowEdges = new HashSet<>();

        String getNodes = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#>\n" +
                "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "select distinct ?edge where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t{?node a* IMMoRTALS_analysis:DataflowNode}\n" +
                "\t\tUNION\n" +
                "\t\t{?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowNode .\n" +
                "\t\t?node a* ?tester .}\n" +
                "\t\t\n" +
                "\t\t?node IMMoRTALS:hasAbstractResourceTemplate <???RESOURCE???> .\n" +
                "\n" +
                "\t\t{?edge a* IMMoRTALS_analysis:DataflowEdge}\n" +
                "\t\tUNION\n" +
                "\t\t{?tempEdge rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge .\n" +
                "\t\t ?edge a* ?tempEdge .}\n" +
                "\t\t\n" +
                "\t\t {?edge IMMoRTALS:hasConsumer ?node}\n" +
                "\t\tUNION\n" +
                "\t\t{?edge IMMoRTALS:hasProducer ?node}\n" +
                "\t\t\n" +
                "\t}\n" +
                "}";
        getNodes = getNodes.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???PROJ_UUID???", projUUID)
                .replace("???RESOURCE???", resourceUUID);
        AssertableSolutionSet edgeSolutions = new AssertableSolutionSet();

        taskHelper.getClient().executeSelectQuery(getNodes, edgeSolutions);

        if (!edgeSolutions.getSolutions().isEmpty()) {
            for (Solution edgeSolution : edgeSolutions.getSolutions()) {
                String edgeUUID = edgeSolution.get("edge");
                DataflowEdge dataflowEdge = (DataflowEdge) TriplesToPojo.convert(taskHelper.getGraphName(), edgeUUID, taskHelper.getClient());
                dataflowEdges.add(dataflowEdge);
            }

            return dataflowEdges;

        } else {
            throw new RuntimeException("UNABLE TO FIND NODES BELONGING TO SPECIFIED RESOURCE");
        }
    }

    private static Set<File> getProjectDependencies(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config, String projectUUID, MethodInvocationDataflowNode streamInitNode) throws UnsupportedEncodingException {
        Set<File> thirdPartyDependencies = getThirdPartyDependencies(taskHelper, projectUUID);
        Optional<String> androidJarOption = retrieveProjectPlatform(taskHelper, projectUUID);

        Set<File> platformResources = new HashSet<>();
        if (androidJarOption.isPresent()) {
            //androidApp
            File androidJarFile = new File(androidJarOption.get());
            if (androidJarFile.exists()) {
                platformResources.add(androidJarFile);
            } else {
                System.err.println("UNABLE TO FIND ANDROID JAR");
            }
        } else {
            platformResources.addAll(getJavaPlatformResources(taskHelper, config, streamInitNode));
        }

        thirdPartyDependencies.addAll(platformResources);

        return thirdPartyDependencies;
    }

    private static Set<File> getJavaPlatformResources(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config, MethodInvocationDataflowNode streamInitNode) throws UnsupportedEncodingException {
        String getDependentFiles = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?dependentFiles where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t<???CLASS???> IMMoRTALS:hasDependentFiles ?dependentFiles .\n" +
                "\t}\n" +
                "}";
        getDependentFiles = getDependentFiles.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                replace("???CLASS???", getClassWhereMethodNodeExists(taskHelper, config.getNamingContext().getNameForObject(streamInitNode)));
        AssertableSolutionSet dependentFileSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getDependentFiles, dependentFileSolutions);

        taskHelper.getPw().println("Precise location of initialization found, can now proceed to create wrapper class...");

        Set<String> dependentFilePaths = sanitizeDependencyPaths(dependentFileSolutions);

        Set<File> dependentFiles = new HashSet<>();
        String systemOS = System.getProperty("os.name");
        boolean windows = false;
        if (systemOS.toLowerCase().contains("windows")) {
            windows = true;
        }

        for (String dependentFilePath : dependentFilePaths) {
            if (!windows) {
                dependentFiles.add(new File("/" + dependentFilePath));
            } else {
                dependentFiles.add(new File(dependentFilePath));
            }
        }

        return dependentFiles;
    }

    //uses old field, hasSemanticLink
    @Deprecated
    private static String getClassWhereMethodNodeExists(GradleTaskHelper taskHelper, String nodeUUID) {

        String getClassWhereNodeExists = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?aClass where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "    \n" +
                "    ?methodCalls IMMoRTALS:hasSemanticLink <???METHOD_NODE???> .\n" +
                "    \n" +
                "    ?methods IMMoRTALS:hasInterestingInstructions ?methodCalls .\n" +
                "    \n" +
                "    ?aClass IMMoRTALS:hasMethods ?methods .\n" +
                "  }\n" +
                "}";
        getClassWhereNodeExists = getClassWhereNodeExists.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                replace("???METHOD_NODE???", nodeUUID);
        AssertableSolutionSet classSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getClassWhereNodeExists, classSolutions);

        if (!classSolutions.getSolutions().isEmpty()) {
            return classSolutions.getSolutions().get(0).get("aClass");
        } else {
            System.out.println("UNABLE TO FIND CLASS WHERE METHOD NODE EXISTS");
            return null;
        }
    }

    private static Set<String> sanitizeDependencyPaths(AssertableSolutionSet dependentFileSolutions) throws UnsupportedEncodingException {
        Set<String> dependentFiles = new HashSet<>();
        for (Solution dependentFileSolution : dependentFileSolutions.getSolutions()) {
            String dependentFileFullPath = dependentFileSolution.get("dependentFiles");
            dependentFiles.add(URLDecoder.decode(dependentFileFullPath.replace("\\", "/").
                    substring(dependentFileFullPath.indexOf("jar:file:/") + 10, dependentFileFullPath.indexOf("!")), "UTF-8"));
        }
        return dependentFiles;
    }

    private static Optional<String> retrieveProjectPlatform(GradleTaskHelper taskHelper, String projectUUID) {
        String checkIfApplicableProjectIsAndroid = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "select ?uberJar where {\n" +
                "\tgraph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t<???PROJECT_UUID???> IMMoRTALS:hasAndroidApp ?androidApp .\n" +
                "    \n" +
                "   \t\t ?androidApp IMMoRTALS:hasPathToUberJar ?uberJar .\n" +
                "    \n" +
                "\t}\n" +
                "}";
        checkIfApplicableProjectIsAndroid = checkIfApplicableProjectIsAndroid.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???PROJECT_UUID???", projectUUID);
        AssertableSolutionSet androidJarSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(checkIfApplicableProjectIsAndroid, androidJarSolutions);

        if (androidJarSolutions.getSolutions().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(androidJarSolutions.getSolutions().get(0).get("uberJar"));
        }
    }

    protected static Set<File> getThirdPartyDependencies(GradleTaskHelper taskHelper, String projectUUID) {

        Set<File> dependencies = new HashSet<>();

        String getDependencyPaths = "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n\n" +
                "select ?filePaths where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t\t<???PROJECT_UUID???> IMMoRTALS:hasClasspaths ?classPaths .\n" +
                "\t\t\n" +
                "\t\t?classPaths IMMoRTALS:hasClasspathName ?classPathName\n" +
                "\t\t; IMMoRTALS:hasElementHashValues ?hashes .\n" +
                "\n\t\tFILTER regex(?classPathName, \"compile\") .\n" +
                "\t\t?jars a IMMoRTALS_bytecode:JarArtifact\n" +
                "\t\t; IMMoRTALS:hasHash ?hashes\n" +
                "\t\t; IMMoRTALS:hasFileSystemPath ?filePaths .\n" +
                "\t}\n" +
                "}";
        getDependencyPaths = getDependencyPaths.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???PROJECT_UUID???", projectUUID);
        GradleTaskHelper.AssertableSolutionSet dependencySolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getDependencyPaths, dependencySolutions);

        for (GradleTaskHelper.Solution dependencySolution : dependencySolutions.getSolutions()) {
            dependencies.add(new File(dependencySolution.get("filePaths")));
        }

        return dependencies;
    }

    private static String getNodesProject(GradleTaskHelper taskHelper, String nodeUUID) {

        String getNodesProject = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select distinct ?project where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "    \n" +
                "    {\n" +
                "    ?methodCalls IMMoRTALS:hasSemanticLink <???METHOD_NODE???> .\n" +
                "    \n" +
                "    ?methods IMMoRTALS:hasInterestingInstructions ?methodCalls .\n" +
                "    \n" +
                "    ?aClass IMMoRTALS:hasMethods ?methods\n" +
                "      ; IMMoRTALS:hasClassName ?fullName .\n" +
                "    \n" +
                "    BIND(str(STRBEFORE(str(?fullName), \"$\")) as ?regexedName)\n" +
                "    BIND(REPLACE(str(?regexedName), \"/\", \"\\\\.\") as ?urlFullName)\n" +
                "    \n" +
                "    ?sourceFiles IMMoRTALS:hasFullyQualifiedName ?urlFullName .\n" +
                "    \n" +
                "    ?sourceRepo IMMoRTALS:hasSourceFiles ?sourceFiles .\n" +
                "    \n" +
                "    ?project IMMoRTALS:hasSourceCodeRepo ?sourceRepo .\n" +
                "    }\n" +
                "    union\n" +
                "    {\n" +
                "    ?methodCalls IMMoRTALS:hasSemanticLink <???METHOD_NODE???> .\n" +
                "    \n" +
                "    ?methods IMMoRTALS:hasInterestingInstructions ?methodCalls .\n" +
                "    \n" +
                "    ?aClass IMMoRTALS:hasMethods ?methods\n" +
                "      ; IMMoRTALS:hasClassName ?fullName .\n" +
                "    \n" +
                "    BIND(REPLACE(str(?fullName), \"/\", \"\\\\.\") as ?urlFullName)\n" +
                "    \n" +
                "    ?sourceFiles IMMoRTALS:hasFullyQualifiedName ?urlFullName .\n" +
                "    \n" +
                "    ?sourceRepo IMMoRTALS:hasSourceFiles ?sourceFiles .\n" +
                "    \n" +
                "    ?project IMMoRTALS:hasSourceCodeRepo ?sourceRepo .\n" +
                "    }\n" +
                "    \n" +
                "    \n" +
                "    \t\n" +
                "\n" +
                "  }\n" +
                "}";
        getNodesProject = getNodesProject.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???METHOD_NODE???", nodeUUID);
        AssertableSolutionSet projectSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getNodesProject, projectSolutions);

        if (!projectSolutions.getSolutions().isEmpty()) {
            return projectSolutions.getSolutions().get(0).get("project");
        } else {
            System.out.println("UNABLE TO FIND PROJECT THAT DATA FLOW NODE: " + nodeUUID + " BELONGS TO");
            return null;
        }
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

    public static String getMethodSignatureFromPointer(String javaMethodPointer) {
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

    private static AspectConfigureSolution getAspectConfigureSolution(GradleTaskHelper taskHelper, String projectUUID) throws Exception {
        DeploymentModel deploymentModel = retrieveDeploymentModel(taskHelper);
        List<AspectConfigureSolution> configureSolutions = AnalysisFrameAssessment.retrieveConfigurationSolutions(taskHelper, projectUUID);
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
                "select distinct ?constraintViolation where {\n" +
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
