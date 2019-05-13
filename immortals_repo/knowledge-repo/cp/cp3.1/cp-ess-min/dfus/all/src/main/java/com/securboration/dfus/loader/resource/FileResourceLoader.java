package com.securboration.dfus.loader.resource;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

@DfuAnnotation(
        functionalityBeingPerformed = ResourceLoader.class
)
public class FileResourceLoader {

    @FunctionalAspectAnnotation(
            aspect = ApplyFileResourceLoaderAspect.class
    )
    public static String readFileFromClassLoader(String resourceName) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (InputStream in = FileResourceLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            boolean stop = false;
            final byte[] buffer = new byte[1024];
            while(!stop) {
                final int bytesRead = in.read(buffer);

                if(bytesRead < 0) {
                    stop = true;
                } else {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        return new String(out.toByteArray(), Charset.defaultCharset());
    }

}
