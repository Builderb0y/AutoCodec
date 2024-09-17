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
	public record Example(float value, @DefaultFloat(42.0F) float fallback) {}
	AutoCoder<Example> coder = AUTO_CODEC.createCoder(Example.class);

	//decoding:
	json = { "value": 123.0 }; //notice fallback is missing here.
	Example example = AUTO_CODEC.decode(decoder, json, JsonOps.INSTANCE); //will succeed.
	assert example.value() == 123.0F;
	assert example.fallback() == 42.0F;

	//encoding:
	Example example = new Example(123.0F, 42.0F);
	JsonObject json = AUTO_CODEC.encode(coder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("value");
	assert !json.has("fallback"); //fallback is 42.0F, which is the default value, so it is not serialized.

	example = new Example(123.0F, 24.0F);
	json = AUTO_CODEC.encode(coder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("value");
	assert json.has("fallback"); //fallback is 24.0F, which is not the default value, so it is serialized.
}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultFloat {

	/** returns the default value for the annotated element. */
	public abstract float value();

	/**
	if the value being encoded is the same as {@link #value()},
	then it will only actually be encoded if this attribute returns true.
	*/
	public abstract boolean alwaysEncode() default false;

	/**
	how this value should be interpreted.
	defaults to {@link DefaultMode#DECODED}.
	*/
	public abstract DefaultMode mode() default DefaultMode.DECODED;
}