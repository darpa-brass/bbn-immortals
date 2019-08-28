package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public interface SolverInterface<T> {

	T loadData(@Nonnull AbstractDataSource dataSource) throws NestedPathException;

	DynamicObjectContainer solveFromJsonFiles(@Nonnull Path inputJsonFile, @Nonnull Path inventoryJsonFile);

	DynamicObjectContainer solve();
}
