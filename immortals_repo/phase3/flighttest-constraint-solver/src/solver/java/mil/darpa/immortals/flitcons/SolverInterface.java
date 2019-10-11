package mil.darpa.immortals.flitcons;

import com.google.gson.JsonObject;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

public interface SolverInterface<T> {

	T loadData(@Nonnull AbstractDataSource dataSource) throws NestedPathException;

	DynamicObjectContainer solveFromJsonFiles(@Nonnull Path inputJsonFile, @Nonnull Path inventoryJsonFile);

	DynamicObjectContainer solve();

	@Nullable
	JsonObject getMetrics();
}
