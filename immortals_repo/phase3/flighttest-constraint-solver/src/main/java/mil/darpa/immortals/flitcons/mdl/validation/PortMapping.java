package mil.darpa.immortals.flitcons.mdl.validation;

import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class PortMapping implements DuplicateInterface<PortMapping> {
	public final String id;

	public PortMapping(@Nonnull String id) {
		this.id = id;
	}

	public Map<String, Measurement> measurements = new HashMap<>();
	public Map<String, DataStream> dataStreams = new HashMap<>();
	public Map<String, DevicePort> devicePorts = new HashMap<>();
	public final Map<String, DauPort> dauPorts = new HashMap<>();

	@Override
	public PortMapping duplicate() {
		PortMapping rval = new PortMapping(id);
		rval.measurements = Utils.duplicateMap(measurements);
		rval.dataStreams = Utils.duplicateMap(dataStreams);
		rval.devicePorts = Utils.duplicateMap(devicePorts);
		rval.dauPorts.putAll(Utils.duplicateMap(dauPorts));
		return rval;
	}
}
