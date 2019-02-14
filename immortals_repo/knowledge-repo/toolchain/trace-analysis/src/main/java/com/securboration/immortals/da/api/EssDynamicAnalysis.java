package com.securboration.immortals.da.api;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;

import com.securboration.immortals.da.BlockLookup;
import com.securboration.immortals.da.Event;
import com.securboration.immortals.da.ReplayEngine;
import com.securboration.immortals.da.TraversalSearchListener;

public class EssDynamicAnalysis {
    
    private final File baseDir;
    
    public EssDynamicAnalysis(final String essDirPath){
        baseDir = new File(essDirPath);
    }
    
    private static byte[] tgz(final File dir) throws IOException{
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try(final GzipCompressorOutputStream gz = new GzipCompressorOutputStream(bos)){
            try(final TarArchiveOutputStream t = new TarArchiveOutputStream(gz)){
                
                final String basePath = dir.getCanonicalPath();
                
                for(File f:FileUtils.listFiles(dir, null, true)){
                    final String path = f.getCanonicalPath();
                    final String name = path.substring(path.indexOf(basePath) + basePath.length() + 1);
                    
                    System.out.println(name);
                    
                    TarArchiveEntry entry = new TarArchiveEntry(f, name);
                    t.putArchiveEntry(entry);
                    t.write(FileUtils.readFileToByteArray(f));
                    t.closeArchiveEntry();
                }
            }
        }
        
        return bos.toByteArray();
    }
    
    public File acquireDynamicAnalysisArtifact() throws IOException{
        //TODO: tons of hard coding going on here
        
        final File analysisOutputDir = new File(baseDir,"analysisResults");
        
        final File rampartOutput = new File(baseDir,"rampartData");
        final File traceInput = new File(analysisOutputDir,"analysis.tar.gz");
        {//tgz the trace
            final byte[] traceTgz = tgz(rampartOutput);
            FileUtils.writeByteArrayToFile(traceInput, traceTgz);
        }
        
        final String[] interestingApiPrefixes = {
                "java/nio",
                "java/net",
                "java/io",
        };
        
        //walk through the client dynamic analysis data
        analyze(
            traceInput,
            "messages",
            new File(rampartOutput,"dictionaries/client.dict"),
            "com/securboration/client/ClientRunner clientAction ()V",
            "java/net/HttpURLConnection getOutputStream",
            new File(analysisOutputDir,"client"),
            interestingApiPrefixes
            );
        
//        analyze(
//            traceInput,
//            "rampartData/client",
//            new File("data/client.dict"),
//            "com/securboration/client/ClientRunner clientAction ()V",
//            "java/net/HttpURLConnection getOutputStream",
//            new File(analysisOutputDir,"client"),
//            interestingApiPrefixes
//            );
        
        final File f = new File(analysisOutputDir,"client/stacks-at-entry.hist");
        
        if(!f.exists()){
            throw new RuntimeException("expected a file to exist but it doesn't: " + f.getAbsolutePath());
        }
        
        return f;
    }
    
    
    
    
    
    
    public static void main(String[] args) throws Exception {
        EssDynamicAnalysis test = new EssDynamicAnalysis("..\\..\\cp\\cp3.1\\cp-eval-service\\eval-out\\ess\\ess");
        
        System.out.println("\n\n\n\n\n");
        System.out.println(FileUtils.readFileToString(test.acquireDynamicAnalysisArtifact()));
    }
    
    private static void analyze(
            final File traceInput,
            final String tracePrefix,
            final File dictionary,
            final String entrypointPrefix,
            final String exitpointPrefix,
            final File outputDir,
            final String[] interestingApiPrefixes
            ) throws IOException{
        final BlockLookup blockLookup = getBlockLookupFromDictionaryFile(dictionary);
        
        //create a histogram of calls to interesting API prefixes
        generateCallHistogram(
            traceInput,
            tracePrefix,
            blockLookup,
            outputDir,
            interestingApiPrefixes
            );
        
        //create a histogram of the stack traces to interesting API calls
        getStacksAtApiInvocation(
            traceInput,
            tracePrefix,
            blockLookup,
            outputDir,
            interestingApiPrefixes
            );
        
        //search for traversals from the entrypoint to interesting exitpoints e.g., socketChannel.write
        extractTraversals(
            traceInput,
            tracePrefix,
            blockLookup,
            entrypointPrefix,
            exitpointPrefix,
            outputDir
            );
    }
    
    private static String dumpEventsToString(
            List<Event> events,
            Long limit
            ) {
        return dumpEventsToString(events,true,limit);
    }
    
    private static String dumpEventsToString(
            List<Event> events,
            boolean includeEventIds,
            Long limit
            ) {
        StringBuilder sb = new StringBuilder();
        
        long count = 0;
        boolean ellipsesPrinted = false;
        for(Event e:events) {
            if(limit != null && (count > limit/2) && (count < (events.size()-limit/2))) {
                //do nothing
                if(ellipsesPrinted) {
                    //really do nothing
                } else {
                    ellipsesPrinted = true;
                    sb.append("\t...\n");
                }
            } else {
                sb.append(
                        String.format(
                                "\t%-10d %-15s %s\n", 
                                includeEventIds ? e.getEventIndex() : count, 
                                e.getEventType(), 
                                e.getEventDesc()
                                )
                        );
            }
            
            count++;
        }
        
        return sb.toString();
    }
    
    private static String dumpEventListsToString(
            String desc,
            List<List<Event>> lists,
            Long limit
            ) {
        StringBuilder sb = new StringBuilder();
        
        System.out.printf("found %d stacks for %s\n", lists.size(), desc);
        for(int i=0;i<lists.size();i++) {
            System.out.println(dumpEventsToString(lists.get(i),limit));
        }
        
        return sb.toString();
    }
    
    private static void dumpStringToFile(
            File baseDir, 
            String tag, 
            String content
            ) throws IOException {
        FileUtils.writeStringToFile(new File(baseDir,tag), content,"UTF-8");
    }
    
    private static void extractTraversals(
            final File traceInput, 
            final String traceInputPrefix, 
            final BlockLookup blockLookup, 
            
            final String entrypointPrefix,
            final String exitpointPrefix,
            
            final File outputDir
            ) throws IOException {
        {//search for traversals from the entrypoint to some exitpoint e.g., socketChannel.write
            
            final AtomicLong count = new AtomicLong(0L);
            ReplayEngine.findTraversals(
                    traceInput, 
                    traceInputPrefix, 
                    blockLookup, 
                    entrypointPrefix, 
                    exitpointPrefix,
                    new TraversalSearchListener() {
                        
                        @Override
                        public boolean foundEndToEndTraversal(
                                String matchedEntrypointPrefix,
                                String matchedExitpointPrefix, 
                                List<Event> stackAfterExitpoint, 
                                List<Event> trace
                                ) {
                            
                            System.out.printf(
                                    "found trace from entrypoint \"%s\" to termination \"%s\"\n",
                                    trace.size(),
                                    entrypointPrefix,
                                    exitpointPrefix
                                    );
                            System.out.printf("\tstack\n");
                            System.out.println(dumpEventsToString(stackAfterExitpoint,null));
                            System.out.printf("\thist\n");
                            System.out.println(
                                    dumpEventsToString(
                                            trace,
                                            5L
                                            )
                                    );
                            System.out.println();
                            
                            try {
                                final String name = "entry-to-socketchannel-write-" + count.getAndIncrement();
                                
                                FileUtils.writeStringToFile(
                                        new File(outputDir,name + ".trav"), 
                                        dumpEventsToString(
                                                trace,
                                                null
                                                ),
                                        "UTF-8"
                                        );
                                
                                FileUtils.writeStringToFile(
                                        new File(outputDir,name + ".stack"), 
                                        dumpEventsToString(
                                                stackAfterExitpoint,
                                                null
                                                ),
                                        "UTF-8"
                                        );
                            } catch(IOException e) {
                                throw new RuntimeException(e);
                            }
                            
                            return true;
                        }
                        
                    });
        }
    }
    
    private static void generateStackHistogram(
            final File traceInput,
            final String traceInputPrefix,
            final BlockLookup blockLookup,
            final File outputDir,
            final String...interestingApiPrefixes
            ) throws IOException {
        final Map<String,List<List<Event>>> stacks = 
                ReplayEngine.getEntrypointCallstacks(traceInput, traceInputPrefix, blockLookup, interestingApiPrefixes);
        
        final Map<String,Map<String,Integer>> histogram = new TreeMap<>();
        
        for(final String methodDesc:stacks.keySet()) {
            Map<String,Integer> counts = new LinkedHashMap<>();
            
            for(List<Event> stack:stacks.get(methodDesc)) {
                String key = dumpEventsToString(stack,null);
                
                Integer count = counts.get(key);
                if(count == null) {
                    count = 0;
                }
                
                counts.put(key, count+1);
            }
            
            histogram.put(methodDesc, counts);
        }//TODO
        
        StringBuilder sb = new StringBuilder();
        for(String apiCall:histogram.keySet()){
            Map<String,Integer> counts = histogram.get(apiCall);
            
            sb.append(
                String.format(
                    "%d invocations of %s\n",
                    counts.size(),
                    apiCall
                    )
                );
            
            for(String stack:counts.keySet()){
                sb.append(String.format("\t%d occurrences of %s\n", counts.get(stack), stack));
            }
            
            sb.append("\n");
        }
        
        
        dumpStringToFile(
                outputDir,
                "stacks-at-entry.hist",
                sb.toString()
                );
    }
    
    private static void getStacksAtApiInvocation(
            final File traceInput,
            final String traceInputPrefix,
            final BlockLookup blockLookup,
            final File outputDir,
            final String...interestingApiPrefixes
            ) throws IOException {
        final Map<String,List<List<Event>>> stacks = 
                ReplayEngine.getEntrypointCallstacks(traceInput, traceInputPrefix, blockLookup, interestingApiPrefixes);
        
        final Map<String,Map<String,Integer>> histogram = new TreeMap<>();
        
        for(final String methodDesc:stacks.keySet()) {
            Map<String,Integer> counts = new LinkedHashMap<>();
            
            for(List<Event> stack:stacks.get(methodDesc)) {
                String key = dumpEventsToString(stack,false,null);
                
                Integer count = counts.get(key);
                if(count == null) {
                    count = 0;
                }
                
                counts.put(key, count+1);
            }
            
            histogram.put(methodDesc, counts);
        }//TODO
        
        StringBuilder sb = new StringBuilder();
        for(String apiCall:histogram.keySet()){
            Map<String,Integer> counts = histogram.get(apiCall);
            
            sb.append(
                String.format(
                    "%d invocations of %s\n",
                    counts.size(),
                    apiCall
                    )
                );
            
            for(String stack:counts.keySet()){
                sb.append(String.format("\t%d occurrences of \n%s\n", counts.get(stack), stack));
            }
            
            sb.append("\n");
        }
        
        
        dumpStringToFile(
                outputDir,
                "stacks-at-entry.hist",
                sb.toString()
                );
    }
    
    private static void generateCallHistogram(
            final File traceInput,
            final String traceInputPrefix,
            final BlockLookup blockLookup,
            final File outputDir,
            final String...interestingApiPrefixes
            ) throws IOException {
        //search for the presence of interesting API calls
        final Map<String,Integer> histogram = ReplayEngine.buildMatchingCallHistogram(
                traceInput, 
                traceInputPrefix,
                blockLookup, 
                interestingApiPrefixes
                );
        
        StringBuilder sb = new StringBuilder();
        for(String key:histogram.keySet()) {
            sb.append(String.format("\t%-10d %s\n", histogram.get(key),key));
        }
        
        dumpStringToFile(
                outputDir,
                "calls.hist",
                sb.toString()
                );
    }
    
    private static BlockLookup getBlockLookupFromDictionaryFile(File dict) throws IOException {
        //com/securboration/client/ClientRunner <init> ()V 0 ---> com/securboration/rampart/inst/rt/DynamicAnalysisRuntime print (Ljava/lang/String;)V, java/lang/Object <init> ()V
        final String separator = "--->";
        
        Map<String,String[]> blocksToCalls = new HashMap<>();
        try(FileInputStream fis = new FileInputStream(dict)){
            try(GZIPInputStream gis = new GZIPInputStream(fis)){
                try(InputStreamReader isr = new InputStreamReader(gis)){
                    try(BufferedReader reader = new BufferedReader(isr)){
                        
                        boolean stop = false;
                        while(!stop) {
                            final String line = reader.readLine();
                            
                            if(line == null) {
                                stop = true;
                            } else if(line.isEmpty()) {
                                continue;
                            } else {
                                final int index = line.indexOf(separator);
                                final String desc = line.substring(0,index).trim();
                                final String[] calls = line.substring(index + separator.length()).trim().split(",");
                                for(int i=0;i<calls.length;i++) {
                                    calls[i] = calls[i].trim();
                                }
                                blocksToCalls.put(desc, calls);
                            }
                        }
                    }
                }
            }
        }
        
        return new BlockLookup() {

            @Override
            public String[] getOutgoingMethodCalls(String blockDesc) {
                return blocksToCalls.get(blockDesc);
            }
            
        };
    }

}
