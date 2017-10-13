package com.securboration.immortals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DtclientApplication {
	public static final String GID = "//*[local-name()='project']/*[local-name()='groupId']";
	public static final String VER = "//*[local-name()='project']/*[local-name()='version']";
	public static final String AID = "//*[local-name()='project']/*[local-name()='artifactId']";
	
	public static final String PGID = "//*[local-name()='parent']/*[local-name()='groupId']";
	public static final String PVER = "//*[local-name()='parent']/*[local-name()='version']";
	//public static final String PAID = "//*[local-name()='parent']/*[local-name()='artifactId']"; //should not need
	
	public static final String submitDestination = "http://localhost:8080/jarsubmit";
	
	/**
	 * Prints text to command line explaining basic usage.
	 */
	public static void printInstructions(){
		print("Pass in a directory as arg[0]. Will recursively travel this folder and all children for jar files.\nThere must be a pom file of the same name as each jar for each jar file\n"+
				"If you pass it in a single jar file, its folder must contain a .pom of the same name");
		System.exit(0);
	}
	
	/**
	 * Takes a path to a directory or jar file. Takes jar file. Finds pom file of same name, extracts artifact information. Sends to Jar Ingestor at 'submitDestination' static class variable.
	 * submitDestination needs to be a running immortals-repository-service server.
	 * @param args args[0] needs to be a directory or jar file.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		long time = System.currentTimeMillis();
		if (args.length != 1 || args[0] == null){
			printInstructions();
			System.exit(0);
		}
		
		Path p = Paths.get(args[0]);
		if (Files.isDirectory(p)){
				walkDirectoryTree(p);
		}
		else if(Files.isRegularFile(p)){
			try {
				processDirectoryItem(p);
			} catch (Exception e) {
				print("Injest failed. Stack trace: ");
				e.printStackTrace();
			}
		}
		else{
			print("That's not a valid input.");
			printInstructions();
		}
		long ms = System.currentTimeMillis() - time;
		System.out.format("Program run time %d:%d:%d", (ms / (1000 * 60 * 60)) % 24,(ms / (1000 * 60)) % 60,(ms / 1000) % 60);
	}
	
	/**
	 * Errors in ArrayList should come in a 2 length String[] representing {filename, error_message}
	 * @param errorList
	 */
	public static void printErrors(ArrayList<String[]> errorList){
		if (errorList.size() != 0){
			print("Errors encountered: jar/error ");
			try{
				errorList.forEach(x -> System.out.print(x[0] + " Error: " + x[1] + "\n"));
			}
			catch (Exception e){
				System.out.println("Error in this entry, length of error array: " + String.valueOf(errorList.size()));
			}
		}
		else{
			print("No errors caught");
		}
	}
	
	/**
	 * Main loop for traversing file structure. Calls processDirectoryItem() on each jar it finds.
	 * @param p Starting parent directory
	 * @throws IOException
	 */
	public static void walkDirectoryTree(Path p) throws IOException{
		ArrayList<String[]> errors = new ArrayList<String[]>();
		Stream<Path> s = Files.walk(p).filter(x -> x.toString().endsWith(".jar"));
		s.parallel().forEach(t -> {
			try {
				processDirectoryItem(t);
			} catch (Exception e) {
				String[] error = {t.toString(),e.getMessage()};
				errors.add(error);
			}
		});
		printErrors(errors);
	}
	
	/**
	 * Processes a jar file. Calls pomExtraction and submitItem.
	 * @param p path to a jar file
	 * @throws Exception
	 */
	public static void processDirectoryItem(Path jar) throws Exception{
		if (!Files.exists(jar) || !Files.isRegularFile(jar)){
			throw new IOException("this path doesn't have a jar or it doesn't exist");
		}
		HashMap<String,Object> sb = new HashMap<String,Object>();
		Path pom = Paths.get(jar.toString().replace(".jar", ".pom"));
		print(jar);
		if (Files.exists(pom)){
			String[] pomInfo = pomExtraction(Files.newInputStream(pom));
			if (pomInfo == null){
				throw new IOException("Pom read failure");
			}
			byte[] bytes = Files.readAllBytes(jar);
			sb.put("name", jar.getFileName());
			sb.put("bytes", bytes);
			sb.put("groupName", pomInfo[0]);
			sb.put("artifactId", pomInfo[1]);
			sb.put("version", pomInfo[2]);
			
			String returnstatus = submitItem(sb);
			if (returnstatus == null){
				print("Server error");
				throw new Exception("Server error");
			}
		}
		else{
			print("Pom error");
			throw new IOException("No pom file\n");
		}
	}
	
	/**
	 * Helper function, actually does the work of extracting info from a pom file. The InputStream should be one from the pom file
	 * @param in InputStream from a pom file
	 * @return String[] {groupId, artifactId, version} 
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static String[] pomExtraction(InputStream in) throws IOException, DocumentException{
		SAXReader reader = new SAXReader();
		Document d = reader.read(in);
		String[] s;
		try{
			String gidN = d.selectSingleNode(GID).getText();
			String aidN = d.selectSingleNode(AID).getText();
			String verN = d.selectSingleNode(VER).getText();
			s = new String[]{gidN,aidN,verN};
		}
		catch (NullPointerException ex){
			try{
				String gidN = d.selectSingleNode(PGID).getText();
				String aidN = d.selectSingleNode(AID).getText();
				String verN = d.selectSingleNode(PVER).getText();
				s = new String[]{gidN,aidN,verN};
			}
			catch(NullPointerException ex2){
				print("POMREAD fail");
				in.close();
				return null;
			}
		}
		finally{
			in.close();
		}
		return s;
	}
	
	/**
	 * Submits item to class variable submitDestination
	 * @param sb class file to submit
	 * @return
	 */
	public static String submitItem(HashMap<String,Object> sb){
		RestTemplate rt = new RestTemplate();
		try{
			return rt.postForObject(submitDestination,sb,String.class);
		}
		catch (HttpServerErrorException e){
			return null;
		}
	}
	
	/**
	 * Convenience function
	 * @param o
	 */
	public static void print(Object o){
		System.out.println(o.toString());
	}
}
