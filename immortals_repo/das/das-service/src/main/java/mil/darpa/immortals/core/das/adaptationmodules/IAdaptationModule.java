package mil.darpa.immortals.core.das.adaptationmodules;

import mil.darpa.immortals.das.context.DasAdaptationContext;

public interface IAdaptationModule {

	boolean isApplicable(DasAdaptationContext context) throws Exception;
	void apply(DasAdaptationContext context) throws Exception;
}
