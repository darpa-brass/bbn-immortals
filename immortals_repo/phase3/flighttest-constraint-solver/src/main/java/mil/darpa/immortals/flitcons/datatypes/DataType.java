package mil.darpa.immortals.flitcons.datatypes;

public enum DataType {
	Inventory_Raw(true, false, false, true, false, false),
	Inventory_Transformed(false, true, false, true, false, false),
	FaultyConfiguration_Raw(true, false, false, false, true, false),
	FaultyConfiguration_Interconnected(false, false, true, false, true, false),
	FaultyConfiguration_InterconnectedTransformed(false, true, true, false, true, false),
	FaultyConfigurationExternalData_Raw(true, false, false, false, true, false),
	FaultyConfigurationExternalData_Transformed(false, true, false, false, true, false),
	ValidConfiguration_Raw(true, false, false, false, false, true),
	ValidConfiguration_Interconnected(false, false, true, false, false, true),
	ValidConfiguration_InterconnectedTransformed(false, true, true, false, false, true),
	ValidConfigurationExternalData_Raw(true, false, false, false, false, true),
	ValidConfigurationExternalData_Transformed(false, true, false, false, false, true);

	public final boolean isRaw;
	public final boolean isTransformed;
	public final boolean isInterconnected;
	public final boolean isInventory;
	public final boolean isFaultyConfiguration;
	public final boolean isValidConfiguration;

	DataType(boolean isRaw, boolean isTransformed, boolean isInterconnected, boolean isInventory, boolean isFaultyConfiguration, boolean isValidConfiguration) {
		this.isRaw = isRaw;
		this.isTransformed = isTransformed;
		this.isInterconnected = isInterconnected;
		this.isInventory = isInventory;
		this.isFaultyConfiguration = isFaultyConfiguration;
		this.isValidConfiguration = isValidConfiguration;
	}
}
