package com.securboration.immortals.da;

public class Event{
	
	private final String thread;
	private final long eventIndex;
	private final EventType eventType;
	private final String eventDesc;
	
	public static Event parseFromLine(
			final long index, 
			final String thread,
			String line
			) {
		if(!line.contains(",")) {
			return null;
		}
		
		final int separatorIndex = line.indexOf(",");
		
		final String type = line.substring(0, separatorIndex);
		final String desc = line.substring(separatorIndex + 1);
		
		return new Event(thread,index,EventType.valueOf(type),desc);
	}
	
	public Event(String thread, long eventIndex, EventType eventType, String eventDesc) {
		super();
		this.thread = thread;
		this.eventIndex = eventIndex;
		this.eventType = eventType;
		this.eventDesc = eventDesc;
	}
	
	public Event(
			final String thread,
			final long index, 
			String desc
			) {
		this.thread = thread;
		this.eventIndex = index;
		
		if(desc.startsWith("b")) {//entered a block in the last index
			this.eventType = EventType.CFG_BLOCK;
			this.eventDesc = desc;
		} else {
			desc = desc.trim();
			
			final int firstSpace = desc.indexOf(" ");
			if(firstSpace == -1) {
				throw new RuntimeException("expected at least two sections in text: " + desc);
			}
			
			final String eventTypeString = desc.substring(0,firstSpace).trim();
			
			if(eventTypeString.startsWith("enter@")) {
				this.eventType = EventType.METHOD_BEGIN;
			} else if(eventTypeString.startsWith("enter*@")) {
				this.eventType = EventType.METHOD_BEGIN_EXTERNAL;
			} else if(eventTypeString.startsWith("return@")) {
				this.eventType = EventType.RETURN;
			} else if(eventTypeString.startsWith("return*@")) {
				this.eventType = EventType.RETURN;
			} else if(eventTypeString.startsWith("uncaught@")) {
				this.eventType = EventType.UNCAUGHT;
			} else {
				throw new RuntimeException("unhandled case: " + eventTypeString);
			}
			
			this.eventDesc = desc.substring(firstSpace+1); 
		}
	}

	public String getThread() {
		return thread;
	}

	public long getEventIndex() {
		return eventIndex;
	}

	public EventType getEventType() {
		return eventType;
	}

	public String getEventDesc() {
		return eventDesc;
	}
}
