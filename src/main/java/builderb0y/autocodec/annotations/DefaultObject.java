package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.common.DynamicOpsContext;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;

/**
specifies the default value to use when the encoded value is not present.
the default value can be obtained from a field or a method.
this annotation can be applied to any type.
example usage: {@code
	public record IntBox(int value) {

		public static final IntBox DEFAULT = new IntBox(-1);
	}
	public record Example(@DefaultObject(name = "DEFAULT", mode = DefaultObjectMode.FIELD) IntBox box) {}

	//decoding
	json = {}
	Example example = AutoCodec.DECODE(decoder, json, JsonPos.INSTANCE);
	assert example,box == IntBox.DEFAULT

	//encoding
	Example example = new Example(IntBox.DEFAULT)
	JsonObject json = AUTO_CODEC.encode(encoder, example, JsonOps.INSTANCE).getAsJsonObject()
	assert json.size() == 0 //box is default, so it is not serialized.

	example = new Example(new IntBox(42))
	json = AUTO_CODEC.encode(encoder, example, JsonOps.INSTANCE).getAsJsonObject();
	assert json.has("box")
	assert json.getAsJsonObject("box").value() == 42
}
It is also worth noting that while the other Default<thing>
annotations modify the data input before decoding it,
DefaultObject skips this step and uses the default
object directly, without decoding it from anywhere.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultObject {

	/**
	returns the name of the field or method to obtain the default value from.
	the field or method targeted by this annotation is called "the target".
	*/
	public abstract String name();

	/**
	returns the class where the target is declared.
	if this attribute is omitted or manually set to void.class,
	then the target is searched for inside the type that this annotation is applied to.
	for example: {@code
		public class Example {

			public @DefaultObject(name = "DEFAULT", mode = DefaultObjectMode.FIELD) IntBox box;
		}
	}
	"DEFAULT" is searched for inside IntBox, not Example.
	*/
	public Class<?> in() default void.class;

	/**
	returns some information about how to find the target,
	and how to use it once it is found.

	for {@link DefaultObjectMode#FIELD}, the target must be a static
	field named {@link #name()} declared in {@link #in()} whose type
	matches the type of the element that this annotation is applied to.
	the value in the field will be obtained every time the default value is needed.

	for {@link DefaultObjectMode#METHOD_WITH_CONTEXT}, the target must be a
	static method named {@link #name()} declared in {@link #in()} which returns
	the type of the element that this annotation is applied to,
	and takes a single parameter of type {@link DynamicOpsContext}<T>,
	where T is a type parameter declared on the method. the type parameter
	T's name does not matter, only that it is the only type parameter on the
	method, and that it is used to parameterize the {@link DynamicOpsContext}.
	in other words, the method should look something like this: {@code
		public static <T_Encoded> IntBox getDefaultValue(DynamicOpsContext<T_Encoded> context)
	}
	the method may throw any exceptions, which will be caught and wrapped in
	either an {@link EncodeException} or {@link DecodeException} as applicable.
	if the method throws an {@link Error}, then it will be re-thrown as-is.
	the method will be invoked every time the default value is needed.
	in practice, the provided DynamicOpsContext will be either an {@link EncodeContext} or a
	{@link DecodeContext}, depending on what action is being taken which requires the default value.

	for {@link DefaultObjectMode#METHOD_WITHOUT_CONTEXT}, the target must be a
	static method named {@link #name()} declared in {@link #in()} which returns
	the type of the element that this annotation is applied to, and takes no parameters.
	the method may throw any exceptions, which will be caught and wrapped in
	either an {@link EncodeException} or {@link DecodeException} as applicable.
	if the method throws an {@link Error}, then it will be re-thrown as-is.
	the method will be invoked every time the default value is needed.
	*/
	public abstract DefaultObjectMode mode();

	/**
	if strict is set to false, the search conditions mentioned in {@link #mode()}
	are relaxed, and will accept fields and methods with normally invalid declarations.

	for {@link DefaultObjectMode#FIELD}, the target must be a static
	field named {@link #name()} declared in {@link #in()}, with any type.

	for {@link DefaultObjectMode#METHOD_WITH_CONTEXT}, the target must be a
	static method named {@link #name()} declared in {@link #in()} which returns
	a non-void value, and which takes a single parameter of
	type {@link DynamicOpsContext}<any parameterization>.
	the method may throw any exceptions, which will be caught and wrapped in
	either an {@link EncodeException} or {@link DecodeException} as applicable.
	if the method throws an {@link Error}, then it will be re-thrown as-is.

	for {@link DefaultObjectMode#METHOD_WITHOUT_CONTEXT}, the target must be a
	static method named {@link #name()} declared in {@link #in()} which returns
	any non-void value, and takes no parameters.
	the method may throw any exceptions, which will be caught and wrapped in
	either an {@link EncodeException} or {@link DecodeException} as applicable.
	if the method throws an {@link Error}, then it will be re-thrown as-is.

	the usage of the target after it has been found is not affected by this attribute.
	*/
	public boolean strict() default true;

	/**
	if alwaysEncode is set to false and the encoded value equals the encoded *default* value,
	then the encoded value is skipped (meaning that {@link EncodeContext#empty()}
	is returned by the AutoEncoder}.
	*/
	public boolean alwaysEncode() default false;

	public static enum DefaultObjectMode {
		FIELD,
		METHOD_WITH_CONTEXT,
		METHOD_WITHOUT_CONTEXT;
	}
}