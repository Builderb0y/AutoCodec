package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.encoders.EncodeContext;

/**
when applied to an array,
if the encoded value is empty {@link DecodeContext#isEmpty()},
then the decoded value will be an empty array, instead of null.
when applied to any other type, an {@link AutoConstructor}
will be created for that type, and the constructor's result
will be used as the decode result, without being imprinted.

as usual when dealing with annotated arrays, it is worth repeating
that the annotation goes between the component type and the brackets.
in other words, int @DefaultEmpty [], not @DefaultEmpty int[].

if a default empty String ("") is desired, consider
using @DefaultString("") instead of this annotation.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultEmpty {

	/**
	if true, the value will be encoded normally.
	if false, if the value encodes into an empty list or empty map,
	it will be replaced with {@link EncodeContext#empty()}.
	*/
	public boolean alwaysEncode() default false;

	/**
	if true, a shared instance of the decoded object will
	be re-used every time an empty context is decoded.
	if false, a new instance of the decoded object will
	be constructed every time an empty context is decoded.

	note: the shared instance may be mutable depending on the
	implementation of {@link AutoConstructor} used to create it.
	as such, it is important to ensure that the shared
	instance is not modified when this attribute is set to true!
	*/
	public boolean shared() default false;
}