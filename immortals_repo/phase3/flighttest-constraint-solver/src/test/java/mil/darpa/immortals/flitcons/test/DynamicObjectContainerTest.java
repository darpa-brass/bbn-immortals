package mil.darpa.immortals.flitcons.test;

import com.google.gson.*;
import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicObjectContainerTest {

	private static Gson gson;

	@BeforeClass
	public void setup() {
		gson = Utils.difGson;
	}

	private static void jsonObjectValidator(@Nonnull JsonElement ele0, @Nonnull JsonElement ele1) {
		if (ele0.isJsonArray()) {
			Assert.assertTrue(ele1.isJsonArray());

			JsonArray ja0 = ele0.getAsJsonArray();
			JsonArray ja1 = ele1.getAsJsonArray();

			Assert.assertEquals(ja0.size(), ja1.size());

			for (int i = 0; i < ja0.size(); i++) {
				jsonObjectValidator(ja0.get(i), ja1.get(i));
			}

		} else if (ele0.isJsonNull()) {
			Assert.assertTrue(ele1.isJsonNull());

			JsonNull n0 = ele0.getAsJsonNull();
			JsonNull n1 = ele1.getAsJsonNull();
			Assert.assertEquals(n0, n1);

		} else if (ele0.isJsonObject()) {
			Assert.assertTrue(ele1.isJsonObject());

			JsonObject obj0 = ele0.getAsJsonObject();
			JsonObject obj1 = ele1.getAsJsonObject();

			Map<String, JsonElement> obj0EntryMap = obj0.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			Map<String, JsonElement> obj1EntryMap = obj1.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			Assert.assertEquals(obj0EntryMap.keySet(), obj1EntryMap.keySet());

			for (String key : obj0EntryMap.keySet()) {
				JsonElement e0 = obj0EntryMap.get(key);
				JsonElement e1 = obj1EntryMap.get(key);
				jsonObjectValidator(e0, e1);
			}

		} else if (ele0.isJsonPrimitive()) {
			Assert.assertTrue(ele1.isJsonPrimitive());

			JsonPrimitive p0 = ele0.getAsJsonPrimitive();
			JsonPrimitive p1 = ele1.getAsJsonPrimitive();

			if (p0.isBoolean()) {
				Assert.assertTrue(p1.isBoolean());
				Assert.assertEquals(p0.getAsBoolean(), p1.getAsBoolean());

			} else if (p0.isNumber()) {
				Assert.assertTrue((p1.isNumber()));
				Number n = p0.getAsNumber();
				Number n2 = p1.getAsNumber();
				Assert.assertEquals(n.floatValue(), n2.floatValue());
				Assert.assertEquals(n.longValue(), n2.longValue());
				Assert.assertEquals(n.byteValue(), n2.byteValue());
				Assert.assertEquals(n.doubleValue(), n2.doubleValue());
				Assert.assertEquals(n.intValue(), n2.intValue());
				Assert.assertEquals(n.shortValue(), n2.shortValue());

			} else if (p0.isString()) {
				Assert.assertTrue(p1.isString());
				Assert.assertEquals(p0.getAsString(), p1.getAsString());

			} else {
				Assert.fail("obj0 does not match an expected JSON type!");
			}

		} else {
			Assert.fail("obj0 does not match an expected JSON type!");
		}
	}

	private static void dynamicObjectContainerValidator(@Nonnull DynamicObjectContainer container0, @Nonnull DynamicObjectContainer container1) {
		validateSingleValue(container0, container1, false);
	}

	private static void dynamicValueValidator(@Nonnull DynamicValue val0, @Nonnull DynamicValue val1, boolean validateDynamicObjectContainerExtras) {
		Assert.assertEquals(val0.multiplicity, val1.multiplicity);
		switch (val0.multiplicity) {

			case SingleValue:
				validateSingleValue(val0.singleValue, val1.singleValue, validateDynamicObjectContainerExtras);
				break;

			case Range:
				Assert.assertNotNull(val0.range.Min);
				Assert.assertNotNull(val0.range.Max);
				Assert.assertNotNull(val1.range.Min);
				Assert.assertNotNull(val1.range.Max);
				validateSingleValue(val0.range.Min, val1.range.Min, validateDynamicObjectContainerExtras);
				validateSingleValue(val0.range.Max, val1.range.Max, validateDynamicObjectContainerExtras);
				break;

			case Set:
				Assert.assertNotNull(val0.valueArray);
				Assert.assertNotNull(val1.valueArray);
				Assert.assertEquals(val0.valueArray.length, val1.valueArray.length);

				for (int i = 0; i < val0.valueArray.length; i++) {
					validateSingleValue(val0.valueArray[i], val1.valueArray[i], validateDynamicObjectContainerExtras);
				}
				break;

			case NullValue:
				throw new RuntimeException("Null value detected! Undefiend values should simply be omitted!");
		}
	}

	private static void validateSingleValue(@Nonnull Object obj0, @Nonnull Object obj1, boolean validateDynamicObjectContainerExtras) {
		if (obj0 instanceof DynamicObjectContainer) {
			Assert.assertTrue(obj1 instanceof DynamicObjectContainer);
			DynamicObjectContainer d0 = (DynamicObjectContainer) obj0;
			DynamicObjectContainer d1 = (DynamicObjectContainer) obj1;

			Assert.assertEquals(d0.size(), d1.size());
			for (String key : d0.keySet()) {
				DynamicValue val0 = d0.get(key);
				DynamicValue val1 = d1.get(key);
				dynamicValueValidator(val0, val1, validateDynamicObjectContainerExtras);
			}

			if (validateDynamicObjectContainerExtras) {
				Assert.assertEquals(d0.identifier, d1.identifier);
				Assert.assertEquals(d0.debugAttributes, d1.debugAttributes);
				for (String key : d0.debugAttributes.keySet()) {
					DynamicValue val0 = d0.debugAttributes.get(key);
					DynamicValue val1 = d1.debugAttributes.get(key);
					dynamicValueValidator(val0, val1, validateDynamicObjectContainerExtras);
				}
			}

		} else if (obj0 instanceof String) {
			Assert.assertTrue(obj1 instanceof String);
			Assert.assertEquals(obj0, obj1);
		} else if (obj0 instanceof Long) {
			Assert.assertTrue(obj1 instanceof Long);
			Assert.assertEquals(obj0, obj1);
		} else if (obj0 instanceof Float) {
			Assert.assertTrue(obj1 instanceof Float);
			Assert.assertEquals(obj0, obj1);
		} else if (obj0 instanceof Boolean) {
			Assert.assertTrue(obj1 instanceof Boolean);
			Assert.assertEquals(obj0, obj1);
		}
	}

	@Test
	public void serializationDeserializationTestOne() {
		InputStreamReader isr0 = new InputStreamReader(
				DynamicObjectContainerTest.class.getClassLoader().getResourceAsStream("dummy_data/mock-dsl-input.json"));

		JsonElement json0 = gson.fromJson(isr0, JsonElement.class);

		InputStreamReader isr1 = new InputStreamReader(
				DynamicObjectContainerTest.class.getClassLoader().getResourceAsStream("dummy_data/mock-dsl-input.json"));

		DynamicObjectContainer container0 = gson.fromJson(isr1, DynamicObjectContainer.class);

		JsonElement json1 = gson.toJsonTree(container0);
		try {
			FileOutputStream fos = new FileOutputStream("dsl-input-test.json");
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			gson.toJson(container0, osw);
			osw.flush();
			osw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		jsonObjectValidator(json0, json1);
		jsonObjectValidator(json1, json0);

		DynamicObjectContainer container1 = gson.fromJson(json1, DynamicObjectContainer.class);
		dynamicObjectContainerValidator(container0, container1);
	}

	@Test
	public void serializationDeserializationTestTwo() {
		InputStreamReader isr0 = new InputStreamReader(
				DynamicObjectContainerTest.class.getClassLoader().getResourceAsStream("dummy_data/mock-dsl-output.json"));

		JsonElement json0 = gson.fromJson(isr0, JsonElement.class);

		InputStreamReader isr1 = new InputStreamReader(
				DynamicObjectContainerTest.class.getClassLoader().getResourceAsStream("dummy_data/mock-dsl-output.json"));

		DynamicObjectContainer container0 = gson.fromJson(isr1, DynamicObjectContainer.class);

		JsonElement json1 = gson.toJsonTree(container0);
		try {
			FileOutputStream fos = new FileOutputStream("dsl-input-test.json");
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			gson.toJson(container0, osw);
			osw.flush();
			osw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		jsonObjectValidator(json0, json1);
		jsonObjectValidator(json1, json0);

		DynamicObjectContainer container1 = gson.fromJson(json1, DynamicObjectContainer.class);
		dynamicObjectContainerValidator(container0, container1);
	}
}

