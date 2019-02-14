package com.securboration.dataflow.analyzer;

import com.securboration.immortals.ontology.analysis.DataflowGraphComponent;
import soot.Scene;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class Main {
    public static void main(String[] args) throws IOException {

        DataflowAnalyzerPlatform dataflowAnalyzerPlatform = new DataflowAnalyzerPlatform();

        String pathToProjJar = args[0];
        String pathToADSLJar = args[1];
        String pathToCallTraceFile = args[2];

        Options.v().set_whole_program(true);
        Options.v().set_keep_line_number(true);
        Scene.v().setSootClassPath(Scene.v().getSootClassPath()
                + File.pathSeparatorChar + pathToProjJar
                + File.pathSeparatorChar + pathToADSLJar);

        List<Stack<String>> callTraceStackList = dataflowAnalyzerPlatform.parseCallTraceStack(pathToCallTraceFile);
        Set<Stack<DataflowGraphComponent>> dataflowGraphs = dataflowAnalyzerPlatform.processCallTraceStacks(callTraceStackList);
        String outputFilePath = dataflowAnalyzerPlatform.serializeDataflows(dataflowGraphs);
        System.out.println("Emitted dataflows at: " + outputFilePath);
    }
}
