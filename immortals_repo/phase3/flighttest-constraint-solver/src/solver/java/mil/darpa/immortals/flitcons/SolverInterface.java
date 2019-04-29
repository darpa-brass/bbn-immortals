package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;

import javax.annotation.Nonnull;

public interface SolverInterface {

	void loadData(@Nonnull DynamicObjectContainer inputConfiguration, @Nonnull DynamicObjectContainer inventory);

	DynamicObjectContainer solve();

}
