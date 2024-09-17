package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
specifies the default value to use when the encoded value is not present.
this annotation can be applied to any type which encodes into a number,
and which can be decoded from a number.
example usage: {@code
	public record Example(byte value, @DefaultByte(42) byte fallback) {}
	AutoCoder<Example> coder = AUTO_CODEC.createCoder(Example.class);

	//decoding:
	json = { "value": 123 }; //notice fallback is missing here.
	Example example = AUTO_CODEC.decode(decoder, json, JsonOps.INSTANCE); //will succeed.
	assert example.value() == 123;
	assert example.fallback() == 42;

	//encoding:
	Example example = new Example(123, 42);
	JsonObject json = AUTO_CODEC.encode(coder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("value");
	assert !json.has("fallback"); //fallback is 42, which is the default value, so it is not serialized.

	example = new Example(123, 24);
	json = AUTO_CODEC.encode(coder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("value");
	assert json.has("fallback"); //fallback is 24, which is not the default value, so it is serialized.
}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultByte {

	/** returns the default value for the annotated element. */
	public abstract byte value();

	/**
	if the value being encoded is the same as {@link #value()},
	then it will only actually be encoded if this attribute returns true.
	*/
	public boolean alwaysEncode() default false;

	/**
	how this value should be interpreted.
	defaults to {@link DefaultMode#DECODED}.
	*/
	public abstract DefaultMode mode() default DefaultMode.DECODED;
}