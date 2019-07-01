package mil.darpa.immortals.flitcons.mdl.device;

import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.mdl.port.PortDirectionEnum;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ExcitationSourceType {
	Internal,
	External;

	private static final Set<String> valueNames = Arrays.stream(PortDirectionEnum.values()).map(PortDirectionEnum::name).collect(Collectors.toSet());

	public static boolean contains(Object object) {
		return Utils.stringListContains(valueNames, object);
	}
}
