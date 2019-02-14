package mil.darpa.immortals.schemaevolution.datatypes;

import javax.annotation.Nonnull;

/**
 * Mock OutputData for SwRI - Scenario 6
 */
public class OutputData {

	/**
	 * A record UID to tie everything together
	 */
	public final String evaluationInstanceIdentifier;

	/**
	 * The input data that led to this. Stored as part of the output data to associate it with the results without any
	 * convoluted bookkeeping
	 */
	public final InputData inputData;

	/**
	 * Placeholder for anything and everything this object will contain.
	 *
	 * I recommend some simple top level metrics to see pass/fail/degradation at-a-glance.
	 */
	public final Object[] metrics;

	public OutputData(@Nonnull String evaluationInstanceIdentifier, @Nonnull InputData inputData, Object... metrics) {
		this.evaluationInstanceIdentifier = evaluationInstanceIdentifier;
		this.inputData = inputData;
		this.metrics = metrics;
	}
}
