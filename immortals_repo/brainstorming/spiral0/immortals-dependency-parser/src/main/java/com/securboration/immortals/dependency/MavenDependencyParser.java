package com.securboration.immortals.dependency;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifact;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.bytecode.Dependency;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MavenDependencyParser
{
    public static void main(String [] args)
    {
        Path tmpFile = Paths.get("mvn.tmp").toAbsolutePath();
        System.out.println(tmpFile);

        System.out.println("\nPrinting paths in M2 with walkFileTree.");
        try
        {
            Path dir = Paths.get("C:\\Users\\Adam\\.m2\\repository");

            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
                {
                    System.out.println(dir);

                    try(Stream<Path> dirStream = Files.list(dir))
                    {
                        List<Path> files = dirStream.filter(Files::isRegularFile).collect(Collectors.toList());

                        if(files.stream().anyMatch(p -> p.getFileName().toString().endsWith(".jar")))
                        {
                            Optional<String> pom = files.stream()
                                    .map(Path::getFileName)
                                    .map(Path::toString)
                                    .filter(p -> p.endsWith(".pom"))
                                    .findFirst();

                            if(pom.isPresent())
                            {
                                try
                                {
                                    System.out.println("POM = " + pom.get());

                                    ProcessBuilder pb = new ProcessBuilder("mvn.cmd", "-f", pom.get(),
                                            "dependency:tree", "-DoutputFile=" + tmpFile);
                                    pb.directory(dir.toFile());

                                    Process p = pb.start();

                                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

                                    while(true)
                                    {
                                        String consoleLine = br.readLine();

                                        if(consoleLine != null)
                                            System.out.println(consoleLine);

                                        if(!p.isAlive())
                                        {
                                            br.close();
                                            break;
                                        }
                                    }

                                    p.waitFor();

                                    System.out.println("Process completed.");
                                    BytecodeArtifact artifact = buildByteCodeArtifact(tmpFile);

                                    if(artifact != null)
                                    {
                                        System.out.println("\nResults");
                                        System.out.println(artifact.getCoordinate());
                                        Arrays.stream(artifact.getDependencies()).forEach(System.out::println);
                                    }

                                } catch(IOException | InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    catch(IOException e)
                    {
                        System.out.println("Skipping " + dir);
                        e.printStackTrace();
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                {
                    System.out.println(file.toString());

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static BytecodeArtifact buildByteCodeArtifact(Path path)
    {
        BytecodeArtifact artifact = new BytecodeArtifact();
        Pattern p = Pattern.compile("\\W*(\\w.*)");

        try
        {
            BufferedReader br = Files.newBufferedReader(path);

            ArrayList<Dependency> dependencies = new ArrayList<>();

            boolean firstLine = true;
            String line = br.readLine();
            while(line != null)
            {
                System.out.println("LINE = " + line);
                Matcher m = p.matcher(line);

                if(!m.matches())
                {
                    System.out.println("Unable to parse maven dependency output.");
                    throw new RuntimeException("REGEX MISMATCH");
                }

                String[] parts = m.group(1).split(":");

                if(parts.length != 4 && parts.length != 5)
                    throw new RuntimeException("Unable to parse maven dependency output.");

                BytecodeArtifactCoordinate coordinate = new BytecodeArtifactCoordinate();
                coordinate.setGroupId(parts[0]);
                coordinate.setArtifactId(parts[1]);
                coordinate.setVersion(parts[3]);

                if(firstLine)
                {
                    artifact.setCoordinate(coordinate);
                    firstLine = false;
                }
                else
                {
                    Dependency d = new Dependency();

                    d.coordinate = coordinate;
                    d.type = parts[2];
                    d.scope = parts[4];

                    dependencies.add(d);
                }

                line = br.readLine();
            }

            br.close();

            artifact.setDependencies(dependencies.toArray(new Dependency[0]));

        } catch(IOException e)
        {
            e.printStackTrace();
        }

        return artifact;
    }
}
