package mil.darpa.immortals.flitcons.datatypes;

public interface AdvancedComparisonInterface<T> {
	boolean equivalent(T obj, boolean includeUniqueData);

	String toString(boolean includeUniqueData);
}
