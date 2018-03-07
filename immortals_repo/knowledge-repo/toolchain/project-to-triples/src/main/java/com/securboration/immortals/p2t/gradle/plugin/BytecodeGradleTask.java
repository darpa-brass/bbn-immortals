package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.project2triples.GradleData;
import com.securboration.immortals.project2triples.ProjectToTriplesMain;
import com.securboration.immortals.repo.etc.WebServiceStrings;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BytecodeGradleTask extends ImmortalsGradleTask {

    private static ArrayList<String> skip = new ArrayList<>();
    {
        skip.add("api");
        skip.add("apiElements");
        skip.add("implementation");
        skip.add("runtimeElements");
        skip.add("runtimeOnly");
        skip.add("testImplementation");
        skip.add("testRuntimeOnly");

    }

    private void pushToContext(String serviceAddress,
                               String contextID,
                               String pathToGraphs,
                               FileWriter fw) throws IOException {
        WebServiceStrings serviceStrings = new WebServiceStrings(serviceAddress);
        RestTemplate restTemplate = new RestTemplate();
        byte[] encoded = null;
        String url;
        List<String> graphNames = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles((new File(pathToGraphs)), null, true);
        fw.write("Done. Gathering graphs...\n");
        for (File file : files) {
            fw.write("Found graph: " + file.getName());
            try {
                encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                String model = new String(encoded, Charset.defaultCharset());
                url = serviceStrings.createGraphUrl(file.getParentFile().getName());
                String newGraphName = restTemplate.postForObject(
                        url, model,
                        String.class
                );
                fw.write(". Added to database as: " + newGraphName + "\n");
                graphNames.add(newGraphName);
            } catch (IOException exc) {
                fw.write("ERROR: Unable to parse graph file, " + file.getName());
            }
        }

        fw.write("Added " + files.size() + " graphs to database.\n");
        fw.write("Pairing graphs with provided context identifier...\n");

        HttpClient client = HttpClients.createDefault();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));

        for (String graphName : graphNames) {
            url = serviceStrings.addGraphToContextUrl(contextID, graphName);
            // TODO whether to catch error and try pairing as many as was we can, or hard stop...
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, null, String.class);

            if (!response.getBody().equals("Contextual Pairing Successful")) {
                fw.write("WARNING: Contextual Pairing Unsuccessful with: " + graphName + "\n");
            } else {
                fw.write(response.getBody() + " with graph: " + graphName + "\n");
            }
        }

        fw.write("Added " + files.size() + " graphs to context: " + contextID + "\n");
        fw.write("Completed context creation.\n");
        url = serviceStrings.pushContextUrl(contextID);

        try {
            fw.write("Merging context graphs and pushing to fuseki...\n");
            restTemplate.postForObject(url, null, String.class);
            fw.write("Completed push. Uber graph describing context: " + contextID + " is able to be queried.");
        } catch (HTTPException exc) {
            fw.write("WARNING: Context graph(s) were unable to be pushed to fuseki instance");
        }
    }
    
    @TaskAction
    public void analysis() throws FileNotFoundException {
        Project p = getProject();
        String pluginOutput = null;
        ArrayList<String> includedLibs = null;
        try {
            ImmortalsGradlePlugin.ImmortalsPluginExtension extension = (ImmortalsGradlePlugin.ImmortalsPluginExtension) p.getExtensions().getByName("krgp");
            pluginOutput = extension.getTargetDir();
            includedLibs = new ArrayList<>();
            includedLibs.addAll(Arrays.asList(extension.getIncludedLibs()));
        } catch (UnknownDomainObjectException exc) {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        } catch (Exception exc) {
            includedLibs = null;
        }
        ProjectToTriplesMain p2tm = new ProjectToTriplesMain();
        GradleTaskHelper taskHelper = new GradleTaskHelper(client, null, pluginOutput, p.getName());
        //initializeReportsDirectory(pluginOutput + "/" + p.getName());
       // this.getTaskHelper().setPw(new PrintWriter(new FileOutputStream(getLogPath(pluginOutput + "/" + p.getName(),
                //GradleTaskHelper.TaskType.BYTECODE), true)));
        GradleData data = new GradleData();
        
        String version = p.getVersion().toString();
        if (version != null && !version.equals("")){
            data.setImmortalsVersion(version);
        }
        else{
            data.setImmortalsVersion("[ERROR:Version_Unsupplied]");
        }

        ArrayList<String> outputArtifact = new ArrayList<String>(Arrays.asList(p.fileTree("build/outputs").getAsPath().split(splitter)));
        for(int i = 0; i < outputArtifact.size(); i++){
            if (outputArtifact.get(i).contains(".apk")){
                //data.setCompiledProjectJarPath(outputArtifact.get(i));
            }else if(outputArtifact.get(i).contains(".jar")){
                data.setCompiledProjectJarPath(outputArtifact.get(i));
            }
        }

        HashMap<String,ArrayList<String>> cp2c = new HashMap<>();
        
        for (String listName : p.getConfigurations().getNames()) {
            if (!skip.contains(listName)) {
                cp2c.put(listName, readClasspath(p, listName));
            }
        }
        
        data.setClasspathNameToClasspathList(cp2c);

        data.setCompiledProjectName(p.getName());
        data.setPathToBuildFile(p.getBuildFile().getAbsolutePath());
        data.setBaseDir(p.getRootDir().getAbsolutePath());
        data.setProperties(p.getProperties());
        System.out.println(p.getRootDir().getAbsolutePath());

        ArrayList<String> myList = new ArrayList<String>(Arrays.asList(p.fileTree("build").getAsPath().split(splitter)));
        ArrayList<String> classFiles = new ArrayList<>();
        ArrayList<String> testClassFiles = new ArrayList<>();

        for(int i = 0; i < myList.size(); i++){
            String classFile = myList.get(i);
            if(classFile.contains(".class")){
                if (classFile.contains("/test/") || classFile.contains("\\test\\")){
                    testClassFiles.add(classFile);
                }
                else{
                    classFiles.add(myList.get(i));
                }
            }
        }
        data.setClassFilesRoot(p.file("build").getAbsolutePath());
        data.setClassFilePaths(classFiles);
        data.setTestClassFilePaths(testClassFiles);

        myList = new ArrayList<String>(Arrays.asList(p.fileTree("src").getAsPath().split(splitter)));
        ArrayList<String> sourceFiles = new ArrayList<>();
        ArrayList<String> testSourceFiles = new ArrayList<>();
        for(int i = 0; i < myList.size(); i++){
            String sourceFile = myList.get(i);
            if(sourceFile.contains(".java")){
                if (sourceFile.contains("/test/") || sourceFile.contains("\\test\\")){
                    testSourceFiles.add(sourceFile);
                }
                else{
                    sourceFiles.add(sourceFile);
                }
            }
        }

        myList = new ArrayList<String>(Arrays.asList(p.fileTree("build/generated").getAsPath().split(splitter)));
        for(int i = 0; i < myList.size(); i++){
            String sourceFile = myList.get(i);
            if(sourceFile.contains(".java")){
                sourceFiles.add(sourceFile);
            }
        }
        
        data.setSourceFilePaths(sourceFiles);
        data.setTestSourceFilePaths(testSourceFiles);

        try {
            String result = "";
            initializeReportsDirectory(pluginOutput + "/" + p.getName());
            taskHelper.setPw(new PrintWriter(new FileOutputStream(getLogPath(pluginOutput + "/" + p.getName(),
                    GradleTaskHelper.TaskType.BYTECODE), true)));
            result = p2tm.gradleDataToTriples(data, taskHelper, includedLibs);
            GradleTaskHelper.recordCPElement(result, taskHelper.getResultsDir() + p.getName() + "/structures/" + p.getName()
            + "-projectOutput.ttl");
            
            String pushContext = String.valueOf(data.getProperty("pushContext"));
            if (Boolean.parseBoolean(pushContext)) {

                FileWriter fw = new FileWriter("pushContextLog.txt");
                fw.write("Beginning immortals context creation.\n");
                fw.write("Gathering configuration options...\n");

                String serviceAddress = String.valueOf(data.getProperty("repoServiceAddress"));
                String contextID = String.valueOf(data.getProperty("repoServiceContextID"));
                String pathToGraphs = String.valueOf(data.getProperty("krgpDirectory"));

                pushToContext(serviceAddress, contextID, p.getProjectDir().getAbsolutePath() + pathToGraphs, fw);

                fw.flush();
                fw.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("-----------");
            System.out.println(e.getMessage());
        }
    }

    private ArrayList<String> readClasspath(Project p, String var){
        ArrayList<String> items = new ArrayList<>();
        
        Configuration conf = p.getConfigurations().getByName(var);
        Set<File> tree = null;
        try{
            tree = conf.getAsFileTree().getFiles();
        }
        catch (Exception e){
            tree = new HashSet<>();
        }
        
        for (File f : tree){
            items.add(f.getAbsolutePath());
        }
        return items;
    }

    private static final String splitter = System.getProperty("path.separator");

}
