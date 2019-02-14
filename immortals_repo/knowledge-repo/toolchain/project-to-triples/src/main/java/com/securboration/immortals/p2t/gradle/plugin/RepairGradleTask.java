package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.*;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class RepairGradleTask extends ImmortalsGradleTask {

    @TaskAction
    public void repair() throws IOException {

        Project p = getProject();

        String projArtifactId = p.getName();
        String projGroupId = p.getGroup().toString();
        String projVersion = p.getVersion().toString();

        String pluginOutput = null;
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        try {
            ImmortalsGradlePlugin.ImmortalsPluginExtension extension = (ImmortalsGradlePlugin.ImmortalsPluginExtension) p.getExtensions().getByName("krgp");
            //TODO
            pluginOutput = extension.getTargetDir();
        } catch (UnknownDomainObjectException exc) {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        }

        if (pluginOutput == null) {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        }

        GradleTaskHelper taskHelper = new GradleTaskHelper(client, null, pluginOutput, p.getName());

        String path = taskHelper.getResultsDir();
        List<File> files = new ArrayList<>();

        {//get a list of all TTL files to process
            File pathFile = new File(path);

            if(!pathFile.exists()){
                throw new RuntimeException("no file found for path " + path);
            }

            if (pathFile.isDirectory()) {
                files.addAll(FileUtils.listFiles(pathFile, new String[]{"ttl"}, true));
            } else if (pathFile.isFile()) {
                files.add(pathFile);
            } else {
                throw new RuntimeException(path + " is not a normal file or dir");
            }
        }

        Model m = ModelFactory.createDefaultModel();
        {//load everything into fuseki
            for(File f:files){
                System.out.println("READING IN: " + f.getName());
                if (!(f.length() > 50000000)) {
                    try {
                        m.read(
                                new ByteArrayInputStream(FileUtils.readFileToByteArray(f)),
                                null,
                                "TURTLE"
                        );
                    } catch (Exception exc) {
                        if (f.getParentFile().getName().contains("Pax")) {
                            System.out.println("read pax file...");
                            continue;
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        String projUUID = null;
        StmtIterator projectIter = m.listStatements(new SimpleSelector(null,
                m.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasCoordinate"), (RDFNode)null));
        while (projectIter.hasNext()) {

            Statement projectStmt = projectIter.nextStatement();
            Resource projectCoord = projectStmt.getObject().asResource();

            StmtIterator projectGroupIdIter = m.listStatements(new SimpleSelector(projectCoord,
                    m.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasGroupId"), (RDFNode) null));

            if (projectGroupIdIter.hasNext()) {
                Statement groupIdStmt = projectGroupIdIter.nextStatement();
                String groupId = groupIdStmt.getObject().asLiteral().getString();

                if (!groupId.equals(projGroupId)) {
                    continue;
                }
            }

            StmtIterator projectVersionIter = m.listStatements(new SimpleSelector(projectCoord,
                    m.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasVersion"), (RDFNode) null));

            if (projectVersionIter.hasNext()) {
                Statement versionStmt = projectVersionIter.nextStatement();
                String version = versionStmt.getObject().asLiteral().getString();

                if (!version.equals(projVersion)) {
                    continue;
                }
            }

            StmtIterator projectArtifactIter = m.listStatements(new SimpleSelector(projectCoord,
                    m.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasArtifactId"), (RDFNode) null));

            if (projectArtifactIter.hasNext()) {
                Statement artifactStmt = projectArtifactIter.nextStatement();
                String artifact = artifactStmt.getObject().asLiteral().getString();

                if (artifact.equals(projArtifactId)) {
                    projUUID = projectStmt.getSubject().getURI();
                    break;
                }
            }
        }


        FusekiClient client = new FusekiClient("http://localhost:3030/ds");
        m.add(client.getModel(client.getFusekiServiceDataUrl() + "/" + projUUID + "-AdaptArtifacts"));

        List<BytecodeArtifactCoordinate> newDependencies = new ArrayList<>();
        List<Resource> analysisImpacts = new ArrayList<>();
        StmtIterator iter = m.listStatements(new SimpleSelector(null, m.createProperty(
                "http://darpa.mil/immortals/ontology/r2.0.0#hasAnalysisImpacts"), (RDFNode) null));

        Resource assessmentReportResource = null;
        while (iter.hasNext()) {

            Statement s = iter.nextStatement();

            assessmentReportResource = s.getSubject();
            if (s.getObject().isResource()) {
                analysisImpacts.add(s.getObject().asResource());
            }
        }


        for (Resource analysisImpact : analysisImpacts) {

           // StmtIterator testIter = m.listStatements(new SimpleSelector(analysisImpact, null, (RDFNode) null));

            //while (testIter.hasNext()) {
           //     Statement s = testIter.nextStatement();
          //      System.out.println("DEBUG");
          //  }


            StmtIterator impactProjIter = m.listStatements(new SimpleSelector(analysisImpact, null, (RDFNode) null) {
                @Override
                public boolean selects(Statement s) {
                    return s.getPredicate().getLocalName().equals("hasProjectUUID");
                }
            });

            if (impactProjIter.hasNext()) {
                Statement impactProjStmt = impactProjIter.nextStatement();
                String impactProjUUID = impactProjStmt.getObject().asLiteral().getString();

                if (!impactProjUUID.equals(projUUID)) {
                    continue;
                }
            } else {
                continue;
            }

            StmtIterator impactIter = m.listStatements(new SimpleSelector(analysisImpact, null, (RDFNode) null) {
                @Override
                public boolean selects(Statement s) {
                    return s.getPredicate().getLocalName().equals("hasNewDependencies");
                }
            });

            List<Resource> dependencyCoords = new ArrayList<>();
            while (impactIter.hasNext()) {

                Statement s = impactIter.nextStatement();

                if (s.getObject().isResource()) {
                    dependencyCoords.add(s.getObject().asResource());
                }
            }

            for (Resource dependencyCoord : dependencyCoords) {

                StmtIterator dependencyGroupIter = m.listStatements(new SimpleSelector(dependencyCoord, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement s) {
                        return s.getPredicate().getLocalName().equals("hasGroupId");
                    }
                });

                BytecodeArtifactCoordinate newDependency = new BytecodeArtifactCoordinate();
                if (dependencyGroupIter.hasNext()) {
                    Statement dependencyGroupStmt = dependencyGroupIter.nextStatement();
                    newDependency.setGroupId(dependencyGroupStmt.getObject().asLiteral().getString());
                }

                StmtIterator dependencyArtifactIter = m.listStatements(new SimpleSelector(dependencyCoord, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement s) {
                        return s.getPredicate().getLocalName().equals("hasArtifactId");
                    }
                });

                if (dependencyArtifactIter.hasNext()) {
                    Statement dependencyArtifactStmt = dependencyArtifactIter.nextStatement();
                    newDependency.setArtifactId(dependencyArtifactStmt.getObject().asLiteral().getString());
                }

                StmtIterator dependencyVersionIter = m.listStatements(new SimpleSelector(dependencyCoord, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement s) {
                        return s.getPredicate().getLocalName().equals("hasVersion");
                    }
                });

                if (dependencyVersionIter.hasNext()) {
                    Statement dependencyVersionStmt = dependencyVersionIter.nextStatement();
                    newDependency.setVersion(dependencyVersionStmt.getObject().asLiteral().toString());
                }

                newDependencies.add(newDependency);
            }

            StmtIterator augmentedSourceIter = m.listStatements(new SimpleSelector(analysisImpact, null, (RDFNode) null) {
                @Override
                public boolean selects(Statement s) {
                    return s.getPredicate().getLocalName().equals("hasAugmentedUserFile");
                }
            });

            String wrapperPackageLine = null;
            String wrapperClassDir = null;
            if (augmentedSourceIter.hasNext()) {

                Statement augmentedUserSourceStmt = augmentedSourceIter.nextStatement();
                Resource augmentedUserSourceResource = augmentedUserSourceStmt.getObject().asResource();

                StmtIterator newSourceIter = m.listStatements(new SimpleSelector(augmentedUserSourceResource, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement s) {
                        return s.getPredicate().getLocalName().equals("hasSource");
                    }
                });

                String newSource = null;
                if (newSourceIter.hasNext()) {
                    Statement newSourceStmt = newSourceIter.nextStatement();
                    newSource = newSourceStmt.getObject().asLiteral().getString();
                }

                wrapperPackageLine = newSource.substring(0, newSource.indexOf(";") + 1);

                StmtIterator fullNameIter = m.listStatements(new SimpleSelector(augmentedUserSourceResource, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement s) {
                        return s.getPredicate().getLocalName().equals("hasFullyQualifiedName");
                    }
                });

                String fullName = null;
                if (fullNameIter.hasNext()) {
                    Statement fullNameStmt = fullNameIter.nextStatement();
                    fullName = fullNameStmt.getObject().asLiteral().getString();
                    final String normalizedName = fullName.replace(".", File.separator);

                    List<String> sourceFilePaths = new ArrayList<>(Arrays.asList(p.fileTree("src").getAsPath().split(
                            System.getProperty("path.separator"))));
                    Optional<String> filePathOption = sourceFilePaths.stream().filter(filePath -> filePath.contains(normalizedName)).findFirst();

                    if (filePathOption.isPresent()) {
                        wrapperClassDir = filePathOption.get().substring(0, filePathOption.get().lastIndexOf(File.separator));
                        FileUtils.writeStringToFile(new File(filePathOption.get()), newSource, Charset.defaultCharset());
                    }
                }
            }

            StmtIterator wrapperClassIter = m.listStatements(new SimpleSelector(analysisImpact, null, (RDFNode) null) {
                @Override
                public boolean selects(Statement s) {
                    return s.getPredicate().getLocalName().equals("hasProducedSourceFiles");
                }
            });

            Resource completeWrapperClass = null;
            String wrapperClassFileName = null;
            while (wrapperClassIter.hasNext()) {
                Statement wrapperClassStmt = wrapperClassIter.nextStatement();
                Resource wrapperClassTester = wrapperClassStmt.getObject().asResource();

                StmtIterator wrapperClassTesterIter = m.listStatements(new SimpleSelector(wrapperClassTester, null, (RDFNode) null) {

                    @Override
                    public boolean selects(Statement s) {
                        return s.getPredicate().getLocalName().equals("hasFileName");
                    }
                });

                if (wrapperClassTesterIter.hasNext()) {
                    Statement wrapperClassTesterFileName = wrapperClassTesterIter.nextStatement();
                    String tempWrapperClassFileName = wrapperClassTesterFileName.getObject().asLiteral().getString();
                    if (!tempWrapperClassFileName.contains("[")) {
                        completeWrapperClass = wrapperClassTester;
                        wrapperClassFileName = tempWrapperClassFileName;
                        break;
                    }
                }
            }

            StmtIterator wrapperClassSourceIter = m.listStatements(new SimpleSelector(completeWrapperClass, null, (RDFNode)null) {
                @Override
                public boolean selects(Statement s) {
                    return s.getPredicate().getLocalName().equals("hasSource");
                }
            });

            if (wrapperClassSourceIter.hasNext()) {
                Statement wrapperClassSourceStmt = wrapperClassSourceIter.nextStatement();
                String wrapperClassSource = wrapperClassSourceStmt.getObject().asLiteral().getString();
                wrapperClassSource = (wrapperPackageLine + "\n") + wrapperClassSource;

                File newWrapperClassFile = new File(wrapperClassDir + File.separator + wrapperClassFileName);

                FileUtils.writeStringToFile(newWrapperClassFile, wrapperClassSource);
            }
        }
    }
}
