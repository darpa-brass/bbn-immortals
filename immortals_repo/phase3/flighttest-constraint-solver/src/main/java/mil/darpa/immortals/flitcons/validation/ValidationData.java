package mil.darpa.immortals.flitcons.validation;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueMultiplicity;
import mil.darpa.immortals.flitcons.NestedPathException;
import mil.darpa.immortals.flitcons.datatypes.dynamic.Equation;
import mil.darpa.immortals.flitcons.datatypes.dynamic.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class ValidationData {
	private final ValidationDataContainer parent;
	public final String name;
	public final DynamicValueMultiplicity multiplicity;
	public final Object value;
	private final Type type;

	private boolean isValid = false;

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean value) {
		isValid = value;
	}

	public ValidationData(@Nonnull ValidationDataContainer parent, @Nonnull String name, @Nullable DynamicValueMultiplicity multiplicity,
	                      @Nullable Object value) throws NestedPathException {
		this.parent = parent;
		this.name = name;
		this.multiplicity = multiplicity;
		this.value = value;
		validate(false, this);

		if (value instanceof Range) {
			type = ((Range) value).Max.getClass();
		} else if (value instanceof Object[]) {
			if (((Object[]) value).length > 0) {
				type = ((Object[]) value)[0].getClass();
			} else {
				type = null;
			}
		} else if (value != null) {
			type = value.getClass();
		} else {
			type = null;
		}

	}

	public Type getType() {
		return type;
	}

	public ValidationDataContainer getParent() {
		return parent;
	}

	private static boolean isValidValue(@Nonnull Object value) {
		return (value instanceof ValidationData || value instanceof ValidationDataContainer || value instanceof Long ||
				value instanceof Float || value instanceof Boolean || value instanceof String || value instanceof Equation);

	}

	private static void validate(boolean allowNull, ValidationData data) throws NestedPathException {
		switch (data.multiplicity) {

			case SingleValue:
				if (!isValidValue(data.value)) {
					throw new NestedPathException(data.name, "Invalid SingleValue value!");

				}
				break;

			case Range:
				if (!(data.value instanceof Range)) {
					throw new NestedPathException(data.name, "Invalid Range value!");

				} else {
					Range r = (Range) data.value;
					if (!isValidValue(r.Min)) {
						throw new NestedPathException(data.name, "Invalid Min Range value!");

					} else if (!isValidValue(r.Max)) {
						throw new NestedPathException(data.name, "Invalid Max Range value!");
					}
				}
				break;

			case Set:
				if (!(data.value instanceof Object[])) {
					throw new NestedPathException(data.name, "Invalid Set value!");
				} else {
					Object[] a = (Object[]) data.value;

					for (Object o : a) {
						if (!isValidValue(o)) {
							throw new NestedPathException(data.name, "Invalid Set child value!");
						}
					}
				}
				break;

			case NullValue:
				break;
		}
	}
}