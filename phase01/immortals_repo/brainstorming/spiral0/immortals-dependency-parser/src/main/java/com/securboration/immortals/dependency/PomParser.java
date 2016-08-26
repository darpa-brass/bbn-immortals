package com.securboration.immortals.dependency;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.bytecode.Dependency;
import com.securboration.immortals.ontology.bytecode.DependencyExclusion;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PomParser
{
    private static final String projectXPath = "/project";
    private static final String parentXPath = "parent";
    private static final String groupIdXPath = "groupId";
    private static final String artifactIdXPath = "artifactId";
    private static final String versionXPath = "version";
    private static final String dependencyXPath = "/project/dependencies/dependency";

    private Document document;
    private List<Dependency> dependencies;
    private BytecodeArtifactCoordinate coordinate;

    public void parse(String pomPath) throws ParserConfigurationException, IOException, SAXException
    {
        dependencies = null;
        coordinate = null;
        document = null;

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        document = builder.parse(new File(pomPath));
    }

    public List<Dependency> getDependencies()
    {
        if(document != null && dependencies == null)
        {
            XPath xpath = XPathFactory.newInstance().newXPath();

            try
            {
                dependencies = new ArrayList<>();
                NodeList dependencyNodes = (NodeList) xpath.evaluate(dependencyXPath, document, XPathConstants.NODESET);

                for(int i = 0; i < dependencyNodes.getLength(); i++)
                {
                    NodeList dependencyProperties = dependencyNodes.item(i).getChildNodes();

                    HashMap<String, String> values = new HashMap<>();
                    List<DependencyExclusion> exclusions = null;

                    for(int j = 0; j < dependencyProperties.getLength(); j++)
                    {
                        Node property = dependencyProperties.item(j);

                        if(property.getNodeType() != Node.ELEMENT_NODE)
                            continue;

                        if(property.getNodeName().equals("exclusions"))
                            exclusions = buildExclusions(property);
                        else
                            values.put(property.getNodeName(), property.getTextContent());
                    }

                    dependencies.add(buildDependency(values, exclusions));
                }
            } catch(XPathExpressionException e)
            {
                dependencies = null;
                e.printStackTrace();
            }
        }

        return dependencies;
    }

    public BytecodeArtifactCoordinate getCoordinate()
    {
        if(document != null && coordinate == null)
        {
            try
            {
                coordinate = new BytecodeArtifactCoordinate();

                XPath xpath = XPathFactory.newInstance().newXPath();

                Node project = (Node) xpath.evaluate(projectXPath, document, XPathConstants.NODE);
                Node parent = (Node) xpath.evaluate(parentXPath, project, XPathConstants.NODE);

                if(parent != null)
                    System.out.println("FOUND PARENT NODE.");

                // Get the GroupId
                Node groupId = (Node) xpath.evaluate(groupIdXPath, project, XPathConstants.NODE);
                if(groupId == null)
                    groupId = (Node) xpath.evaluate(groupIdXPath, parent, XPathConstants.NODE);
                coordinate.setGroupId(groupId.getTextContent());

                // Get the ArtifactId
                Node artifactId = (Node) xpath.evaluate(artifactIdXPath, project, XPathConstants.NODE);
                coordinate.setArtifactId(artifactId.getTextContent());

                // Get the Version
                Node version = (Node) xpath.evaluate(versionXPath, project, XPathConstants.NODE);
                if(version == null)
                    version = (Node) xpath.evaluate(versionXPath, parent, XPathConstants.NODE);
                coordinate.setVersion(version.getTextContent());

            } catch(XPathExpressionException e)
            {
                coordinate = null;
                e.printStackTrace();
            }
        }

        return coordinate;
    }

    private List<DependencyExclusion> buildExclusions(Node exclusionsNode)
    {
        ArrayList<DependencyExclusion> exclusions = new ArrayList<>();

        NodeList nodes = exclusionsNode.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node exclusionNode = nodes.item(i);

            if(exclusionNode.getNodeType() != Node.ELEMENT_NODE)
                continue;

            DependencyExclusion exclusion = new DependencyExclusion();

            Node exclusionChild = exclusionNode.getFirstChild();
            while(exclusionChild != null)
            {
                if(exclusionChild.getNodeType() == Node.ELEMENT_NODE)
                {
                    if(exclusionChild.getNodeName().equals("groupId"))
                        exclusion.groupId = exclusionChild.getTextContent();
                    else
                        exclusion.artifactId = exclusionChild.getTextContent();
                }

                exclusionChild = exclusionChild.getNextSibling();
            }

            exclusions.add(exclusion);
        }

        return exclusions;
    }

    private Dependency buildDependency(Map<String, String> values)
    {
        return buildDependency(values, null);
    }

    private Dependency buildDependency(Map<String, String> values, List<DependencyExclusion> exclusions)
    {
        Dependency dependency = new Dependency();
        BytecodeArtifactCoordinate coordinate = new BytecodeArtifactCoordinate();

        for(Map.Entry<String, String> entry: values.entrySet())
        {
            try
            {
                Field f = dependency.getClass().getField(entry.getKey());
                f.set(dependency, entry.getValue());
            }
            catch(NoSuchFieldException e1)
            {
                // Check if its the coordinate
                try
                {
                    Field f = coordinate.getClass().getDeclaredField(entry.getKey());
                    f.setAccessible(true);
                    f.set(coordinate, entry.getValue());
                } catch(NoSuchFieldException e2)
                {
                    e2.printStackTrace();
                } catch(IllegalAccessException e2)
                {
                    e2.printStackTrace();
                }
            }
            catch(IllegalAccessException e1)
            {
                e1.printStackTrace();
            }
        }

        if(exclusions != null)
            dependency.exclusions = exclusions.toArray(new DependencyExclusion[0]);

        dependency.coordinate = coordinate;

        return dependency;
    }

    public static void main(String [] args)
    {
        System.out.println("Processing: src/test/resources/LocationProviderAndroidGpsBuiltIn-1.0-LOCAL.pom");

        PomParser parser = new PomParser();

        try
        {
            parser.parse("src/test/resources/LocationProviderAndroidGpsBuiltIn-1.0-LOCAL.pom");
            System.out.println(parser.getCoordinate());
            parser.getDependencies().forEach(System.out::println);
            System.out.println();
        } catch(ParserConfigurationException | SAXException | IOException e )
        {
            e.printStackTrace();
            return;
        }

        try(Stream<Path> stream = Files.walk(Paths.get("../../../shared/IMMORTALS_REPO/mil/darpa/immortals/dfus")))
        {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("pom"))
                    .map(Path::toString)
                    .forEach(p -> {
                        System.out.println("Processing: " + p);
                        try
                        {
                            parser.parse(p);
                            System.out.println(parser.getCoordinate());
                            parser.getDependencies().forEach(System.out::println);
                        } catch(ParserConfigurationException | IOException | SAXException e)
                        {
                            System.out.println("Parsing failed!");
                            e.printStackTrace();
                        }
                        System.out.println();
                    });
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("\nPrinting paths in M2 with walkFileTree.");
        try
        {
            Path dir = Paths.get("../../../shared/IMMORTALS_REPO/");
            //Path dir = Paths.get("C:\\Users\\Adam\\.m2\\repository");

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
                            System.out.println("Found JAR.");
                            Optional<String> pomPath = files.stream()
                                    .filter(p -> p.getFileName().toString().endsWith(".pom"))
                                    .map(Path::toString)
                                    .findFirst();

                            if(pomPath.isPresent())
                            {
                                try
                                {
                                    parser.parse(pomPath.get());
                                    System.out.println(parser.getCoordinate());
                                    parser.getDependencies().forEach(System.out::println);
                                } catch(ParserConfigurationException | SAXException | IOException e)
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
}
