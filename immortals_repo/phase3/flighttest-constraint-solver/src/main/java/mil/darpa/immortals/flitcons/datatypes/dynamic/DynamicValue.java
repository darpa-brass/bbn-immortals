package mil.darpa.immortals.flitcons.datatypes.dynamic;

import mil.darpa.immortals.flitcons.NestedPathException;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import org.apache.commons.lang.math.NumberUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static mil.darpa.immortals.flitcons.Utils.duplicateObject;

public class DynamicValue implements DuplicateInterface<DynamicValue> {

	public final HierarchicalIdentifier dataSource;
	public transient final DynamicValueMultiplicity multiplicity;
	public final Range range;
	public final Object[] valueArray;
	public final Object singleValue;

	DynamicValue(@Nullable Range range, @Nullable Object[] valueArray, @Nullable Object singleValue) throws NestedPathException {
		this.dataSource = null;
		this.range = parseValue(range);
		this.valueArray = parseValue(valueArray);
		this.singleValue = parseValue(singleValue);
		this.multiplicity = validateAndReturnMultiplicity();
	}

	private static <T> T parseValue(@Nullable Object val, @Nonnull Class<T> clazz) throws NestedPathException {
		if (val == null) {
			throw new NestedPathException("", "Value is null!");
		}
		if (val.getClass().isAssignableFrom(clazz)) {
			return (T) val;
		} else {
			throw new NestedPathException("", "Value type '" + val.getClass().getName() + "' instead of '" + clazz.getName() + "'!");
		}
	}

	private static <T> Range parseRange(@Nullable Object val, @Nonnull Class<T> clazz) throws NestedPathException {
		if (val == null) {
			throw new NestedPathException("", "Range is null!");
		} else if (!val.getClass().isAssignableFrom(Range.class)) {
			throw new NestedPathException("", "Value is not a Range!");
		}
		Range range = (Range) val;
		if (!range.Min.getClass().isAssignableFrom(clazz)) {
			throw new NestedPathException("", "Range Max type '" + range.Min.getClass().getName() + "' instead of '" + clazz.getName() + "'!");
		}
		if (!range.Max.getClass().isAssignableFrom(clazz)) {
			throw new NestedPathException("", "Range Min type '" + range.Max.getClass().getName() + "' instead of '" + clazz.getName() + "'!");
		}
		return new Range(range.Min, range.Max);
	}

	private static <T> Set<T> parseArray(@Nullable Object val, @Nonnull Class<T> clazz) throws NestedPathException {
		if (val == null) {
			throw new NestedPathException("", "Value Array is null!");
		} else if (!(val instanceof Object[])) {
			throw new NestedPathException("", "Value is not an array of objects!");
		}
		Object[] valueArray = (Object[]) val;
		Set<T> rval = new HashSet<>();
		for (int i = 0; i < valueArray.length; i++) {
			if (!(valueArray[i].getClass().isAssignableFrom(clazz))) {
				throw new NestedPathException("", "An array element is not of type String!");
			}
			rval.add((T) valueArray[i]);
		}
		return rval;
	}

	public String parseString() throws NestedPathException {
		return parseValue(singleValue, String.class);
	}

	public Set<String> parseStringArray() throws NestedPathException {
		return parseArray(valueArray, String.class);
	}

	public Range parseStringRange() throws NestedPathException {
		return parseRange(range, String.class);
	}

	public Long parseLong() throws NestedPathException {
		return parseValue(singleValue, Long.class);
	}

	public Set<Long> parseLongArray() throws NestedPathException {
		return parseArray(valueArray, Long.class);
	}

	public Range parseLongRange() throws NestedPathException {
		return parseRange(range, Long.class);
	}

	public Float parseFloat() throws NestedPathException {
		return parseValue(singleValue, Float.class);
	}

	public Set<Float> parseFloatArray() throws NestedPathException {
		return parseArray(valueArray, Float.class);
	}

	public Range parseFloatRange() throws NestedPathException {
		return parseRange(range, Float.class);
	}

	public Boolean parseBoolean() throws NestedPathException {
		return parseValue(singleValue, Boolean.class);
	}

	public Set<Boolean> parseBooleanArray() throws NestedPathException {
		return parseArray(valueArray, Boolean.class);
	}

	public Range parseBooleanRange() throws NestedPathException {
		return parseRange(range, Boolean.class);
	}

	public Equation parseEquation() throws NestedPathException {
		return parseValue(singleValue, Equation.class);
	}

	public Set<Equation> parseEquationArray() throws NestedPathException {
		return parseArray(valueArray, Equation.class);
	}

	public Range parseEquationRange() throws NestedPathException {
		return parseRange(range, Equation.class);
	}


	public Set<Long> parseLongSingleValueOrSet() throws NestedPathException {
		Set<Long> rval = null;
		switch (multiplicity) {

			case SingleValue:
				Set<Long> set = new HashSet<>();
				set.add(parseLong());
				rval = set;
				break;

			case Set:
				rval = parseLongArray();
				break;

			case Range:
			case NullValue:
				throw new NestedPathException("", "Is not Single value or set!");
		}
		return rval;
	}

	public DynamicObjectContainer parseDynamicObjectContainer() throws NestedPathException {
		return parseValue(singleValue, DynamicObjectContainer.class);
	}

	public Set<DynamicObjectContainer> parseDynamicObjectContainerArray() throws NestedPathException {
		return parseArray(valueArray, DynamicObjectContainer.class);
	}

	public static DynamicValue fromSingleValue(@Nonnull HierarchicalIdentifier dataSource, @Nonnull Object value) throws NestedPathException {
		return new DynamicValue(dataSource, null, null, value);
	}

	public static DynamicValue fromRange(@Nonnull HierarchicalIdentifier dataSource, @Nonnull Range range) throws NestedPathException {
		return new DynamicValue(dataSource, range, null, null);
	}

	public static DynamicValue fromValueArray(@Nonnull HierarchicalIdentifier dataSource, @Nonnull Object[] valueArray) throws NestedPathException {
		return new DynamicValue(dataSource, null, valueArray, null);
	}

	public DynamicValue(@Nonnull HierarchicalIdentifier dataSource, @Nullable Range range, @Nullable Object[] valueArray, @Nullable Object singleValue) throws NestedPathException {
		this.dataSource = dataSource;
		this.range = parseValue(range);
		this.valueArray = parseValue(valueArray);
		this.singleValue = parseValue(singleValue);
		this.multiplicity = validateAndReturnMultiplicity();
	}

	private static Object[] parseValue(@Nullable Object[] value) {
		if (value == null) {
			return null;
		}

		for (int i = 0; i < value.length; i++) {
			value[i] = parseValue(value[i]);
		}
		return value;
	}

	private static Range parseValue(@Nullable Range range) {
		if (range == null) {
			return null;
		}
		return new Range(parseValue(range.Min), parseValue(range.Max));
	}

	private static Equation parseValue(@Nullable Equation equation) {
		if (equation == null) {
			return null;
		}
		return new Equation((String) parseValue(equation.Equation));
	}

	private static Object parseValue(@Nullable Object value) {
		if (value == null) {
			return null;
		} else if (value instanceof String && NumberUtils.isNumber((String) value)) {
			if (((String) value).contains(".")) {
				return Float.parseFloat((String) value);
			} else if (((String) value).contains("e")) {
				return Double.valueOf((String) value).longValue();
			} else {
				return Long.parseLong((String) value);
			}
		} else if (value instanceof Object[]) {
			Object[] val = (Object[]) value;
			for (int i = 0; i < val.length; i++) {
				val[i] = parseValue(val[i]);
			}
			return val;
		} else if (value instanceof Integer) {
			return ((Integer) value).longValue();

		} else if (value instanceof Double) {
			return ((Double) value).floatValue();

		} else {
			return value;
		}
	}

	public Object getValue() {
		if (singleValue != null) {
			return singleValue;
		} else if (valueArray != null) {
			return valueArray;
		} else {
			return range;
		}
	}

	private static boolean isValueInvalid(Object value) {
		return !(value instanceof DynamicValue || value instanceof DynamicObjectContainer ||
				value instanceof Long || value instanceof Float || value instanceof Boolean || value instanceof String
				|| value instanceof Equation|| value == null);
	}

	private DynamicValueMultiplicity validateAndReturnMultiplicity() throws NestedPathException {
		// TODO: Validate range is only float or long?

		if (range != null) {
			if (range.Min == null) {
				throw new NestedPathException("", "Value found with Max defined but not Min!");

			} else if (!(range.Min instanceof Long || range.Min instanceof Float)) {
				throw new NestedPathException("", "Invalid range.Min type!");

			} else if (range.Max == null) {
				throw new NestedPathException("", "Value found with Min defined but not Max!");

			} else if (!(range.Max instanceof Long || range.Max instanceof Float)) {
				throw new NestedPathException("", "Invalid range.Min type!");

			} else if (valueArray != null) {
				throw new NestedPathException("", "Value found with Min/Max defined and valueArray defined!");

			} else if (singleValue != null) {
				throw new NestedPathException("", "Value found with Min/Max defined and value defined!");

			} else {
				return DynamicValueMultiplicity.Range;
			}

		} else if (valueArray != null) {
			if (singleValue != null) {
				throw new NestedPathException("", "Value found with valueArray and value defined!");
			} else {
				for (Object v : valueArray) {
					if (isValueInvalid(v)) {
						throw new NestedPathException("", "Invalid type '" + v.getClass().toString() + "' in valueArray!");
					}
				}
				return DynamicValueMultiplicity.Set;
			}

		} else if (singleValue != null) {
			if (isValueInvalid(singleValue)) {
				throw new NestedPathException("", "Invalid Value type found!");
			}
			return DynamicValueMultiplicity.SingleValue;
		}
		return DynamicValueMultiplicity.NullValue;
	}

	@Override
	public DynamicValue duplicate() {
		try {
			Object newSingleValue = singleValue == null ? null : duplicateObject(singleValue);
			Range newRange = range == null ? null : duplicateObject(range);
			Object[] newValueArray = null;

			if (valueArray != null) {
				newValueArray = new Object[valueArray.length];
				for (int i = 0; i < valueArray.length; i++) {
					newValueArray[i] = duplicateObject(valueArray[i]);
				}
			}

			return new DynamicValue(dataSource, newRange, newValueArray, newSingleValue);
		} catch (NestedPathException e) {
			throw AdaptationnException.internal(e.getMessage());
		}
	}
}

