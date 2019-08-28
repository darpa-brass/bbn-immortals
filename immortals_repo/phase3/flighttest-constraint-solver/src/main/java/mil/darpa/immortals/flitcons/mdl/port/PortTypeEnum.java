package mil.darpa.immortals.flitcons.mdl.port;

import mil.darpa.immortals.flitcons.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type of the port
 */
public enum PortTypeEnum {
	ARINC629429,
	ARINC629,
	Acquisition,
	Analog,
	Audio,
	Bus,
	CANbus,
	CJCCompensation,
	CardStrapping,
	ChannelControl,
	ChannelInput,
	DCAB,
	Digital,
	Ethernet,
	Excitation,
	FiberChannel,
	GPIO,
	HSDB,
	IEEE1394,
	MIL_STD_1553,
	Overhead,
	PCM,
	Serial,
	SignalConditioner,
	TestPoint,
	Thermocouple,
	Video,
	Virtual,
	Extension;

	private static final Set<String> valueNames = Collections.unmodifiableSet(Arrays.stream(PortTypeEnum.values()).map(PortTypeEnum::name).collect(Collectors.toSet()));

	public static Set<String>  getNames() {
		return valueNames;
	}
	public static boolean contains(Object object) {
		return Utils.stringListContains(valueNames, object);
	}
}