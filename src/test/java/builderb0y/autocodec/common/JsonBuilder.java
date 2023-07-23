package builderb0y.autocodec.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public interface JsonBuilder {

	public abstract JsonElement build();

	public static JsonElement toJson(Object object) {
		if (object instanceof JsonElement element  ) return element;
		if (object instanceof JsonBuilder builder  ) return builder.build();
		if (object instanceof String      string   ) return new JsonPrimitive(string);
		if (object instanceof Number      number   ) return new JsonPrimitive(number);
		if (object instanceof Character   character) return new JsonPrimitive(character);
		if (object instanceof Boolean     bool     ) return new JsonPrimitive(bool);
		throw new IllegalArgumentException(object.toString());
	}

	public static JsonObject object(String key, Object value) {
		JsonObject object = new JsonObject();
		object.add(key, toJson(value));
		return object;
	}

	public static JsonObject object(
		String key1, Object value1,
		String key2, Object value2
	) {
		JsonObject object = new JsonObject();
		object.add(key1, toJson(value1));
		object.add(key2, toJson(value2));
		return object;
	}

	public static JsonObject object(
		String key1, Object value1,
		String key2, Object value2,
		String key3, Object value3
	) {
		JsonObject object = new JsonObject();
		object.add(key1, toJson(value1));
		object.add(key2, toJson(value2));
		object.add(key3, toJson(value3));
		return object;
	}

	public static JsonObject object(
		String key1, Object value1,
		String key2, Object value2,
		String key3, Object value3,
		String key4, Object value4
	) {
		JsonObject object = new JsonObject();
		object.add(key1, toJson(value1));
		object.add(key2, toJson(value2));
		object.add(key3, toJson(value3));
		object.add(key4, toJson(value4));
		return object;
	}

	public static JsonArray array(Object... values) {
		JsonArray array = new JsonArray(values.length);
		for (Object value : values) {
			array.add(toJson(value));
		}
		return array;
	}

	public static class JsonObjectBuilder implements JsonBuilder {

		public final JsonObject object = new JsonObject();

		@Override
		public JsonObject build() {
			return this.object;
		}

		public JsonObjectBuilder add(String key, Object value) {
			this.object.add(key, toJson(value));
			return this;
		}
	}

	public static class JsonListBuilder implements JsonBuilder {

		public final JsonArray array = new JsonArray();

		@Override
		public JsonArray build() {
			return this.array;
		}

		public JsonListBuilder add(Object value) {
			this.array.add(toJson(value));
			return this;
		}
	}
}