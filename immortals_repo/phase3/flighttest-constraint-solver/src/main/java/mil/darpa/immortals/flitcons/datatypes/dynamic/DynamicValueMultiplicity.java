package mil.darpa.immortals.flitcons.datatypes.dynamic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Indicates the form a value takes in terms of multiplicity
 */
public enum DynamicValueMultiplicity {
	SingleValue,
	Range,
	Set,
	NullValue;

	public static List<String> names = Arrays.stream(DynamicValueMultiplicity.values()).map(DynamicValueMultiplicity::name).collect(Collectors.toList());
}
