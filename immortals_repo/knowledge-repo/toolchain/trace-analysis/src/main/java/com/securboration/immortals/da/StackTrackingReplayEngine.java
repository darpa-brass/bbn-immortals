package com.securboration.immortals.da;

import java.util.ArrayList;
import java.util.List;

public class StackTrackingReplayEngine implements TraceReplayEngine {
	
	private final TraceReplayListener listener;
	private final List<Event> stack = new ArrayList<>();
	private final List<Event> history = new ArrayList<>();
	private final BlockLookup lookup;
	
	private void reset() {
		this.stack.clear();
		this.history.clear();
	}
	
	public StackTrackingReplayEngine(
			BlockLookup blockLookup, 
			TraceReplayListener listener
			) {
		this.listener = listener;
		this.lookup = blockLookup;
	}

	@Override
	public void beforeThread(String thread) {
		reset();
		listener.beforeReplay();
	}

	@Override
	public void afterThread(String thread) {
		reset();
		listener.afterReplay();
	}
	
	private void stackBottomedOut(final String thread) {
		//TODO: this is only a problem if we care about perfect consistency
	}
	
	private void addEventToHistory(final Event e) {
		history.add(e);
	}
	
	private void methodCalled(final Event e) {
		stack.add(e);
	}
	
	private void methodExited(
			final String thread, 
			final String methodDesc
			) {
		if(stack.size() == 0) {
			stackBottomedOut(thread);
			return;
		}
		
		final Event removed = stack.remove(stack.size()-1);
		if(removed.getEventDesc().equals(methodDesc)) {
			//all is well
		} else {
			throw new RuntimeException("popped \"" + removed.getEventDesc() +"\" off the stack but expected \"" + methodDesc + "\"");
		}
		
		if(stack.size() == 0) {
//			System.out.printf("found a complete trace of length %d in %s\n",history(thread).size(),thread);
			history.clear();//TODO: this should be moved into the listener
		}
	}

	@Override
	public boolean event(Event e) {
		addEventToHistory(e);
		
		boolean isCfg = false;
		
		{//stack tracking
			if(e.getEventType().ordinal() == EventType.METHOD_BEGIN.ordinal()) {
				methodCalled(e);
			} else if(e.getEventType().ordinal() == EventType.RETURN.ordinal()) {
				methodExited(e.getThread(),e.getEventDesc());
			} else if(e.getEventType().ordinal() == EventType.UNCAUGHT.ordinal()) {
				methodExited(e.getThread(),e.getEventDesc());
			} else if(e.getEventType().ordinal() == EventType.CFG_BLOCK.ordinal()) {
				isCfg = true;
			}
		}
		
		listener.replayEvent(
				e, 
				stack, 
				history
				);
		
		if(isCfg) {
			cfg(e.getEventIndex(),e.getThread(),e.getEventDesc());
		}
		
		return true;
	}
	
	
	private boolean isExternal(final String methodDesc) {
		if(methodDesc.startsWith("java/")) {//TODO
			return true;
		}
	
		return false;
	}
	
	private void cfg(
			final long index,
			final String thread, 
			final String blockId
			) {
		if(lookup == null) {
			return;
		}
		
		if(stack.size() == 0) {
			stackBottomedOut(blockId);
			return;
		}
		final String currentMethodDesc = stack.get(stack.size()-1).getEventDesc();
		
		final String blockPart = blockId.startsWith("b") ? blockId.substring(1) : blockId;
		
		final String blockDesc = currentMethodDesc + " " + blockPart;
		
		final String[] blockCalls = lookup.getOutgoingMethodCalls(blockDesc);
		if(blockCalls == null) {
			System.err.printf("no entry for %s\n", blockDesc);
			return;
		}
		for(String blockCall:blockCalls) {
			if(!isExternal(blockCall)) {
				continue;
			}
			
			{
				Event enter = new Event(thread,index,EventType.METHOD_BEGIN,blockCall);
				stack.add(enter);
				history.add(enter);
				listener.replayEvent(enter, stack, history);
			}
			
			{
				Event exit = new Event(thread,index,EventType.RETURN,blockCall);
				stack.remove(stack.size()-1);
				history.add(exit);
				listener.replayEvent(exit, stack, history);
			}
		}
	}

}
