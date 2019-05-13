package mil.darpa.immortals.flitcons.datatypes.dynamic;

import com.google.gson.*;
import mil.darpa.immortals.flitcons.Utils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DynamicValueTest {

	private static Gson gson;

	@BeforeClass
	public void setup() {
		gson = Utils.difGson;
	}

	private void innerParserTest(Class classTest, DynamicValue value, DynamicValueMultiplicity multiplicity, boolean shouldPass) {
		Boolean pass = null;
		try {
			switch (multiplicity) {
				case SingleValue:
					if (classTest == String.class) {
						value.parseString();
					} else if (classTest == Long.class) {
						value.parseLong();
					} else if (classTest == Float.class) {
						value.parseFloat();
					} else if (classTest == Boolean.class) {
						value.parseBoolean();
					} else if (classTest == Equation.class) {
						value.parseEquation();
					} else {
						throw new RuntimeException("Unexpected class type '" + classTest.getName() + "'!");
					}
					break;
				case Range:
					if (classTest == String.class) {
						value.parseStringRange();
					} else if (classTest == Long.class) {
						value.parseLongRange();
					} else if (classTest == Float.class) {
						value.parseFloatRange();
					} else if (classTest == Boolean.class) {
						value.parseBooleanRange();
					} else {
						throw new RuntimeException("Unexpected class type '" + classTest.getName() + "'!");
					}
					break;
				case Set:
					if (classTest == String.class) {
						value.parseStringArray();
					} else if (classTest == Long.class) {
						value.parseLongArray();
					} else if (classTest == Float.class) {
						value.parseFloatArray();
					} else if (classTest == Boolean.class) {
						value.parseBooleanArray();
					} else {
						throw new RuntimeException("Unexpected class type '" + classTest.getName() + "'!");
					}
					break;
				case NullValue:
					throw new RuntimeException("No parser for NullValue!");
			}
		} catch (Exception e) {
			Assert.assertFalse(shouldPass);
			return;
		}
		Assert.assertTrue(shouldPass);
	}


	@Test
	public void longSetTest() {
		try {
			Long long0 = -2485623454336252435L;
			Long long1 = 5485623454336252435L;
			Long long2 = 8485623454336252435L;
			Long long3 = 3365623454336252435L;
			Long[] values = {long0, long1, long2, long3};

			DynamicValue set = new DynamicValue(null, values, null);

			innerParserTest(Long.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, set, DynamicValueMultiplicity.Set, true);
			innerParserTest(Long.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(set);
			Assert.assertTrue(je instanceof JsonArray);
			JsonArray ja = (JsonArray) je;
			Assert.assertEquals(ja.size(), 4);
			Assert.assertEquals((long) long0, ja.get(0).getAsLong());
			Assert.assertEquals((long) long1, ja.get(1).getAsLong());
			Assert.assertEquals((long) long2, ja.get(2).getAsLong());
			Assert.assertEquals((long) long3, ja.get(3).getAsLong());

			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
			Assert.assertNotNull(dv.valueArray);
			Assert.assertEquals(dv.valueArray[0], long0);
			Assert.assertEquals(dv.valueArray[1], long1);
			Assert.assertEquals(dv.valueArray[2], long2);
			Assert.assertEquals(dv.valueArray[3], long3);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void longRangeTest() {
		try {
			Long long0 = -2485623454336252435L;
			Long long1 = 5485623454336252435L;
			DynamicValue range = new DynamicValue(new Range(long0, long1), null, null);

			innerParserTest(Long.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, range, DynamicValueMultiplicity.Range, true);
			innerParserTest(String.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, range, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, range, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, range, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, range, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(range);
			Assert.assertTrue(je instanceof JsonObject);
			JsonObject jo = (JsonObject) je;
			Assert.assertTrue(jo.has("Min"));
			Assert.assertEquals((long) long0, jo.get("Min").getAsLong());
			Assert.assertTrue(jo.has("Max"));
			Assert.assertEquals((long) long1, jo.get("Max").getAsLong());

			DynamicValue dv = gson.fromJson(jo, DynamicValue.class);
			Assert.assertNotNull(dv.range);
			Assert.assertEquals(dv.range.Min, long0);
			Assert.assertEquals(dv.range.Max, long1);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void longValueTest() {
		try {
			Long long0 = -2485623454336252435L;
			DynamicValue value = new DynamicValue(null, null, long0);

			innerParserTest(Long.class, value, DynamicValueMultiplicity.SingleValue, true);
			innerParserTest(Long.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(value);
			Assert.assertTrue(je instanceof JsonPrimitive);
			JsonPrimitive jp = (JsonPrimitive) je;
			Assert.assertTrue(jp.isNumber());
			Assert.assertEquals((long) long0, jp.getAsLong());

			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
			Assert.assertEquals(dv.singleValue, long0);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void floatSetTest() {
		try {
			Float float0 = -248562345.4336252435F;
			Float float1 = 5485623454.336252435F;
			Float float2 = 84856234543.36252435F;
			Float float3 = 336562345433.6252435F;
			Float[] values = {float0, float1, float2, float3};

			DynamicValue set = new DynamicValue(null, values, null);

			innerParserTest(Long.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.Set, true);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(set);
			Assert.assertTrue(je instanceof JsonArray);
			JsonArray ja = (JsonArray) je;
			Assert.assertEquals(ja.size(), 4);
			Assert.assertEquals(float0, ja.get(0).getAsFloat());
			Assert.assertEquals(float1, ja.get(1).getAsFloat());
			Assert.assertEquals(float2, ja.get(2).getAsFloat());
			Assert.assertEquals(float3, ja.get(3).getAsFloat());

			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
			Assert.assertNotNull(dv.valueArray);
			Assert.assertEquals(dv.valueArray[0], float0);
			Assert.assertEquals(dv.valueArray[1], float1);
			Assert.assertEquals(dv.valueArray[2], float2);
			Assert.assertEquals(dv.valueArray[3], float3);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void floatRangeTest() {
		try {
			Float float0 = -2485623454.336252435F;
			Float float1 = 548562345433.6252435F;
			DynamicValue range = new DynamicValue(new Range(float0, float1), null, null);

			innerParserTest(Long.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, range, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, range, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, range, DynamicValueMultiplicity.Range, true);
			innerParserTest(Boolean.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, range, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, range, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, range, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, range, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(range);
			Assert.assertTrue(je instanceof JsonObject);
			JsonObject jo = (JsonObject) je;
			Assert.assertTrue(jo.has("Min"));
			Assert.assertEquals(float0, jo.get("Min").getAsFloat());
			Assert.assertTrue(jo.has("Max"));
			Assert.assertEquals(float1, jo.get("Max").getAsFloat());

			DynamicValue dv = gson.fromJson(jo, DynamicValue.class);
			Assert.assertNotNull(dv.range);
			Assert.assertEquals(dv.range.Min, float0);
			Assert.assertEquals(dv.range.Max, float1);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void floatValueTest() {
		try {
			Float float0 = -248562345.4336252435F;
			DynamicValue value = new DynamicValue(null, null, float0);

			innerParserTest(Long.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.SingleValue, true);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(value);
			Assert.assertTrue(je instanceof JsonPrimitive);
			JsonPrimitive jp = (JsonPrimitive) je;
			Assert.assertTrue(jp.isNumber());
			Assert.assertEquals(float0, jp.getAsFloat());

			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
			Assert.assertEquals(dv.singleValue, float0);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void booleanSetTest() {
		try {
			Boolean boolean0 = true;
			Boolean boolean1 = false;
			Boolean boolean2 = false;
			Boolean boolean3 = true;
			Boolean[] values = {boolean0, boolean1, boolean2, boolean3};

			DynamicValue set = new DynamicValue(null, values, null);

			innerParserTest(Long.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.Set, true);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(set);
			Assert.assertTrue(je instanceof JsonArray);
			JsonArray ja = (JsonArray) je;
			Assert.assertEquals(ja.size(), 4);
			Assert.assertEquals((boolean) boolean0, ja.get(0).getAsBoolean());
			Assert.assertEquals((boolean) boolean1, ja.get(1).getAsBoolean());
			Assert.assertEquals((boolean) boolean2, ja.get(2).getAsBoolean());
			Assert.assertEquals((boolean) boolean3, ja.get(3).getAsBoolean());

			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
			Assert.assertNotNull(dv.valueArray);
			Assert.assertEquals(dv.valueArray[0], boolean0);
			Assert.assertEquals(dv.valueArray[1], boolean1);
			Assert.assertEquals(dv.valueArray[2], boolean2);
			Assert.assertEquals(dv.valueArray[3], boolean3);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	// Boolean range not supported

	@Test
	public void booleanValueTest() {
		try {
			Boolean boolean0 = true;
			DynamicValue value = new DynamicValue(null, null, boolean0);

			innerParserTest(Long.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.SingleValue, true);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(value);
			Assert.assertTrue(je instanceof JsonPrimitive);
			JsonPrimitive jp = (JsonPrimitive) je;
			Assert.assertTrue(jp.isBoolean());
			Assert.assertEquals((boolean) boolean0, jp.getAsBoolean());

			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
			Assert.assertEquals(dv.singleValue, boolean0);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void stringSetTest() {
		try {
			String str0 = "Hello";
			String str1 = "World";
			String str2 = "This";
			String str3 = "Is me.";
			String[] values = {str0, str1, str2, str3};

			DynamicValue set = new DynamicValue(null, values, null);

			innerParserTest(Long.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, set, DynamicValueMultiplicity.Set, true);
			innerParserTest(String.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, set, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, set, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(set);
			Assert.assertTrue(je instanceof JsonArray);
			JsonArray ja = (JsonArray) je;
			Assert.assertEquals(ja.size(), 4);
			Assert.assertEquals(str0, ja.get(0).getAsString());
			Assert.assertEquals(str1, ja.get(1).getAsString());
			Assert.assertEquals(str2, ja.get(2).getAsString());
			Assert.assertEquals(str3, ja.get(3).getAsString());

			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
			Assert.assertNotNull(dv.valueArray);
			Assert.assertEquals(str0, dv.valueArray[0]);
			Assert.assertEquals(str1, dv.valueArray[1]);
			Assert.assertEquals(str2, dv.valueArray[2]);
			Assert.assertEquals(str3, dv.valueArray[3]);

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}

	// String ranges not supported

	@Test
	public void equationValueTest() {
		try {
			Equation eq0 = new Equation("Hello Major this is Tom.");
			DynamicValue value = new DynamicValue(null, null, eq0);

			innerParserTest(Long.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Long.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Long.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(String.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Float.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.SingleValue, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Range, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.SingleValue, true);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.Set, false);
			innerParserTest(Equation.class, value, DynamicValueMultiplicity.Range, false);

			JsonElement je = gson.toJsonTree(value);
			Assert.assertTrue(je instanceof JsonObject);
			JsonObject jo = je.getAsJsonObject();
			Assert.assertNotNull(jo.get("Equation"));
			Assert.assertEquals(jo.get("Equation").getAsString(), eq0.Equation);

			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
			Assert.assertTrue(dv.singleValue instanceof Equation);
			Assert.assertTrue(eq0.Equation.equals(((Equation)dv.singleValue).Equation));

		} catch (DynamicValueeException e) {
			throw new RuntimeException(e);
		}
	}



	// Equation ranges not supported

//	@Test
//	public void stringValueTest() {
//		try {
//			String str0 = "Hello Major this is Tom.";
//			DynamicValue value = new DynamicValue(null, null, str0, null);
//
//			innerParserTest(Long.class, value, DynamicValueMultiplicity.SingleValue, false);
//			innerParserTest(Long.class, value, DynamicValueMultiplicity.Set, false);
//			innerParserTest(Long.class, value, DynamicValueMultiplicity.Range, false);
//			innerParserTest(String.class, value, DynamicValueMultiplicity.SingleValue, true);
//			innerParserTest(String.class, value, DynamicValueMultiplicity.Set, false);
//			innerParserTest(String.class, value, DynamicValueMultiplicity.Range, false);
//			innerParserTest(Float.class, value, DynamicValueMultiplicity.SingleValue, false);
//			innerParserTest(Float.class, value, DynamicValueMultiplicity.Set, false);
//			innerParserTest(Float.class, value, DynamicValueMultiplicity.Range, false);
//			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.SingleValue, false);
//			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Set, false);
//			innerParserTest(Boolean.class, value, DynamicValueMultiplicity.Range, false);
//
//			JsonElement je = gson.toJsonTree(value);
//			Assert.assertTrue(je instanceof JsonPrimitive);
//			JsonPrimitive jp = (JsonPrimitive) je;
//			Assert.assertTrue(jp.isString());
//			Assert.assertEquals(str0, jp.getAsString());
//
//			DynamicValue dv = gson.fromJson(je, DynamicValue.class);
//			Assert.assertEquals(dv.singleValue, str0);
//
//		} catch (DynamicValueeException e) {
//			throw new RuntimeException(e);
//		}
//	}
}
