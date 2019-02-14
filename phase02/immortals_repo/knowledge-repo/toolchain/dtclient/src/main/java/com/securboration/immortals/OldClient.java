package com.securboration.immortals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.dom4j.DocumentException;
import org.springframework.web.client.RestTemplate;

/**
 * BACKUP OF OLD VERSION
 * @author Clayton
 *
 */
public class OldClient {
	
	/**
	 * Used on a path to a .jar file and extracts info from the internal (to the jar) pom file
	 * @param p path to a jar file
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static HashMap<String,Object> processPath(Path p) throws IOException, DocumentException{
		HashMap<String,Object> sb = new HashMap<String,Object>();
		String[] jarInfo;
		byte[] bytes = Files.readAllBytes(p);
		sb.put("bytes", bytes);
		sb.put("name", p.getFileName());
		jarInfo = extractPOMInfoFromJar(p.toFile());
		sb.put("groupName", jarInfo[0]);
		sb.put("artifactId", jarInfo[1]);
		sb.put("version", jarInfo[2]);
		return sb;
	}
	
	/**
	 * Takes in a path to a jar file as well as a set of arguments defining the artifactId, groupId, and version.
	 * Does no extraction from POM files, requires manual input.
	 * @param p path to a jar file
	 * @param args
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,Object> processPathManual(Path p, String[] args) throws IOException{
		HashMap<String,Object> sb = new HashMap<String,Object>();
		byte[] bytes = Files.readAllBytes(p);
		sb.put("bytes", bytes);
		sb.put("name", p.getFileName());
		sb.put("groupName", args[1]);
		sb.put("artifactId", args[2]);
		sb.put("version", args[3]);
		return sb;
	}
	
	/**
	 * Reaches into a .jar file and finds the POM file inside, then extracts the information about artifactId, groupId, and version.
	 * @param p a .jar file
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static String[] extractPOMInfoFromJar(File p) throws IOException, DocumentException{
		ZipFile zf = new ZipFile(p);
		Enumeration<? extends ZipEntry> entries = zf.entries();
		ZipEntry entry = null;
		while (entries.hasMoreElements()){
			entry = entries.nextElement();
			if (entry.getName().contains("pom.xml")){
				break;
			}
		}
		System.out.println(entry.getName());
		InputStream in = zf.getInputStream(entry);
		String[] s = DtclientApplication.pomExtraction(in);
		zf.close();
		return s;
	}
	
	public static void sendSingleJar(Path p){
		if (Files.isRegularFile(p, null)){
			
		}
		else{
			
		}
	}
	
	public static void legacyBackup(String[] args) throws Exception{
		ArrayList<String[]> errorPaths = new ArrayList<String[]>();
		RestTemplate rt = new RestTemplate();
		Path p = Paths.get(args[0]);
		HashMap<String,Object> sb = null;
		if (Files.isDirectory(p)){
			DirectoryStream<Path> stream = Files.newDirectoryStream(p);
			for (Path dspath: stream){
				try {
					sb = processPath(dspath);
				} catch (DocumentException e) {
					print("Failed to read pom.xml for jar: " + dspath.getFileName().toString());
					errorPaths.add(new String[]{dspath.getFileName().toString(),e.getMessage()});
				}				
			}
			stream.close();
		}
		else if(Files.isRegularFile(p)){
				System.out.println("is file");
				if (args.length == 4){
					sb = processPathManual(p, args);
				}
				else{
					try {
						sb = processPath(p);
					} catch (DocumentException e) {
						errorPaths.add(new String[]{p.getFileName().toString(),e.getMessage()});
					}
				}
				System.out.println(rt.postForObject("http://localhost:8080/jarsubmit",sb,String.class));
		}
		else{
			System.out.println("Invalid file or directory");
			System.exit(0);
		}
		if(errorPaths.size() != 0){
			DtclientApplication.printErrors(errorPaths);
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
