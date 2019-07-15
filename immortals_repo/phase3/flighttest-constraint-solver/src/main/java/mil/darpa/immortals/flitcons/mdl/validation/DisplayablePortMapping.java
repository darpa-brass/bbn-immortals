package mil.darpa.immortals.flitcons.mdl.validation;

import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;

public class DisplayablePortMapping {

	private final String id;
	private String dataRateRangeDisplayString;
	private boolean dataRateRangePass;
	private String dataLengthRangeDisplayString;
	private boolean dataLengthRangePass;
	private String sampleRateRangeDisplayString;
	private boolean sampleRateRangePass;
	private String dataRateSelectionDisplayString;
	private boolean dataRateSelectionPass;
	private String dataLengthSelectionDisplayString;
	private boolean dataLengthSelectionPass;
	private String sampleRateSelectionDisplayString;
	private boolean sampleRateSelectionPass;
	private String directionDisplayString;
	private boolean directionPass;
	private String excitationDisplayString;
	private boolean excitationPass;
	private String thermocoupleDisplayString;
	private boolean thermocouplePass;
	private String portTypeDisplayString;
	private boolean portTypePass;

	public DisplayablePortMapping(@Nonnull String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getDataRateRangeDisplayString() {
		return dataRateRangeDisplayString;
	}

	public boolean isDataRateRangePass() {
		return dataRateRangePass;
	}

	public String getDataLengthRangeDisplayString() {
		return dataLengthRangeDisplayString;
	}

	public boolean isDataLengthRangePass() {
		return dataLengthRangePass;
	}

	public String getSampleRateRangeDisplayString() {
		return sampleRateRangeDisplayString;
	}

	public boolean isSampleRateRangePass() {
		return sampleRateRangePass;
	}

	public String getDataRateSelectionDisplayString() {
		return dataRateSelectionDisplayString;
	}

	public boolean isDataRateSelectionPass() {
		return dataRateSelectionPass;
	}

	public String getDataLengthSelectionDisplayString() {
		return dataLengthSelectionDisplayString;
	}

	public boolean isDataLengthSelectionPass() {
		return dataLengthSelectionPass;
	}

	public String getSampleRateSelectionDisplayString() {
		return sampleRateSelectionDisplayString;
	}

	public boolean isSampleRateSelectionPass() {
		return sampleRateSelectionPass;
	}

	public String getDirectionDisplayString() {
		return directionDisplayString;
	}

	public boolean isDirectionPass() {
		return directionPass;
	}

	public String getExcitationDisplayString() {
		return excitationDisplayString;
	}

	public boolean isExcitationPass() {
		return excitationPass;
	}

	public String getThermocoupleDisplayString() {
		return thermocoupleDisplayString;
	}

	public boolean isThermocouplePass() {
		return thermocouplePass;
	}

	public String getPortTypeDisplayString() {
		return portTypeDisplayString;
	}

	public boolean isPortTypePass() {
		return portTypePass;
	}

	public void setDataRateRangeResult(@Nonnull String displayString, boolean pass) {
		if (dataRateRangeDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		dataRateRangeDisplayString = displayString;
		dataRateRangePass = pass;
	}

	public void setDataLengthRangeResult(@Nonnull String displayString, boolean pass) {
		if (dataLengthRangeDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		dataLengthRangeDisplayString = displayString;
		dataLengthRangePass = pass;
	}

	public void setSampleRateRangeResult(@Nonnull String displayString, boolean pass) {
		if (sampleRateRangeDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		sampleRateRangeDisplayString = displayString;
		sampleRateRangePass = pass;
	}

	public void setDataRateSelectionResult(@Nonnull String displayString, boolean pass) {
		if (dataRateSelectionDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		dataRateSelectionDisplayString = displayString;
		dataRateSelectionPass = pass;
	}

	public void setDataLengthSelectionResult(@Nonnull String displayString, boolean pass) {
		if (dataLengthSelectionDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		dataLengthSelectionDisplayString = displayString;
		dataLengthSelectionPass = pass;
	}


	public void setSampleRateSelectionResult(@Nonnull String displayString, boolean pass) {
		if (sampleRateSelectionDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		sampleRateSelectionDisplayString = displayString;
		sampleRateSelectionPass = pass;
	}


	public void setDirectionResult(@Nonnull String displayString, boolean pass) {
		if (directionDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		directionDisplayString = displayString;
		directionPass = pass;
	}

	public void setExcitationResult(@Nonnull String displayString, boolean pass) {
		if (excitationDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		excitationDisplayString = displayString;
		excitationPass = pass;
	}

	public void setThermocoupleResult(@Nonnull String displayString, boolean pass) {
		if (thermocoupleDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		thermocoupleDisplayString = displayString;
		thermocouplePass = pass;
	}

	public void setPortTypeResult(@Nonnull String displayString, boolean pass) {
		// TODO: add check
		if (portTypeDisplayString != null) {
			throw AdaptationnException.internal("Value being set multiple times indicates a conflict!");
		}
		portTypeDisplayString = displayString;
		portTypePass = pass;
	}
}
