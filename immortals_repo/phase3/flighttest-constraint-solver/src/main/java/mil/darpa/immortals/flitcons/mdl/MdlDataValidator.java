package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueException;
import mil.darpa.immortals.flitcons.validation.DataValidator;
import mil.darpa.immortals.flitcons.validation.ValidationDataContainer;

import javax.annotation.Nullable;
import java.io.File;

public class MdlDataValidator extends DataValidator {

	public MdlDataValidator(@Nullable File inputExcelFile, @Nullable File outputDrlFile) {
		super(inputExcelFile, outputDrlFile);
	}

	public void init() {
		super.init();
	}

	public synchronized ValidationDataContainer validate(ValidationScenario scenario, DynamicObjectContainer data) throws DynamicValueException {
		return super.validate(scenario.filter, data, scenario.configuration);
	}
}
