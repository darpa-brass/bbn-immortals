//package com.securboration.rampart.analysis.dynamic;
//
//import java.io.BufferedOutputStream;
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.GZIPOutputStream;
//
//import org.apache.commons.compress.archivers.ArchiveEntry;
//import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//
//public class DynamicAnalysisReplayEngineBackup {
//	
//	private Map<String,ByteArrayOutputStream> map = new LinkedHashMap<>();
//	
//	public static void main(String[] args) throws FileNotFoundException, IOException {
//		final File traceInput = new File("C:/Users/Securboration/Desktop/vm_shared/analysis.tar.gz");
//		final File dictionaryInput = new File("C:/Users/Securboration/Desktop/code/rampart/rampart/trunk/instrument/scripts/rampartData/dictionaries/client.dict");
//		final File replayOutput = new File("./da.replay");
//		
//		final File usefulTraceDir = new File("../data/dynamic-analysis/server");
//		
//		final BlockLookup blockLookup = getBlockLookupFromDictionaryFile(dictionaryInput);
//		
//////		if(false)
////		{
////			String[] calls = blockLookup.getOutgoingMethodCalls("org/springframework/aop/framework/CglibAopProxy$ProxyCallbackFilter equalsPointcuts (Lorg/springframework/aop/Advisor;Lorg/springframework/aop/Advisor;)Z 0");
////			for(String c:calls) {
////				System.out.println("\t" + c);
////			}
////			System.exit(-1);//TODO
////			
//////			com/securboration/rampart/inst/rt/DynamicAnalysisRuntime print (Ljava/lang/String;)V, 
//////			org/springframework/aop/PointcutAdvisor getPointcut ()Lorg/springframework/aop/Pointcut;, 
//////			org/springframework/util/ObjectUtils nullSafeEquals (Ljava/lang/Object;Ljava/lang/Object;)Z
////		}
//		
////		if(false) 
//		{
//			createReplayFile(
//					traceInput,
//					replayOutput
//					);
//		}
//		
////		if(false) //TEST
//		{
//			replay(replayOutput,new TraceReplayEngine() {
//				final Map<String,List<String>> stacks = new HashMap<>();
//				final Map<String,List<Event>> eventHistories = new HashMap<>();
//				
//				private List<String> stack(String thread){
//					if(stacks.containsKey(thread)) {
//						return stacks.get(thread);
//					}
//					
//					List<String> stack = new ArrayList<>();
//					stacks.put(thread, stack);
//					return stack;
//				}
//				
//				private void stackBottomedOut(final String thread) {
//					//TODO: this is only a problem if we care about perfect consistency
//				}
//				
//				private List<Event> history(final String thread){
//					List<Event> history = eventHistories.get(thread);
//					if(history == null) {
//						history = new ArrayList<>();
//						eventHistories.put(thread, history);
//					}
//					return history;
//				}
//				
//				private void addEventToHistory(final String thread, final Event e) {
//					history(thread).add(e);
//				}
//				
//				private void methodCalled(final String thread, final String methodDesc) {
//					List<String> stack = stack(thread);
//					stack.add(methodDesc);
//				}
//				
//				private void methodExited(final String thread, final String methodDesc) {
//					List<String> stack = stack(thread);
//					
//					if(stack.size() == 0) {
//						stackBottomedOut(thread);
//						return;
//					}
//					
//					final String removed = stack.remove(stack.size()-1);
//					if(removed.equals(methodDesc)) {
//						//all is well
//					} else {
//						throw new RuntimeException("popped \"" + removed +"\" off the stack but expected \"" + methodDesc + "\"");
//					}
//					
//					if(stack.size() == 0) {
////						System.out.printf("found a complete trace of length %d in %s\n",history(thread).size(),thread);
//						history(thread).clear();//TODO
//					}
//				}
//
//				@Override
//				public boolean event(Event e) {
////					if(e.eventType.ordinal() == EventType.THREAD_RESUME.ordinal() || e.eventType.ordinal() == EventType.THREAD_PAUSE.ordinal()) {
////						System.out.printf("%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////					}
//					
////					System.out.printf("%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////					System.out.printf("\t%s\n", stack(e.thread));//TODO
////					System.out.printf("\t%s\n", stack(e.thread).size());//TODO
//					
//					{//stack tracking
//						addEventToHistory(e.thread,e);
//						
//						if(e.eventType.ordinal() == EventType.METHOD_BEGIN.ordinal()) {
//							methodCalled(e.thread,e.eventDesc);
//						} else if(e.eventType.ordinal() == EventType.RETURN.ordinal()) {
//							methodExited(e.thread,e.eventDesc);
//						} else if(e.eventType.ordinal() == EventType.UNCAUGHT.ordinal()) {
//							methodExited(e.thread,e.eventDesc);
//						}
//					}
//					
//					{
//						List<Event> hist = history(e.thread);
//						List<String> stack = stack(e.thread);
//						
//						if(e.eventType.ordinal() == EventType.RETURN.ordinal() && e.eventDesc.startsWith("com/securboration/server/ServerEndpoint ")) {
//							System.out.printf("\tstack=%d, hist=%d, desc=%s\n", stack.size(), hist.size(), e.eventDesc);
//							
//							if(stack.size() > 1) {
//								try {
//									String[] parts = e.eventDesc.split(" ");
//									String name = parts[1] + "-" + UUID.randomUUID().toString() + ".trace";
//									writeReplayToFile(hist,new File(name));
//								} catch(IOException ioe) {
//									throw new RuntimeException(ioe);
//								}
//							}
//							
////							System.out.printf("\t%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////							System.out.printf("\t%s\n", stack(e.thread));//TODO
////							System.out.printf("\t%s\n", stack(e.thread).size());//TODO
////							System.out.println();
//						}
//					}
//					
//					//TODO: analysis goes here
//					
//					return true;
//				}
//			});
//		}
//		
//		if(false) //detect interesting platform calls
//		{
//			replay(replayOutput,new TraceReplayEngine() {
//				
//				long startTime;
//				
//				final Map<String,List<String>> stacks = new HashMap<>();
//				final Map<String,List<Event>> eventHistories = new HashMap<>();
//
//				@Override
//				public void beforeReplay() {
//					startTime = System.currentTimeMillis();
//				}
//				
//				private List<String> stack(String thread){
//					if(stacks.containsKey(thread)) {
//						return stacks.get(thread);
//					}
//					
//					List<String> stack = new ArrayList<>();
//					stacks.put(thread, stack);
//					return stack;
//				}
//				
//				private void stackBottomedOut(final String thread) {
//					//TODO: this is only a problem if we care about perfect consistency
//				}
//				
//				private List<Event> history(final String thread){
//					List<Event> history = eventHistories.get(thread);
//					if(history == null) {
//						history = new ArrayList<>();
//						eventHistories.put(thread, history);
//					}
//					return history;
//				}
//				
//				private void addEventToHistory(final String thread, final Event e) {
//					history(thread).add(e);
//				}
//				
//				private void methodCalled(final String thread, final String methodDesc) {
//					List<String> stack = stack(thread);
//					stack.add(methodDesc);
//				}
//				
//				private void methodExited(final String thread, final String methodDesc) {
//					List<String> stack = stack(thread);
//					
//					if(stack.size() == 0) {
//						stackBottomedOut(thread);
//						return;
//					}
//					
//					final String removed = stack.remove(stack.size()-1);
//					if(removed.equals(methodDesc)) {
//						//all is well
//					} else {
//						throw new RuntimeException("popped \"" + removed +"\" off the stack but expected \"" + methodDesc + "\"");
//					}
//					
//					if(stack.size() == 0) {
////						System.out.printf("found a complete trace of length %d in %s\n",history(thread).size(),thread);
//						history(thread).clear();//TODO
//					}
//				}
//
//				@Override
//				public boolean event(Event e) {
////					if(e.eventType.ordinal() == EventType.THREAD_RESUME.ordinal() || e.eventType.ordinal() == EventType.THREAD_PAUSE.ordinal()) {
////						System.out.printf("%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////					}
//					
////					System.out.printf("%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////					System.out.printf("\t%s\n", stack(e.thread));//TODO
////					System.out.printf("\t%s\n", stack(e.thread).size());//TODO
//					
//					{//stack tracking
//						addEventToHistory(e.thread,e);
//						
//						if(e.eventType.ordinal() == EventType.METHOD_BEGIN.ordinal()) {
//							methodCalled(e.thread,e.eventDesc);
//						} else if(e.eventType.ordinal() == EventType.RETURN.ordinal()) {
//							methodExited(e.thread,e.eventDesc);
//						} else if(e.eventType.ordinal() == EventType.UNCAUGHT.ordinal()) {
//							methodExited(e.thread,e.eventDesc);
//						}
//					}
//					
//					{
//						List<Event> hist = history(e.thread);
//						List<String> stack = stack(e.thread);
//						
//						if(e.eventType.ordinal() == EventType.RETURN.ordinal() && e.eventDesc.startsWith("com/securboration/server/ServerEndpoint ")) {
//							System.out.printf("\tstack=%d, hist=%d, desc=%s\n", stack.size(), hist.size(), e.eventDesc);
//							
//							if(stack.size() > 1) {
//								try {
//									String[] parts = e.eventDesc.split(" ");
//									String name = parts[1] + "-" + UUID.randomUUID().toString() + ".trace";
//									writeReplayToFile(hist,new File(name));
//								} catch(IOException ioe) {
//									throw new RuntimeException(ioe);
//								}
//							}
//							
////							System.out.printf("\t%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////							System.out.printf("\t%s\n", stack(e.thread));//TODO
////							System.out.printf("\t%s\n", stack(e.thread).size());//TODO
////							System.out.println();
//						}
//					}
//					
//					//TODO: analysis goes here
//					
//					return true;
//				}
//
//				@Override
//				public void afterReplay() {
//					System.out.printf("replay took %dms\n", System.currentTimeMillis() - startTime);
//				}
//				
//			});
//		}
//		
//		if(false) //extract meaningful sequences of events from the trace
//		{
//			replay(replayOutput,new TraceReplayEngine() {
//				
//				long startTime;
//				
//				final Map<String,List<String>> stacks = new HashMap<>();
//				final Map<String,List<Event>> eventHistories = new HashMap<>();
//
//				@Override
//				public void beforeReplay() {
//					startTime = System.currentTimeMillis();
//				}
//				
//				private List<String> stack(String thread){
//					if(stacks.containsKey(thread)) {
//						return stacks.get(thread);
//					}
//					
//					List<String> stack = new ArrayList<>();
//					stacks.put(thread, stack);
//					return stack;
//				}
//				
//				private void stackBottomedOut(final String thread) {
//					//TODO: this is only a problem if we care about perfect consistency
//				}
//				
//				private List<Event> history(final String thread){
//					List<Event> history = eventHistories.get(thread);
//					if(history == null) {
//						history = new ArrayList<>();
//						eventHistories.put(thread, history);
//					}
//					return history;
//				}
//				
//				private void addEventToHistory(final String thread, final Event e) {
//					history(thread).add(e);
//				}
//				
//				private void methodCalled(final String thread, final String methodDesc) {
//					List<String> stack = stack(thread);
//					stack.add(methodDesc);
//				}
//				
//				private void methodExited(final String thread, final String methodDesc) {
//					List<String> stack = stack(thread);
//					
//					if(stack.size() == 0) {
//						stackBottomedOut(thread);
//						return;
//					}
//					
//					final String removed = stack.remove(stack.size()-1);
//					if(removed.equals(methodDesc)) {
//						//all is well
//					} else {
//						throw new RuntimeException("popped \"" + removed +"\" off the stack but expected \"" + methodDesc + "\"");
//					}
//					
//					if(stack.size() == 0) {
////						System.out.printf("found a complete trace of length %d in %s\n",history(thread).size(),thread);
//						history(thread).clear();//TODO
//					}
//				}
//
//				@Override
//				public boolean event(Event e) {
////					if(e.eventType.ordinal() == EventType.THREAD_RESUME.ordinal() || e.eventType.ordinal() == EventType.THREAD_PAUSE.ordinal()) {
////						System.out.printf("%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////					}
//					
////					System.out.printf("%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////					System.out.printf("\t%s\n", stack(e.thread));//TODO
////					System.out.printf("\t%s\n", stack(e.thread).size());//TODO
//					
//					{//stack tracking
//						addEventToHistory(e.thread,e);
//						
//						if(e.eventType.ordinal() == EventType.METHOD_BEGIN.ordinal()) {
//							methodCalled(e.thread,e.eventDesc);
//						} else if(e.eventType.ordinal() == EventType.RETURN.ordinal()) {
//							methodExited(e.thread,e.eventDesc);
//						} else if(e.eventType.ordinal() == EventType.UNCAUGHT.ordinal()) {
//							methodExited(e.thread,e.eventDesc);
//						}
//					}
//					
//					{
//						List<Event> hist = history(e.thread);
//						List<String> stack = stack(e.thread);
//						
//						if(e.eventType.ordinal() == EventType.RETURN.ordinal() && e.eventDesc.startsWith("com/securboration/server/ServerEndpoint ")) {
//							System.out.printf("\tstack=%d, hist=%d, desc=%s\n", stack.size(), hist.size(), e.eventDesc);
//							
//							if(stack.size() > 1) {
//								try {
//									String[] parts = e.eventDesc.split(" ");
//									String name = parts[1] + "-" + UUID.randomUUID().toString() + ".trace";
//									writeReplayToFile(hist,new File(name));
//								} catch(IOException ioe) {
//									throw new RuntimeException(ioe);
//								}
//							}
//							
////							System.out.printf("\t%-10d %-15s %s\n", e.eventIndex, e.eventType, e.eventDesc);
////							System.out.printf("\t%s\n", stack(e.thread));//TODO
////							System.out.printf("\t%s\n", stack(e.thread).size());//TODO
////							System.out.println();
//						}
//					}
//					
//					//TODO: analysis goes here
//					
//					return true;
//				}
//
//				@Override
//				public void afterReplay() {
//					System.out.printf("replay took %dms\n", System.currentTimeMillis() - startTime);
//				}
//				
//			});
//		}
//		
//		
//		if(false)//TODO
//		for(File f:FileUtils.listFiles(usefulTraceDir, new String[] {"trace"}, true)){
//			System.out.println(f.getAbsolutePath());
//			
//			replay(f,new TraceReplayEngine() {
//				
//				long startTime;
//				
//				final Map<String,List<String>> stacks = new HashMap<>();
//
//				@Override
//				public void beforeReplay() {
//					startTime = System.currentTimeMillis();
//				}
//				
//				private List<String> stack(String thread){
//					if(stacks.containsKey(thread)) {
//						return stacks.get(thread);
//					}
//					
//					List<String> stack = new ArrayList<>();
//					stacks.put(thread, stack);
//					return stack;
//				}
//				
//				private void methodCalled(final String thread, final String methodDesc) {
//					List<String> stack = stack(thread);
//					stack.add(methodDesc);
//				}
//				
//				private void methodExited(final String thread, final String methodDesc) {
//					List<String> stack = stack(thread);
//					
//					if(stack.size() == 0) {
//						throw new RuntimeException("stack bottomed out in " + methodDesc);
//					}
//					
//					final String removed = stack.remove(stack.size()-1);
//					if(removed.equals(methodDesc)) {
//						//all is well
//					} else {
//						throw new RuntimeException("popped \"" + removed +"\" off the stack but expected \"" + methodDesc + "\"");
//					}
//				}
//
//				@Override
//				public boolean event(Event e) {
//					System.out.printf("\t%-4d %-15s %s\n", stack(e.thread).size(), e.eventType, e.eventDesc);
////					System.out.printf("\t%s\n", stack(e.thread).size());//TODO
//					
//					{//stack tracking
//						if(e.eventType.ordinal() == EventType.METHOD_BEGIN.ordinal()) {
//							methodCalled(e.thread,e.eventDesc);
//						} else if(e.eventType.ordinal() == EventType.RETURN.ordinal()) {
//							methodExited(e.thread,e.eventDesc);
//						} else if(e.eventType.ordinal() == EventType.UNCAUGHT.ordinal()) {
//							methodExited(e.thread,e.eventDesc);
//						}
//					}
//					
//					return true;
//				}
//
//				@Override
//				public void afterReplay() {
//					System.out.printf("replay took %dms\n", System.currentTimeMillis() - startTime);
//				}
//				
//			});
//			
//			throw new RuntimeException("intentional");
//		}
//		
//	}
//	
//	private static boolean isUntrackableCall(String calledMethodDesc) {
//		//TODO
//		if(calledMethodDesc.startsWith("java/lang")) {
//			return true;
//		}
//		
//		return false;
//	}
//	
//	
//	
//	
//	
//	private static void writeReplayToFile(
//			Iterable<Event> replay,
//			File replayOutput
//			) throws IOException {
//		try(FileOutputStream fos = new FileOutputStream(replayOutput)){
//			try(BufferedOutputStream bos = new BufferedOutputStream(fos)){
//				try(GZIPOutputStream gos = new GZIPOutputStream(bos)){
//					try(PrintStream pos = new PrintStream(gos)){
//						for(Event e:replay) {
//							pos.printf("%s,%s\n",e.eventType,e.eventDesc);
//						}
//					}
//				}
//			}
//		}
//	}
//	
//	public static void createReplayFile(
//			File traceInput,
//			File replayOutput
//			) throws IOException {
//		System.out.println("reading trace " + traceInput.getAbsolutePath());
//		
//		final DynamicAnalysisReplayEngineBackup r = new DynamicAnalysisReplayEngineBackup(
//				traceInput.getAbsolutePath(),
//				null
//				);
//		
//		final Iterable<Event> replay = r.getReplayIterator("server");
//		
//		//TODO: wrap this in a method createReplayableTrace(...)
//		String lastThread = null;
//		try(FileOutputStream fos = new FileOutputStream(replayOutput)){
//			try(BufferedOutputStream bos = new BufferedOutputStream(fos)){
//				try(GZIPOutputStream gos = new GZIPOutputStream(bos)){
//					try(PrintStream pos = new PrintStream(gos)){
//						for(Event e:replay) {
//							{
//								String currentThread = e.thread;
//								if(lastThread == null || currentThread != lastThread) {//intentionally not .equals
//									if(lastThread != null) {
//										pos.printf("%s,%s\n",EventType.THREAD_PAUSE,lastThread);
//									}
//									
//									pos.printf("%s,%s\n",EventType.THREAD_RESUME,currentThread);
//								}
//								
//								lastThread = currentThread;
//							}
//							
//							pos.printf("%s,%s\n",e.eventType,e.eventDesc);
//						}
//					}
//				}
//			}
//		}
//	}
//	
//	public static interface TraceReplayEngine {
//		public default void beforeReplay() {}
//		
//		public default boolean event(Event e) {
//			return true;
//		}
//		
//		public default void afterReplay() {}
//	}
//	
//	public static interface BlockLookup {
//		public default String[] getOutgoingMethodCalls(String blockDesc) {
//			return null;
//		}
//	}
//	
//	private static BlockLookup getBlockLookupFromDictionaryFile(File dict) throws IOException {
//		//com/securboration/client/ClientRunner <init> ()V 0 ---> com/securboration/rampart/inst/rt/DynamicAnalysisRuntime print (Ljava/lang/String;)V, java/lang/Object <init> ()V
//		final String separator = "--->";
//		
//		Map<String,String[]> blocksToCalls = new HashMap<>();
//		try(FileInputStream fis = new FileInputStream(dict)){
//			try(GZIPInputStream gis = new GZIPInputStream(fis)){
//				try(InputStreamReader isr = new InputStreamReader(gis)){
//					try(BufferedReader reader = new BufferedReader(isr)){
//						
//						boolean stop = false;
//						while(!stop) {
//							final String line = reader.readLine();
//							
//							if(line == null) {
//								stop = true;
//							} else if(line.isEmpty()) {
//								continue;
//							} else {
//								final int index = line.indexOf(separator);
//								final String desc = line.substring(0,index).trim();
//								final String[] calls = line.substring(index + separator.length()).trim().split(",");
//								for(int i=0;i<calls.length;i++) {
//									calls[i] = calls[i].trim();
//								}
//								blocksToCalls.put(desc, calls);
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		return new BlockLookup() {
//
//			@Override
//			public String[] getOutgoingMethodCalls(String blockDesc) {
//				return blocksToCalls.get(blockDesc);
//			}
//			
//		};
//	}
//	
//	public static void replay(
//			File replayFile, 
//			TraceReplayEngine replayEngine
//			) throws FileNotFoundException, IOException{
//		replayEngine.beforeReplay();
//		
//		long index = 0L;
//		try(FileInputStream fis = new FileInputStream(replayFile)){
//			try(GZIPInputStream gis = new GZIPInputStream(fis)){
//				try(InputStreamReader isr = new InputStreamReader(gis)){
//					try(BufferedReader reader = new BufferedReader(isr)){
//						
//						String currentThread = null;
//						boolean stop = false;
//						while(!stop) {
//							final String line = reader.readLine();
//							
//							if(line == null) {
//								stop = true;
//							} else if(line.isEmpty()) {
//								continue;
//							} else {
//								//parse the line
//								final Event event = Event.parseFromLine(index, currentThread, line.trim());
//								
//								if(event.eventType.ordinal() == EventType.THREAD_RESUME.ordinal()) {
//									currentThread = event.eventDesc;
//								}
//								
//								boolean shouldContinue = replayEngine.event(event);
//								if(!shouldContinue) {
//									stop = true;
//								}
//							}
//							
//							index++;
//						}
//					}
//				}
//			}
//		}
//		
//		replayEngine.afterReplay();
//	}
//	
//	private Map<String,ByteArrayOutputStream> collect(String prefix){
//		Map<String,ByteArrayOutputStream> matches = new LinkedHashMap<>();
//		
//		for(String key:map.keySet()) {
//			if(key.startsWith(prefix)) {
//				matches.put(key,map.get(key));
//			}
//		}
//		
//		return matches;
//	}
//	
//	//81c800:  return@ com/sun/xml/bind/v2/util/QNameMap getEntry (Ljava/lang/String;Ljava/lang/String;)Lcom/sun/xml/bind/v2/util/QNameMap$Entry;
//	
//	private enum EventType{
//		METHOD_BEGIN,
//		METHOD_BEGIN_EXTERNAL,
//		RETURN,
//		RETURN_EXTERNAL,
//		UNCAUGHT,
//		CFG_BLOCK,
//		THREAD_PAUSE,
//		THREAD_RESUME
//		;
//	}
//	
//	private static class Trace implements Iterator<Event>, Iterable<Event>{
//		private final Set<Event[]> viableLists = new LinkedHashSet<>();
//		private final Map<Event[],Integer> indices = new HashMap<>();
//		private final Map<Event[],String> listNameLookup = new HashMap<>();
//
//		private Trace(Map<String,List<Event>> events) {
//			for(String eventListName:events.keySet()) {
//				List<Event> eventList = events.get(eventListName);
//				
//				if(eventList.size() == 0) {
//					continue;
//				}
//				
//				Event[] array = eventList.toArray(new Event[] {});
//				
//				indices.put(array, 0);
//				viableLists.add(array);
//				listNameLookup.put(array, eventListName);
//			}
//		}
//
//		@Override
//		public boolean hasNext() {
//			return viableLists.size() > 0;
//		}
//		
//		private Event getCurrentEvent(Event[] list) {
//			int index = indices.get(list);
//			return list[index];
//		}
//		
//		private void incrementIndex(Event[] list) {
//			Integer count = indices.get(list);
//			count = count + 1;
//			
//			indices.put(list, count);
//			
//			if(count >= list.length) {
//				viableLists.remove(list);
//				indices.put(list, null);
//			}
//		}
//
//		@Override
//		public Event next() {
//			Event[] oldestList = null;
//			Event oldest = null;
//			long oldestIndex = Long.MAX_VALUE;
//			
//			for(Event[] eventList:viableLists) {
//				final Event event = getCurrentEvent(eventList);
//				
//				if(event.eventIndex < oldestIndex) {
//					oldestIndex = event.eventIndex;
//					oldest = event;
//					oldestList = eventList;
//				}
//			}
//			
//			if(oldest != null) {
//				incrementIndex(oldestList);
//				return oldest;
//			}
//			
//			throw new RuntimeException("no next element!");
//		}
//
//		@Override
//		public Iterator<Event> iterator() {
//			return this;
//		}
//	}
//	
//	private static class Event{
//		
//		private final String thread;
//		private final long eventIndex;
//		private final EventType eventType;
//		private final String eventDesc;
//		
//		private static Event parseFromLine(
//				final long index, 
//				final String thread,
//				String line
//				) {
//			if(!line.contains(",")) {
//				return null;
//			}
//			
//			final int separatorIndex = line.indexOf(",");
//			
//			final String type = line.substring(0, separatorIndex);
//			final String desc = line.substring(separatorIndex + 1);
//			
//			return new Event(thread,index,EventType.valueOf(type),desc);
//		}
//		
//		private Event(String thread, long eventIndex, EventType eventType, String eventDesc) {
//			super();
//			this.thread = thread;
//			this.eventIndex = eventIndex;
//			this.eventType = eventType;
//			this.eventDesc = eventDesc;
//		}
//		
//		private Event(
//				final String thread,
//				final long index, 
//				String desc
//				) {
//			this.thread = thread;
//			this.eventIndex = index;
//			
//			if(desc.startsWith("b")) {//entered a block in the last index
//				this.eventType = EventType.CFG_BLOCK;
//				this.eventDesc = desc;
//			} else {
//				desc = desc.trim();
//				
//				final int firstSpace = desc.indexOf(" ");
//				if(firstSpace == -1) {
//					throw new RuntimeException("expected at least two sections in text: " + desc);
//				}
//				
//				final String eventTypeString = desc.substring(0,firstSpace).trim();
//				
//				if(eventTypeString.startsWith("enter@")) {
//					this.eventType = EventType.METHOD_BEGIN;
//				} else if(eventTypeString.startsWith("enter*@")) {
//					this.eventType = EventType.METHOD_BEGIN_EXTERNAL;
//				} else if(eventTypeString.startsWith("return@")) {
//					this.eventType = EventType.RETURN;
//				} else if(eventTypeString.startsWith("return*@")) {
//					this.eventType = EventType.RETURN;
//				} else if(eventTypeString.startsWith("uncaught@")) {
//					this.eventType = EventType.UNCAUGHT;
//				} else {
//					throw new RuntimeException("unhandled case: " + eventTypeString);
//				}
//				
//				this.eventDesc = desc.substring(firstSpace+1); 
//			}
//		}
//	}
//	
//	private Iterable<Event> getReplayIterator(String prefix) throws IOException {
//		final Map<String,ByteArrayOutputStream> streams = collect(prefix);
//		
//		Map<String,List<Event>> events = new LinkedHashMap<>();
//		Set<Long> uniquenessEnforcer = new HashSet<>();
//		long counter = 0L;
//		final long start = System.currentTimeMillis();
//		
//		System.out.printf("preparing replay index for \"%s\" from %d matching traces\n", prefix, streams.size());
//		for(String streamName:streams.keySet()) {
//			ByteArrayOutputStream stream = streams.get(streamName);
//			List<Event> eventsForStream = new ArrayList<>();
//			for(String line:IOUtils.readLines(new ByteArrayInputStream(stream.toByteArray()),"UTF-8")) {
//				int indexEndsAt = line.indexOf(":");
//				
//				if(indexEndsAt < 0) {
//					continue;
//				}
//				
//				final String indexPart = line.substring(0,indexEndsAt).trim();//[0,i-1]
//				final String messagePart = line.substring(indexEndsAt+1).trim();//[i+1,L]
//				
//				final long index = Long.parseLong(indexPart);
//				counter++;
//				
//				if(counter %8192 == 0) {
//					System.out.printf("\t%s\n",line);
//				}
//				
//				if(uniquenessEnforcer.contains(index)) {
//					throw new RuntimeException("multiple entries at index " + index);
//				}
//				
//				uniquenessEnforcer.add(index);
//				
//				eventsForStream.add(new Event(streamName,index,messagePart));
//			}
//			events.put(streamName,eventsForStream);
//		}
//		
//		System.out.printf("discovered %d events from %d traces in %dms\n", counter, events.size(), System.currentTimeMillis() - start);
//		
//		return new Trace(events);
//	}
//	
//	public DynamicAnalysisReplayEngineBackup(
//			String pathToReplayTgz,
//			String prefix
//			) {
//		try{
//			init(pathToReplayTgz,prefix);
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	private static String trimPath(String s) {
//		if(s.startsWith("./")) {
//			return s.substring(2);
//		} else if(s.startsWith(".")) {
//			return s.substring(1);
//		} else if(s.startsWith("/")) {
//			return s.substring(1);
//		}
//		
//		return s;
//	}
//	
//	private static String getThreadName(String path) {
//		
//		final int lastForwardSlash = path.indexOf("/",2);
//		
//		if(lastForwardSlash == -1) {
//			return path;
//		}
//		
//		return path.substring(lastForwardSlash+1).replace("/messages/", "/").replace(".trace", "");
//	}
//	
//	private boolean isTrace(ArchiveEntry a) {
//		return a.getName().endsWith(".trace");
//	}
//	
//	private boolean startsWithPrefix(ArchiveEntry a, String prefix) {
//		
//		final String prefixName = trimPath(prefix);
//		final String archiveName = trimPath(a.getName());
//		
////		System.out.printf("\t%s\n\t%s\n\n", archiveName,prefix);
//		
//		if(archiveName.startsWith(prefixName)) {
//			return true;
//		}
//		
//		return false;
//	}
//	
//	private void init(
//			final String pathToReplayTgz,
//			final String ingestFilterPrefix
//			) throws FileNotFoundException, IOException {
//		final File f = new File(pathToReplayTgz);
//		final long start = System.currentTimeMillis();
//		long numBytes = 0L;
//		try(final FileInputStream fis = new FileInputStream(f)){
//			try(final GZIPInputStream gfis = new GZIPInputStream(fis)){
//				try(final TarArchiveInputStream tgfis = new TarArchiveInputStream(gfis)){
//					boolean stop = false;
//					
//					while(!stop) {
//						ArchiveEntry a = tgfis.getNextEntry();
//						if(a == null) {
//							stop = true;
//						} else {
////							System.out.printf("%s\n",a.getName());
//							
//							if(!isTrace(a)) {
//								continue;
//							}
//							
//							if(ingestFilterPrefix != null && !startsWithPrefix(a,ingestFilterPrefix)) {
//								continue;
//							}
//							
//							final String threadName = getThreadName(a.getName());
//							
////							try(final GZIPInputStream traceStream = new GZIPInputStream(tgfis)){
////								
////							}//TODO
//							
//							numBytes += a.getSize();
//							ByteArrayOutputStream entry = new ByteArrayOutputStream();
//							
//							IOUtils.copy(tgfis, entry);
//							
//							if(entry.size() > 0) {
//								//do nothing
//								
//								System.out.printf("\t%s: %dB for thread %s\n", a.getName(), entry.size(), threadName);
//								
//								map.put(threadName, entry);
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		System.out.printf("read %dB from trace archive in %dms\n", numBytes, System.currentTimeMillis() - start);
//	}
//
//}
