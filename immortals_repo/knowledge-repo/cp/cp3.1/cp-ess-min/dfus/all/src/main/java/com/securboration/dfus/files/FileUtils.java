package com.securboration.dfus.files;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileUtils {
	private static void mkdir(
			final String target
			) throws IOException{
	    final Path path = Paths.get(target);
	    
	    if(Files.notExists(path)){
	        Files.createDirectories(path);
	    }
	}
	
	public static String readFileToString(
			final File f,
			final Charset c
			) throws FileNotFoundException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try(FileInputStream fis = new FileInputStream(f)){
			
			boolean stop = false;
			final byte[] buffer = new byte[1024];
			while(!stop) {
				final int bytesRead = fis.read(buffer);
				
				if(bytesRead < 0) {
					stop = true;
				} else {
					out.write(buffer, 0, bytesRead);
				}
			}
		}
		
		return new String(out.toByteArray(),c);
	}
	
	
	public static void writeBytesToFile(
			final String dir,
			final String name,
			final byte[] data
			) throws IOException {
		mkdir(dir);
		
		Files.write(
				Paths.get(new File(dir,name).getAbsolutePath()), 
				data,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE
				);
	}
	
	public static void writeStringToFile(
			final String dir,
			final String name,
			final String s, 
			final Charset c
			) throws IOException {
		writeBytesToFile(
				dir,
				name,
				s.getBytes(c)
				);
	}
	
	public static Collection<File> listFiles(
			final File dir,
			final String suffix
			) throws IOException {
		final List<File> files = new ArrayList<>();
		
		try(Stream<Path> stream = Files.walk(Paths.get(dir.getAbsolutePath()));){
    		stream.forEach(new Consumer<Path>() {

				@Override
				public void accept(Path t) {
					File f = t.toFile();
					if(f.isFile() && f.getAbsolutePath().endsWith(suffix)) {
						files.add(f);
					}
				}
    		});
		}
		
		return files;
	}
}