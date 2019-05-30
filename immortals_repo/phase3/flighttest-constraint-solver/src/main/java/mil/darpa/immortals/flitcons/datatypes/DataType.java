package mil.darpa.immortals.flitcons.datatypes;

public enum DataType {
	RawInputExternalData(true, false, false, false, true),
	RawInputConfigurationData(true, false, false, false, true),
	RawInventory(true, false, false, true, false),

	InputInterconnectedData(false, false, true, false, true),

	InventoryRequirementsData(false, true, false, true, false),

	InputExternalUsageData(false, true, false, false, true),
	InputExternalRequirementsData(false, true, false, false, true),

	InputInterconnectedRequirementsData(false, true, true, false, true),
//	FaultyConfigurationExternalData_Transformed(false, true, false, false, true),
	InputInterconnectedUsageData(false, true, true, false, true);

	//	FaultyConfiguration_Raw(true, false, false, false, true, false),
	//	ValidConfiguration_Raw(true, false, false, false, true),
	//	ValidConfiguration_Interconnected(false, false, true, false, true),
	//	ValidConfigurationExternalData_Raw(true, false, false, false, true),
	//	ValidConfigurationExternalData_Transformed(false, true, false, false, true);

	public final boolean isRaw;
	public final boolean isTransformed;
	public final boolean isInterconnected;
	public final boolean isInventory;
	public final boolean isInputConfiguration;

	DataType(boolean isRaw, boolean isTransformed, boolean isInterconnected, boolean isInventory, boolean isInputConfiguration) {
		this.isRaw = isRaw;
		this.isTransformed = isTransformed;
		this.isInterconnected = isInterconnected;
		this.isInventory = isInventory;
		this.isInputConfiguration = isInputConfiguration;
	}
	}
