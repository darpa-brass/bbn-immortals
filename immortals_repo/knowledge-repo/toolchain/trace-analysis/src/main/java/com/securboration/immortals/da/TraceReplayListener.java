package com.securboration.immortals.da;

import java.util.List;

public interface TraceReplayListener{
	
	public default void beforeReplay() {}
	public default void afterReplay() {}
	
	public void replayEvent(
			final Event e,
			final List<Event> stackAfterEvent,
			final List<Event> traceAfterEvent
			);
	
}
