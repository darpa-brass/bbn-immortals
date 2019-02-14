package com.securboration.immortals.da;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;


public class TraceEngine {
	
	private static String trimPath(String s) {
		if(s.startsWith("./")) {
			return s.substring(2);
		} else if(s.startsWith(".")) {
			return s.substring(1);
		} else if(s.startsWith("/")) {
			return s.substring(1);
		}
		
		return s;
	}
	
	private static boolean isTrace(ArchiveEntry a) {
		return a.getName().endsWith(".trace");
	}
	
	private static boolean startsWithPrefix(
			ArchiveEntry a, 
			String prefix
			) {
		final String prefixName = trimPath(prefix);
		final String archiveName = trimPath(a.getName());
		
		if(archiveName.startsWith(prefixName)) {
			return true;
		}
		
		return false;
	}
	
	private static String getThreadName(String path) {
		
		final int lastForwardSlash = path.indexOf("/",2);
		
		if(lastForwardSlash == -1) {
			return path;
		}
		
		return path.substring(lastForwardSlash+1).replace("/messages/", "/").replace(".trace", "");
	}
	
	
	private static Event parseFromLine(
			final String thread,
			final String line
			) {
		final int indexEndsAt = line.indexOf(":");
		
		final String indexPart = line.substring(0,indexEndsAt).trim();//[0,i-1]
		final String messagePart = line.substring(indexEndsAt+1).trim();//[i+1,L]
		
		final long index = Long.parseLong(indexPart);
		
		return new Event(thread,index,messagePart);
	}
	
	private static void trace(
			final String threadName,
			final BufferedReader reader, 
			final TraceReplayEngine listener
			) throws IOException {
		listener.beforeThread(threadName);
			
		boolean stop = false;
		while(!stop) {
			final String line = reader.readLine();
			
			if(line == null) {
				stop = true;
			} else if(line.isEmpty()) {
				continue;
			} else {
				Event e = parseFromLine(threadName,line);
				
				listener.event(e);
			}
		}
		
		listener.afterThread(threadName);
	}
	
	public static void trace(
			final File traceTarGz, 
			final String matchPrefix, 
			final TraceReplayEngine listener
			) throws IOException {
		listener.beforeReplay();
		
		System.out.printf("traversing tracefile: %s\n", traceTarGz.getAbsolutePath());
		try(final FileInputStream fis = new FileInputStream(traceTarGz)){
			try(final GZIPInputStream gfis = new GZIPInputStream(fis)){
				try(final TarArchiveInputStream tgfis = new TarArchiveInputStream(gfis)){
					boolean stop = false;
					
					while(!stop) {
						ArchiveEntry a = tgfis.getNextEntry();
						
						if(a == null) {
							stop = true;
						} else {
							if(a.getSize() <= 0) {
								continue;
							}
							
							if(!isTrace(a)) {
								continue;
							}
							
							if(matchPrefix != null && !startsWithPrefix(a,matchPrefix)) {
								continue;
							}
							
							System.out.printf("\ttraversing thread dump \t%s\n",a.getName());
							
							final String threadName = getThreadName(a.getName());
							
							trace(
									threadName,
									new BufferedReader(new InputStreamReader(tgfis)),
									listener
									);
						}
					}
				}
			}
		}
		
		listener.afterReplay();
	}
	
	private static Map<String,byte[]> getRelevantTraceData(
			final File traceTarGz,
			final String matchPrefix
			) throws IOException {
		final Map<String,byte[]> relevantTraceData = new HashMap<>();
		
		final long start = System.currentTimeMillis();
		long numBytes = 0L;
		try(final FileInputStream fis = new FileInputStream(traceTarGz)){
			try(final GZIPInputStream gfis = new GZIPInputStream(fis)){
				try(final TarArchiveInputStream tgfis = new TarArchiveInputStream(gfis)){
					boolean stop = false;
					
					while(!stop) {
						ArchiveEntry a = tgfis.getNextEntry();
						if(a == null) {
							stop = true;
						} else {
							if(!isTrace(a)) {
								continue;
							}
							
							if(matchPrefix != null && !startsWithPrefix(a,matchPrefix)) {
								continue;
							}
							
							final String threadName = getThreadName(a.getName());
							
							ByteArrayOutputStream entry = new ByteArrayOutputStream();
							
							IOUtils.copy(tgfis, entry);
							
							if(entry.size() > 0) {
								final byte[] data = entry.toByteArray();
								
								relevantTraceData.put(threadName, data);
								numBytes += data.length;
								
								System.out.printf(
										"\t%s contains %dB for thread %s\n", 
										a.getName(), 
										data.length, 
										threadName
										);
							}
						}
					}
				}
			}
		}
		
		System.out.printf(
				"extracted trace data for prefix \"%s\" in %dms (%dB extracted)\n",
				matchPrefix,
				System.currentTimeMillis() - start,
				numBytes
				);
		
		return relevantTraceData;
	}
}