package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
specifies the default value to use when the encoded value is not present.
this annotation can be applied to any type which encodes into a String,
and which can be decoded from a String. this can be particularly useful for enums,
since they encode into String's too.
example usage: {@code
	public record Example(String value, @DefaultString("world") String fallback) {}
	AutoCoder<Example> coder = AUTO_CODEC.createCoder(Example.class);

	//decoding:
	json = { "value": "hello" }; //notice fallback is missing here.
	Example example = AUTO_CODEC.decode(decoder, json, JsonOps.INSTANCE); //will succeed.
	assert example.value().equals("hello");
	assert example.fallback().equals("world");

	//encoding:
	Example example = new Example("hello", "world");
	JsonObject json = AUTO_CODEC.encode(coder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("value");
	assert !json.has("fallback"); //fallback is "world", which is the default value, so it is not serialized.

	example = new Example("hello", "universe");
	json = AUTO_CODEC.encode(coder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("value");
	assert json.has("fallback"); //fallback is "universe", which is not the default value, so it is serialized.
}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultString {

	/** returns the default value for the annotated element. */
	public abstract String value();

	/**
	if the value being encoded is the same as {@link #value()},
	then it will only actually be encoded if this attribute returns true.
	*/
	public abstract boolean alwaysEncode() default false;
}