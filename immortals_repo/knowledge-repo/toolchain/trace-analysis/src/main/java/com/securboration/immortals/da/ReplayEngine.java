package com.securboration.immortals.da;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReplayEngine {
	
	private static boolean doesEventMatch(
			Event event, 
			EventType matchType, 
			String descPrefix
			) {
		if(!event.getEventType().equals(matchType)) {
			return false;
		}
		
		if(!event.getEventDesc().startsWith(descPrefix)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Analyzes a trace to determine the callstacks leading to an invocation of an
	 * interesting method
	 * 
	 * @param traceTarGz   a tar.gz archive containing traces from various threads
	 * @param matchPrefix  a prefix for selecting entries within the tar.gz archive
	 * @param blockLookup  used to lookup the non-traced calls issued within basic
	 *                     blocks (e.g., invocation of java/lang/String <init>)*
	 * @param callPrefixes vararg containing prefixes for methods to treat as
	 *                     entrypoints
	 * @return a mapping of entrypoint method descriptors to the stack states
	 *         discovered at entry
	 * @throws IOException
	 */
	public static Map<String,List<List<Event>>> getEntrypointCallstacks(
			final File traceTarGz,
			final String matchPrefix,
			final BlockLookup blockLookup,
			final String...callPrefixes
			) throws IOException{
		final Map<String,List<List<Event>>> stacks = new TreeMap<>();
		
		replay(traceTarGz,matchPrefix,blockLookup,new TraceReplayListener() {

			@Override
			public void replayEvent(Event e, List<Event> stackAfterEvent, List<Event> traceAfterEvent) {
				for(String callPrefix:callPrefixes) {
					if(!doesEventMatch(e,EventType.METHOD_BEGIN,callPrefix)) {
						continue;
					}
						
					List<List<Event>> stacksForEntrypoint = stacks.get(e.getEventDesc());
					
					if(stacksForEntrypoint == null) {
						stacksForEntrypoint = new ArrayList<>();
						stacks.put(e.getEventDesc(), stacksForEntrypoint);
					}
					
					stacksForEntrypoint.add(new ArrayList<>(stackAfterEvent));
				}
			}
		});
		
		return stacks;
	}
	
	public static Map<String,Integer> buildMatchingCallHistogram(
			final File traceTarGz, 
			final String matchPrefix,
			final BlockLookup blockLookup,
			final String...callPrefixes
			) throws IOException{
		final Map<String,Integer> matches = new TreeMap<>();
		
		replay(traceTarGz,matchPrefix,blockLookup,new TraceReplayListener() {

			@Override
			public void replayEvent(Event e, List<Event> stackAfterEvent, List<Event> traceAfterEvent) {
				for(String callPrefix:callPrefixes) {
					if(doesEventMatch(e,EventType.METHOD_BEGIN,callPrefix)) {
						
						Integer count = matches.get(e.getEventDesc());
						if(count == null) {
							count = 0;
						}
						
						matches.put(e.getEventDesc(), count+1);
					}
				}
			}
		});
		
		return matches;
	}
	
	public static void findTraversals(
			final File traceTarGz, 
			final String matchPrefix, 
			final BlockLookup blockLookup,
			final String entryPrefix,
			final String exitPrefix,
			final TraversalSearchListener whenFound
			) throws IOException{
		replay(traceTarGz,matchPrefix,blockLookup,new TraceReplayListener() {
			
			int state = 0;	//0 = waiting for prefix
							//1 = found prefix, waiting for exit
			
			final List<Event> currentHist = new ArrayList<>();

			@Override
			public void replayEvent(Event e, List<Event> stackAfterEvent, List<Event> traceAfterEvent) {
				if(state == 0) {
					if(doesEventMatch(e,EventType.METHOD_BEGIN,entryPrefix)) {
						state = 1;
						currentHist.clear();
						currentHist.add(e);
					}
				} else if(state == 1) {
					currentHist.add(e);
					
					if(doesEventMatch(e,EventType.METHOD_BEGIN,exitPrefix)) {
						System.out.println("found match! " + e.getEventDesc());//TODO
						boolean shouldReset = whenFound.foundEndToEndTraversal(
								entryPrefix, 
								exitPrefix, 
								stackAfterEvent, 
								currentHist
								);
						
						if(shouldReset) {
							state = 0;
							currentHist.clear();
						}
					}
				}
			}

			@Override
			public void beforeReplay() {
				state = 0;
				currentHist.clear();
			}
		});
	}
	
	private static List<List<Event>> getTraversals(
			final File traceTarGz, 
			final String matchPrefix, 
			final BlockLookup blockLookup,
			final String entryPrefix,
			final String exitPrefix
			) throws IOException{
		final List<List<Event>> traversals = new ArrayList<>();
		
		replay(traceTarGz,matchPrefix,blockLookup,new TraceReplayListener() {
			
			int state = 0;	//0 = waiting for prefix
							//1 = found prefix, waiting for exit
			
			final List<Event> currentHist = new ArrayList<>();

			@Override
			public void replayEvent(Event e, List<Event> stackAfterEvent, List<Event> traceAfterEvent) {
				if(state == 0) {
					if(doesEventMatch(e,EventType.METHOD_BEGIN,entryPrefix)) {
						state = 1;
						currentHist.clear();
						currentHist.add(e);
					}
				} else if(state == 1) {
					currentHist.add(e);
					
					if(doesEventMatch(e,EventType.METHOD_BEGIN,exitPrefix)) {
						state = 0;
						
						traversals.add(new ArrayList<>(currentHist));
						currentHist.clear();
						System.out.println("found match! " + e.getEventDesc());//TODO
					}
				}
			}

			@Override
			public void beforeReplay() {
				state = 0;
				currentHist.clear();
			}
		});
		
		return traversals;
	}
	
	public static List<List<Event>> getStacksAfterCalls(
			final File traceTarGz, 
			final String matchPrefix, 
			final BlockLookup blockLookup,
			final String targetMethodPrefix
			) throws IOException{
		final List<List<Event>> matches = new ArrayList<>();
		
		replay(traceTarGz,matchPrefix,blockLookup,new TraceReplayListener() {

			@Override
			public void replayEvent(Event e, List<Event> stackAfterEvent, List<Event> traceAfterEvent) {
				if(e.getEventType() == EventType.METHOD_BEGIN && e.getEventDesc().startsWith(targetMethodPrefix)) {
						matches.add(new ArrayList<>(stackAfterEvent));
					}
				}
			}
		);
		
		return matches;
	}
	
	
	
	public static void replay(
			final File traceTarGz, 
			final String matchPrefix, 
			final BlockLookup blockLookup,
			final TraceReplayListener...listeners
			) throws IOException {
		TraceEngine.trace(
				traceTarGz, 
				matchPrefix, 
				new StackTrackingReplayEngine(blockLookup,new TraceReplayListener() {
					
					@Override
					public void replayEvent(
							Event e, 
							List<Event> stackAfterEvent, 
							List<Event> traceAfterEvent
							) {
						for(TraceReplayListener listener:listeners) {
							listener.replayEvent(
									e, 
									stackAfterEvent, 
									traceAfterEvent
									);
						}
					}

					@Override
					public void beforeReplay() {
						for(TraceReplayListener listener:listeners) {
							listener.beforeReplay();
						}
					}

					@Override
					public void afterReplay() {
						for(TraceReplayListener listener:listeners) {
							listener.afterReplay();
						}
					}
			
		}));
	}

}
