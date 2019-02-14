package com.securboration.immortals.da;

import java.util.List;

public interface TraversalSearchListener {
	
	/**
	 * 
	 * @param matchedEntrypointPrefix
	 * @param matchedExitpointPrefix
	 * @param stackAfterExitpoint
	 * @param trace
	 * @return true iff the search should be reset after finding the first traversal
	 */
	public boolean foundEndToEndTraversal(
			final String matchedEntrypointPrefix,
			final String matchedExitpointPrefix,
			final List<Event> stackAfterExitpoint,
			final List<Event> trace
			);

}
