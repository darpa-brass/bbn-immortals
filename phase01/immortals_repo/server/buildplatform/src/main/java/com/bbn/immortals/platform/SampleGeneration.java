package com.bbn.immortals.platform;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by awellman@bbn.com on 1/27/16.
 */
public class SampleGeneration {

    public static void main(String args[]) {
        if (args == null || args.length != 1) {
            System.out.println("Sample generator must be provided with a target directory and nothing else!");
            System.exit(1);
        }

        // General image porocessing properties
        String externalTypePackage = "com.bbn.cot.CotEventContainer";
        String processingTypePackage = "java.awt.image.BufferedImage";
        String targetPackage = "com.bbn.immortals.processors.pipes";
        String targetClassName = "GeneratedCotProcessor";

        // Processor package identifiers
        String grayscaleProcessor = "com.bbn.marti.immortals.processors.image.ImageGrayscaleProcessor";
        String convolverAwtProcessor = "com.bbn.immortals.processors.image.ImageConvolverAwtProcessor";
        String saveFileProcessor = "com.bbn.immortals.processors.image.ImageFileSavingProcessor";
        String convolverNaiveProcessor = "com.bbn.immortals.processors.image.ImageConvolverNaiveProcessor";

        // Adding three processors to it to save the original file for inspection, convolve the file, and then save the modified file for inspection
        generateProcessor(args[0], targetClassName, targetPackage, externalTypePackage, processingTypePackage,
                saveFileProcessor,
//                grayscaleProcessor,
                convolverNaiveProcessor,
//                convolverAwtProcessor,
                saveFileProcessor);

    }

    public static void generateProcessor(@NotNull String targetFilePath,  @NotNull String targetClassName, @NotNull String targetPackage, @NotNull String externalTypePackage, @NotNull String processingTypePackage, @NotNull String ... processorPackages) {

        String classText = ClassGenerator.GenerateProcessorPipe(targetClassName, targetPackage, externalTypePackage, processingTypePackage, processorPackages);

        try {
            String fileDirectory = targetFilePath + (targetFilePath.endsWith("/") ? "" : "/") + targetPackage.replace('.','/');
            Path fileDirectoryPath = Paths.get(fileDirectory);
            String filePath = fileDirectory + "/" + targetClassName + ".java";

            if (Files.notExists(fileDirectoryPath)) {
                Files.createDirectories(fileDirectoryPath);
            }

            Files.write(Paths.get(filePath), classText.getBytes(), StandardOpenOption.CREATE_NEW);

            System.out.println("File created at '" + filePath + "'");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
