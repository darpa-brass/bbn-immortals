package mil.darpa.immortals.flitcons.mdl.port;

import mil.darpa.immortals.flitcons.Utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The direction of data flow for the port
 */
public enum PortDirectionEnum {
	Input,
	Output,
	Bidirectional,
	Unspecified;

	private static final Set<String> valueNames = Arrays.stream(PortDirectionEnum.values()).map(PortDirectionEnum::name).collect(Collectors.toSet());

	public static boolean contains(Object object) {
		return Utils.stringListContains(valueNames, object);
	}
}
