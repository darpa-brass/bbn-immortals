package com.securboration.client.utility;

import com.securboration.client.FileUtils;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

@DfuAnnotation(
        functionalityBeingPerformed = FileTransformer.class
)
public class GetStringFromFile {

    @FunctionalAspectAnnotation(aspect = RetrieveFileResourceAspect.class)
    public static String getString(String pathToFile) {
        File file = new File(pathToFile);
        if (!file.exists()) {
            System.out.println("FILE DOES NOT EXIST, ASPECT MAY NOT PERFORM AS EXPECTED");
            return null;
        }

        try {
            return FileUtils.readFileToString(file, Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("ERROR READING FILE");
        }
        return null;
    }
}
