package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;

import javax.annotation.Nonnull;

public interface SolverInterface<T> {

	T loadData(@Nonnull AbstractDataSource dataSource) throws NestedPathException;

	DynamicObjectContainer solve();
}
