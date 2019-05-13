package mil.darpa.immortals.flitcons.mdl.port;

import mil.darpa.immortals.flitcons.Utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ThermocoupleEnum {
	K,
	E,
	J,
	N,
	B,
	R,
	S,
	T,
	C,
	M;

	private static final Set<String> valueNames = Arrays.stream(ThermocoupleEnum.values()).map(ThermocoupleEnum::name).collect(Collectors.toSet());

	public static boolean contains(Object object) {
		return Utils.stringListContains(valueNames, object);
	}
}
