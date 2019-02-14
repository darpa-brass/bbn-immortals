package com.securboration.immortals.soot;

import com.securboration.immortals.ontology.constraint.InjectionImpact;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.utility.GradleTaskHelper;
import soot.*;

public class SootXmlTransformer {
    
    public static SootMethod generateXmlTransformMethod(GradleTaskHelper taskHelper, InjectionImpact injectionImpact) {

        String queryForXmlTransformers = "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> \n" +
                "prefix IMMoRTALS_property_impact: <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select distinct ?methodName ?className ?abstractAspect where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t    ?dfu a IMMoRTALS_dfu_instance:DfuInstance\n" +
                "\t\t; IMMoRTALS:hasFunctionalAspects ?aspectInstance .\n" +
                "\t\t\n" +
                "\t\t?aspectInstance IMMoRTALS:hasAbstractAspect ?abstractAspect\n" +
                "\t\t; IMMoRTALS:hasMethodPointer ?pointer .\n" +
                "\t\t\n" +
                "\t\t?abstractAspect IMMoRTALS:hasImpactStatements ?impacts .\n" +
                "\t\t\n" +
                "\t\t?impacts IMMoRTALS:hasApplicableResource ?applicableResource .\n" +
                "\t\t\n" +
                "\t\t?applicableResource a IMMoRTALS_property_impact:XmlResourceImpact\n" +
                "\t\t; IMMoRTALS:hasXmlResourceImpactType \"XML_INSTANCE_CHANGE\" .\n" +
                "\t\t\n" +
                "\t\t?classArt IMMoRTALS:hasClassModel ?aClass .\n" +
                "\t\t\n" +
                "\t\t?aClass IMMoRTALS:hasMethods ?methods\n" +
                "\t\t; IMMoRTALS:hasClassName ?className .\n" +
                "\t\t\n" +
                "\t\t?methods IMMoRTALS:hasBytecodePointer ?pointer\n" +
                "\t\t; IMMoRTALS:hasMethodName ?methodName .\n" +
                "\t}\n" +
                "}";
        queryForXmlTransformers = queryForXmlTransformers.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        GradleTaskHelper.AssertableSolutionSet dfuSolutions = new GradleTaskHelper.AssertableSolutionSet();

        taskHelper.getClient().executeSelectQuery(queryForXmlTransformers, dfuSolutions);
        if (!dfuSolutions.getSolutions().isEmpty()) {

            //TODO for now just take first one, later will likely want criteria for selecting "best" dfu instance
            GradleTaskHelper.Solution dfuSolution = dfuSolutions.getSolutions().get(0);
            String methodName = dfuSolution.get("methodName");
            String className = dfuSolution.get("className");
            String abstractAspect = dfuSolution.get("abstractAspect");

            try {
                injectionImpact.setAspectImplemented((Class<? extends FunctionalAspect>) TriplesToPojo.convert(taskHelper.getGraphName(),
                        abstractAspect, taskHelper.getClient()));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                System.out.println("UNABLE TO RETRIEVE ASPECT IMPLEMENTED");
            }

            SootClass owner = Scene.v().loadClassAndSupport(className.replace("/", "."));
            SootMethod dfuMethod = owner.getMethodByName(methodName);

            return dfuMethod;
        }
       return null;
    }

    public static SootMethod generateFileReader(GradleTaskHelper taskHelper) {

        String getFileReaderQuery = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#>\n" +
                "prefix IMMoRTALS_functionality_imagecapture: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/imagecapture#>\n" +
                "prefix IMMoRTALS_functionality_datatype: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#>\n" +
                "\n" +
                "select distinct ?methodName ?className ?abstractAspect where {\n" +
                "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t    ?dfu a IMMoRTALS_dfu_instance:DfuInstance\n" +
                "\t\t; IMMoRTALS:hasFunctionalAspects ?aspectInstance .\n" +
                "\t\t\n" +
                "\t\t?aspectInstance IMMoRTALS:hasAbstractAspect ?abstractAspect\n" +
                "\t\t; IMMoRTALS:hasMethodPointer ?pointer .\n" +
                "\t\t\n" +
                "\t\t?abstractAspect IMMoRTALS:hasInputs ?in\n" +
                "\t\t; IMMoRTALS:hasOutputs ?out .\n" +
                "\t\t\n" +
                "\t\t?in IMMoRTALS:hasType  IMMoRTALS_functionality_imagecapture:FileHandle .\n" +
                "\t\t?out IMMoRTALS:hasType  IMMoRTALS_functionality_datatype:Text .\n" +
                "\t\t\n" +
                "\t\t?classArt IMMoRTALS:hasClassModel ?aClass .\n" +
                "\t\t\n" +
                "\t\t?aClass IMMoRTALS:hasMethods ?methods\n" +
                "\t\t; IMMoRTALS:hasClassName ?className .\n" +
                "\t\t\n" +
                "\t\t?methods IMMoRTALS:hasBytecodePointer ?pointer\n" +
                "\t\t; IMMoRTALS:hasMethodName ?methodName .\n" +
                "\t}\n" +
                "}";
        getFileReaderQuery = getFileReaderQuery.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        GradleTaskHelper.AssertableSolutionSet fileReaderSolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getFileReaderQuery, fileReaderSolutions);

        if (!fileReaderSolutions.getSolutions().isEmpty()) {
            GradleTaskHelper.Solution fileReaderSolution = fileReaderSolutions.getSolutions().get(0);
            String methodName = fileReaderSolution.get("methodName");
            String className = fileReaderSolution.get("className");
            //TODO
            //String abstractAspect = fileReaderSolution.get("abstractAspect");

            SootClass owner = Scene.v().loadClassAndSupport(className.replace("/", "."));
            SootMethod fileReaderMethod = owner.getMethodByName(methodName);

            return fileReaderMethod;
        }

        return null;
    }
}
