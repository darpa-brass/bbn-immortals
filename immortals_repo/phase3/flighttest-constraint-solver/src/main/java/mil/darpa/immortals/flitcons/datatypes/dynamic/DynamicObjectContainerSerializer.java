package mil.darpa.immortals.flitcons.datatypes.dynamic;

import com.google.gson.*;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static mil.darpa.immortals.flitcons.Utils.GLOBALLY_UNIQUE_ID;

public class DynamicObjectContainerSerializer implements JsonSerializer<DynamicObjectContainer> {
	@Override
	public JsonElement serialize(DynamicObjectContainer src, Type typeOfSrc, JsonSerializationContext context) {
		return context.serialize(src.children);
	}
}
