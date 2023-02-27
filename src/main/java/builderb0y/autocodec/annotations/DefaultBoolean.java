package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
specifies the default value to use when the encoded value is not present.
this annotation can be applied to any type which encodes into a boolean,
and which can be decoded from a boolean.
example usage: {@code
	public record Example(boolean value, @DefaultBoolean(false) boolean fallback) {}
	AutoCoder<Example> coder = AUTO_CODEC.createCoder(Example.class);

	//decoding:
	json = { "value": true }; //notice fallback is missing here.
	Example example = AUTO_CODEC.decode(coder, json, JsonOps.INSTANCE); //will succeed.
	assert example.value() == true;
	assert example.fallback() == false;

	//encoding:
	Example example = new Example(true, false);
	JsonObject json = AUTO_CODEC.encode(coder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("value");
	assert !json.has("fallback"); //fallback is false, which is the default value, so it is not serialized.

	example = new Example(true, true);
	json = AUTO_CODEC.encode(coder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("value");
	assert json.has("fallback"); //fallback is true, which is not the default value, so it is serialized.
}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultBoolean {

	public abstract boolean value();

	/**
	if the value being encoded is the same as {@link #value()},
	then it will only actually be encoded if this attribute returns true.
	*/
	public abstract boolean alwaysEncode() default false;
}