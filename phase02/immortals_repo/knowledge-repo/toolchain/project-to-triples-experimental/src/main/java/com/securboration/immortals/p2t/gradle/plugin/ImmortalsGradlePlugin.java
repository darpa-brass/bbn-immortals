package com.securboration.immortals.p2t.gradle.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.securboration.immortals.repo.etc.WebServiceStrings;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.xml.ws.http.HTTPException;


public class ImmortalsGradlePlugin implements Plugin<Project>{

    public static class ImmortalsPluginExtension {
        
        private boolean staticAnalysisEnabled = false;

        private String targetDir = null;
        
        private String[] includedLibs;
        
        private boolean completeAnalysis;
        
        private String vcsAnchor;

        public boolean isStaticAnalysisEnabled() {
            return staticAnalysisEnabled;
        }

        public void setStaticAnalysisEnabled(boolean value) {
            staticAnalysisEnabled = value;
        }

        public String getTargetDir() {
            return targetDir;
        }

        public String[] getIncludedLibs() {
            return includedLibs;
        }

        public void setIncludedLibs(String[] includedLibs) {
            this.includedLibs = includedLibs;
        }

        public boolean isCompleteAnalysis() {
            return completeAnalysis;
        }

        public void setCompleteAnalysis(boolean completeAnalysis) {
            this.completeAnalysis = completeAnalysis;
        }

        public String getVcsAnchor() {
            return vcsAnchor;
        }

        public void setVcsAnchor(String vcsAnchor) {
            this.vcsAnchor = vcsAnchor;
        }
    }
    
    
	/**
	 * These are blocked from being read
	 */
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
	
	private static final String splitter = System.getProperty("path.separator");
	
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
	private static final String TASK_GROUP = "IMMoRTALS";

	@Override
    public void apply(Project p) {
	    
	    p.getExtensions().add("krgp", new ImmortalsPluginExtension());

		Task bytecode = p.getTasks().create("bytecode", BytecodeGradleTask.class);
		bytecode.setGroup(TASK_GROUP);
		bytecode.setDescription("Perform bytecode-triple generation for the current project.");
		
		Task constraint = p.getTasks().create("constraint", ConstraintGradleTask.class);
		constraint.setGroup(TASK_GROUP);
		constraint.setDescription("Enforce all constraints on current ontology.");
		
		Task cleanUp = p.getTasks().create("cleanup", PluginCleanupGradleTask.class);
		cleanUp.shouldRunAfter(bytecode, constraint);
		cleanUp.setGroup(TASK_GROUP);
		cleanUp.setDescription("Cleanup tools, emit results.");
		
		Task all = p.getTasks().create("krgp").dependsOn(bytecode, constraint, cleanUp);
		all.setGroup(TASK_GROUP);
		all.setDescription("Executes the bytecode, constraint, and frame tasks.");

    }
}
