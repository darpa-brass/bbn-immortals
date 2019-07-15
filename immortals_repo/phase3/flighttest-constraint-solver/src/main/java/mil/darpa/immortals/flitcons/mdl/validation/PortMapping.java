package mil.darpa.immortals.flitcons.mdl.validation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class PortMapping {
	public final String id;

	public PortMapping(@Nonnull String id) {
		this.id = id;
	}

	public Map<String, Measurement> measurements = new HashMap<>();
	public Map<String, DataStream> dataStreams = new HashMap<>();
	public Map<String, DevicePort> devicePorts = new HashMap<>();
	public final Map<String, DauPort> dauPorts = new HashMap<>();
}
