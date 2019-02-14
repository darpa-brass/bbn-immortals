package com.securboration.immortals.da;

public interface TraceReplayEngine {
	public default void beforeReplay() {}
	public default void afterReplay() {}
	
	public default void beforeThread(String thread) {}
	public default void afterThread(String thread) {}
	
	public default boolean event(Event e) {
		return true;
	}
}