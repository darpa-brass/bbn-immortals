package com.securboration.immortals.project2triples.dfus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class DfuModuleParser {
    
    private File localRepo;
    private Collection<Model> repoProjects;
    
    public DfuModuleParser(String localRepoPath) {
        File temp = new File(localRepoPath);
        
        if (temp.exists()) {
            localRepo = temp;
        } else {
            System.err.println("Specified local repository path does not exist!");
        }
    }
    
    
    public Collection<File> getProjectFiles() throws IOException, XmlPullParserException {
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        List<Model> models = new ArrayList<>();
        Collection<File> pomFiles = FileUtils.listFiles(localRepo, new String[]{"pom"}, true);
        List<File> projectFiles = new ArrayList<>();

        for (File pomFile : pomFiles) {
            Model pomModel = mavenReader.read(new FileInputStream(pomFile));

            switch (pomModel.getPackaging()) {

                case "jar":
                    File pomDir = pomFile.getParentFile();
                    FilenameFilter fileSuffixFilter = new SuffixFileFilter("jar");
                    File[] jarFiles = pomDir.listFiles(fileSuffixFilter);

                    if (jarFiles.length > 0) {

                        for (File jarFile : jarFiles) {
                            if (jarFile.getName().contains("sources")) {
                                continue;
                            } else {
                                projectFiles.add(jarFile);
                            }
                        }

                    } else {
                        System.out.println("UNABLE TO FIND JAR");
                    }

                    break;
                default:
                    break;

            }

        }
        this.setRepoProjects(models);
        return projectFiles;
    }
    
    public static void main(String[] args) throws IOException, XmlPullParserException {
        String repoPath = "C:\\BBNImmortals\\shared\\IMMORTALS_REPO";
        DfuModuleParser dfuModuleParser = new DfuModuleParser(repoPath);
    }

    public Collection<Model> getRepoProjects() {
        return repoProjects;
    }

    public void setRepoProjects(Collection<Model> repoProjects) {
        this.repoProjects = repoProjects;
    }
}
