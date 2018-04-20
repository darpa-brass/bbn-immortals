package com.securboration.immortals.test.example;

import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.constraint.PropertyImpactType;
import com.securboration.immortals.ontology.constraint.ScopeOfRepairs;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.base.Sys;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class TestIngestionWithAnalysis {
    
    public static void main(String[] args) throws Exception {

        final String dirPath = args[0];
        final String url = "http://localhost:8080/krs/ingest";
        
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-type", "text/plain");

        StringEntity stringEntity = new StringEntity(dirPath);
        httpPost.getRequestLine();
        httpPost.setEntity(stringEntity);

        System.out.println("Beginning ingestion of specified directory...");
        HttpResponse response = httpClient.execute(httpPost);
        

        InputStream ips  = response.getEntity().getContent();
        BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));
        if(response.getStatusLine().getStatusCode()!= HttpStatus.SC_OK)
        {
            throw new Exception(response.getStatusLine().getReasonPhrase());
        }
        StringBuilder sb = new StringBuilder();
        String s;
        while(true )
        {
            s = buf.readLine();
            if(s==null || s.length()==0)
                break;
            sb.append(s);

        }
        buf.close();
        ips.close();
        
        String graphName = sb.toString();

        System.out.println("Ingestion complete. Gathering produced artifacts...");
        
        String getViolations = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_constraint: <http://darpa.mil/immortals/ontology/r2.0.0/constraint#> \n" +
                "\n" +
                "select ?violations where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t\n" +
                "\t\t?report a IMMoRTALS_constraint:ConstraintAssessmentReport\n" +
                "\t\t; IMMoRTALS:hasConstraintViolations ?violations .\n" +
                "\t\t\n" +
                "\t}\n" +
                "}";
        getViolations = getViolations.replace("???GRAPH_NAME???", graphName);
        FusekiClient fusekiClient = new FusekiClient("http://localhost:3030/ds");
        GradleTaskHelper.AssertableSolutionSet violationSolutions = new GradleTaskHelper.AssertableSolutionSet();
        fusekiClient.executeSelectQuery(getViolations, violationSolutions);
        
        if (!violationSolutions.getSolutions().isEmpty()) {
            System.out.println("Total of " + violationSolutions.getSolutions().size() + " violations found.\n");
            
            for (GradleTaskHelper.Solution violationSolution : violationSolutions.getSolutions()) {

                String violationUUID = violationSolution.get("violations");

                String getViolationData = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?analysisImpacts ?constraint ?scope ?strategy  where {\n" +
                        "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t<???VIOLATION???> IMMoRTALS:hasScopeOfRepairs ?scope\n" +
                        "\t\t; IMMoRTALS:hasConstraint ?constraint\n" +
                        "\t\t; IMMoRTALS:hasMitigationStrategyUtilized ?strategy .\n" +
                        "\t}\n" +
                        "}";
                getViolationData = getViolationData.replace("???GRAPH_NAME???", graphName).replace("???VIOLATION???", violationUUID);
                GradleTaskHelper.AssertableSolutionSet violationDataSolutions = new GradleTaskHelper.AssertableSolutionSet();
                fusekiClient.executeSelectQuery(getViolationData, violationDataSolutions);
                
                System.out.println("Violation " + violationSolutions.getSolutions().indexOf(violationSolution) + ": " +
                        violationUUID);
                String constraintUUID = violationDataSolutions.getSolutions().get(0).get("constraint");

                String getConstraintBindingSites = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?bindingSite ?criterionCriterion ?criterionProperty ?src ?dest where {\n" +
                        "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t<???CONSTRAINT???> IMMoRTALS:hasAssertionBindingSites ?bindingSite\n" +
                        "\t\t; IMMoRTALS:hasCriterion ?criterion .\n" +
                        "\t\t\n" +
                        "\t\t?bindingSite IMMoRTALS:hasSrc ?src \n" +
                        "\t\t; IMMoRTALS:hasDest ?dest .\n" +
                        "\t\t\n" +
                        "\t\t?criterion IMMoRTALS:hasCriterion ?criterionCriterion\n" +
                        "\t\t; IMMoRTALS:hasProperty ?criterionProperty .\n" +
                        "\t}\n" +
                        "}";
                getConstraintBindingSites = getConstraintBindingSites.replace("???GRAPH_NAME???", graphName).replace("???CONSTRAINT???", constraintUUID);
                GradleTaskHelper.AssertableSolutionSet constraintSolutions = new GradleTaskHelper.AssertableSolutionSet();
                fusekiClient.executeSelectQuery(getConstraintBindingSites, constraintSolutions);

                String strategyUtilized = violationDataSolutions.getSolutions().get(0).get("strategy");

                String getStrategyImpacts = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?remProperty ?remCriterion2 where {\n" +
                        "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t<???STRATEGY???> IMMoRTALS:hasImpact ?impact .\n" +
                        "\t\t\n" +
                        "\t\t?impact IMMoRTALS:hasRemediationStrategy ?remediationStrategy .\n" +
                        "\t\t\n" +
                        "\t\t?remediationStrategy IMMoRTALS:hasCriterion ?remCriterion .\n" +
                        "\t\t\n" +
                        "\t\t?remCriterion IMMoRTALS:hasProperty ?remProperty\n" +
                        "\t\t; IMMoRTALS:hasCriterion ?remCriterion2 .\n" +
                        "\t}\n" +
                        "}";
                getStrategyImpacts = getStrategyImpacts.replace("???GRAPH_NAME???", graphName).replace("???STRATEGY???", strategyUtilized);
                GradleTaskHelper.AssertableSolutionSet impactSolutions = new GradleTaskHelper.AssertableSolutionSet();
                fusekiClient.executeSelectQuery(getStrategyImpacts, impactSolutions);
                
                String criterionProperty = constraintSolutions.getSolutions().get(0).get("criterionProperty");
                String criterionType = constraintSolutions.getSolutions().get(0).get("criterionCriterion");
                System.out.println("Violation failed to fulfill constraint " + constraintUUID + " by means of ");

                switch (PropertyCriterionType.valueOf(criterionType)) {
                    case PROPERTY_ABSENT:
                        System.out.println("having the property " + criterionProperty + " absent from its specified binding site(s): ");
                        break;

                    default:
                        break;
                }
                for (GradleTaskHelper.Solution bindingSiteSolution : constraintSolutions.getSolutions()) {
                    String bindingSiteUUID = bindingSiteSolution.get("bindingSite");
                    String srcUUID = bindingSiteSolution.get("src");
                    String destUUID = bindingSiteSolution.get("dest");

                    System.out.println("Binding site " + constraintSolutions.getSolutions().indexOf(bindingSiteSolution)
                            + ", " + bindingSiteUUID + " or any data flows from a \n" + srcUUID + " resource to a " + destUUID + " resource.");
                }

                System.out.print("\nThe strategy used to rectify this constraint was " + strategyUtilized + ", or the ");

                if (!impactSolutions.getSolutions().isEmpty()) {

                    GradleTaskHelper.Solution impactSolution = impactSolutions.getSolutions().get(0);
                    String remProperty = impactSolution.get("remProperty");
                    String propertyCriterion = impactSolution.get("remCriterion2");

                    switch (PropertyCriterionType.valueOf(propertyCriterion)) {
                        case PROPERTY_ADDED:
                            System.out.println("adding of the\nproperty " + remProperty + " to the specified binding site(s).");
                            break;

                        default:
                            break;
                    }
                }
                String scopeOfRepairs = violationDataSolutions.getSolutions().get(0).get("scope");
                System.out.print("The scope of repairs required were ");
                switch (ScopeOfRepairs.valueOf(scopeOfRepairs)) {
                    case INTER_PROCESS:
                        System.out.println("inter-process, meaning the systems on both ends of the data flow in violation" +
                                " were augmented to satisfy the constraint.");
                }

                String getAnalysisImpacts = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?analysisImpacts where {\n" +
                        "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t<???VIOLATION???> IMMoRTALS:hasAnalysisImpacts ?analysisImpacts\n" +
                        "\t}\n" +
                        "}";
                getAnalysisImpacts = getAnalysisImpacts.replace("???GRAPH_NAME???", graphName).replace("???VIOLATION???", violationUUID);
                GradleTaskHelper.AssertableSolutionSet analysisImpactSolutions = new GradleTaskHelper.AssertableSolutionSet();
                fusekiClient.executeSelectQuery(getAnalysisImpacts, analysisImpactSolutions);

                if (!analysisImpactSolutions.getSolutions().isEmpty()) {

                    System.out.println("The introduction of the wrapper class(es) have caused the following impacts to the code base: ");

                    for (GradleTaskHelper.Solution analysisImpactSolution : analysisImpactSolutions.getSolutions()) {
                        String analysisImpact = analysisImpactSolution.get("analysisImpacts");

                        String getProducedFilesFromImpacts = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                "prefix IMMoRTALS_lang: <http://darpa.mil/immortals/ontology/r2.0.0/lang#>\n" +
                                "\n" +
                                "select ?aspectImplemented ?producedSourceFiles ?producedFileSource ?userFileName ?augmentedUserFile ?fileName ?source where {\n" +
                                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                                "\t\t<???ANALYSIS_IMPACT???> IMMoRTALS:hasProducedSourceFiles ?producedSourceFiles\n" +
                                "\t\t; IMMoRTALS:hasAugmentedUserFile ?augmentedUserFile\n" +
                                "\t\t; IMMoRTALS:hasAspectImplemented ?aspectImplemented .\n" +
                                "\t\t\n" +
                                "\t\t?producedSourceFiles IMMoRTALS:hasFileName ?fileName\n" +
                                "\t\t; IMMoRTALS:hasSource ?producedFileSource .\n" +
                                "\t\t\n" +
                                "\t\t?augmentedUserFile IMMoRTALS:hasFileName ?userFileName .\n" +
                                "\t\t\n" +
                                "\t\t?userFile a IMMoRTALS_lang:SourceFile\n" +
                                "\t\t; IMMoRTALS:hasFileName ?userFileName\n" +
                                "\t\t; IMMoRTALS:hasSource ?source .\n" +
                                "\t\t\n" +
                                "\t}\n" +
                                "}";
                        getProducedFilesFromImpacts = getProducedFilesFromImpacts.replace("???GRAPH_NAME???", graphName)
                                .replace("???ANALYSIS_IMPACT???", analysisImpact);
                        GradleTaskHelper.AssertableSolutionSet producedFilesSolutions = new GradleTaskHelper.AssertableSolutionSet();
                        fusekiClient.executeSelectQuery(getProducedFilesFromImpacts, producedFilesSolutions);

                        if (!producedFilesSolutions.getSolutions().isEmpty()) {
                            String aspectImplemented = producedFilesSolutions.getSolutions().get(0).get("aspectImplemented");
                            System.out.print("Impact " + analysisImpactSolutions.getSolutions().indexOf(analysisImpactSolution) + ", "
                                    + analysisImpact + " produced a wrapper file to implement the required aspect \n" + aspectImplemented + ". " +
                                    "\nHere is the file at various stages in the analysis: ");

                            Map<String, String> fileNameToSource = new HashMap<>();
                            for (GradleTaskHelper.Solution producedFileSolution : producedFilesSolutions.getSolutions()) {

                                String producedFileName = producedFileSolution.get("fileName");
                                String producedFileSource = producedFileSolution.get("producedFileSource");

                                fileNameToSource.put(producedFileName, producedFileSource);
                            }

                            Optional<String> passThroughFile = fileNameToSource.keySet().stream().filter(key -> key.contains("[1/2]")).findFirst();
                            passThroughFile.ifPresent(s1 -> System.out.println("\n\nStage 1, Pass-Through Wrapper:\n" +
                                    fileNameToSource.get(s1)));

                            Optional<String> adaptationSurfaceIntegratedFile = fileNameToSource.keySet().stream().filter(key -> key.contains("[2/2]")).findFirst();
                            adaptationSurfaceIntegratedFile.ifPresent(s1 -> System.out.println("\n\nStage 2, Post-Integration of Adaptation Surface:\n" +
                                    fileNameToSource.get(s1)));

                            fileNameToSource.keySet().removeIf(key -> key.contains("["));

                            Iterator<String> fileNameIterator = fileNameToSource.keySet().iterator();
                            if (fileNameIterator.hasNext()) {
                                System.out.println("\n\nCompleted Wrapper File:\n" +
                                        fileNameToSource.get(fileNameIterator.next()));
                            }

                            String augmentedUserFileName = producedFilesSolutions.getSolutions().get(0).get("userFileName");
                            String augmentedUserFile = producedFilesSolutions.getSolutions().get(0).get("augmentedUserFile");
                            String newSource = producedFilesSolutions.getSolutions().get(0).get("source");

                            System.out.println("In addition to the wrapper files produced, user source file " + augmentedUserFileName +
                                    " had to be modified to accommodate the wrapper classes introduced: ");

                            String getAugmentedMethodInvocations = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                                    "\n" +
                                    "select ?methodName ?lineNumber where {\n" +
                                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                                    "\t\t<???AUGMENTED_USER_FILE???> IMMoRTALS:hasAugmentedMethodInvocations ?methodInvokes .\n" +
                                    "\t\t\n" +
                                    "\t\t?methodInvokes IMMoRTALS:hasMethodName ?methodName\n" +
                                    "\t\t; IMMoRTALS:hasLineNumber ?lineNumber .\n" +
                                    "\t}\n" +
                                    "}";
                            getAugmentedMethodInvocations = getAugmentedMethodInvocations.replace("???GRAPH_NAME???", graphName)
                                    .replace("???AUGMENTED_USER_FILE???", augmentedUserFile);

                            GradleTaskHelper.AssertableSolutionSet augmentedMethodInvocationsSolutions = new GradleTaskHelper.AssertableSolutionSet();
                            fusekiClient.executeSelectQuery(getAugmentedMethodInvocations, augmentedMethodInvocationsSolutions);

                            for (GradleTaskHelper.Solution augmentedMethodInvocationSolution : augmentedMethodInvocationsSolutions.getSolutions()) {

                                String methodName = augmentedMethodInvocationSolution.get("methodName");
                                int lineNumber = Integer.parseInt(augmentedMethodInvocationSolution.get("lineNumber"));

                                System.out.println("- In method " + methodName + ", on line number " + lineNumber);
                            }
                            System.out.println("The completed augmented user source code:\n\n" + newSource);
                        }
                    }
                } else {
                    System.out.println("Violation had no impacts on code base; the code impact was likely initiated by another violation in this case.");
                }
            }
        }
    }
}
