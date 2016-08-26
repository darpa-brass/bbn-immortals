package com.bbn.immortals.platform;


import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 1/27/16.
 */
public class ClassGenerator {

    public static String GenerateProcessorPipe(@NotNull String className, @NotNull String packageName, @NotNull String enclosingType, @NotNull String typeToProcess, @NotNull String... processorPackages) {
        String simpleTypeToProcess = typeToProcess.substring(typeToProcess.lastIndexOf('.') + 1, typeToProcess.length());

        List<String> classLines = new LinkedList<>();

        String line = "package " + packageName + ";";
        classLines.add(line);

        line = "import org.jetbrains.annotations.*;";
        classLines.add(line);

        // Generate the class declaration
        line = "public class " + className + " extends com.bbn.immortals.core.AbstractOutputProvider<" + enclosingType + "> implements com.bbn.immortals.core.InputProviderInterface<" + enclosingType + "> {";
        classLines.add(line);

        // For each Processor, create a new one
        for( int i = 0; i < processorPackages.length; i++) {
            String processor = processorPackages[i];
            line = "private " + processor + " " + processor.substring(processor.lastIndexOf('.') + 1, processor.length()) + i + " = new " + processor + "();";
            classLines.add(line);
        }

        line = "public void handleData(@NotNull " + enclosingType + " data) {";
        classLines.add(line);

        line = typeToProcess + " object0 = data.get" + simpleTypeToProcess + "();";
        classLines.add(line);

        for( int i = 0; i < processorPackages.length; i++) {
            String processor = processorPackages[i];
            line = typeToProcess + " object" + (i+1) + " = " + processor.substring(processor.lastIndexOf('.') + 1, processor.length()) + i + ".process(object" + i + ");";
            classLines.add(line);
        }

        line = "data.set" + simpleTypeToProcess + "(object" + processorPackages.length + ");";
        classLines.add(line);

        line = "distributeResult(data);";
        classLines.add(line);

        line = "}";
        classLines.add(line);

        line = "}";
        classLines.add(line);

        String classText = "";

        for (String str : classLines) {
            classText += (str + "\n");
        }

        return classText;
    }
}
