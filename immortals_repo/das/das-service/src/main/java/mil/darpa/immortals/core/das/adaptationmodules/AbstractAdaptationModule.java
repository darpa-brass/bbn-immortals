package mil.darpa.immortals.core.das.adaptationmodules;

import mil.darpa.immortals.das.context.DasAdaptationContext;

public abstract class AbstractAdaptationModule implements IAdaptationModule {

	public abstract boolean isApplicable(DasAdaptationContext context) throws Exception;

	public abstract void apply(DasAdaptationContext context) throws Exception;

}
