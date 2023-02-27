package builderb0y.autocodec.annotations;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.AutoHandler.AutoFactory;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.verifiers.AutoVerifier;

/**
used by {@link UseEncoder}, {@link UseDecoder}, {@link UseCoder},
{@link UseConstructor}, {@link UseImprinter}, and {@link UseVerifier}.
this enum defines how the targeted member will be used.

for the following documentation, it is assumed
that one of the above annotations is applied to type T,
and (handler) is one of: {@link AutoEncoder}, {@link AutoDecoder},
{@link AutoCoder}, {@link AutoConstructor}, {@link AutoImprinter},
or {@link AutoVerifier}, depending on the annotation used.

all of the above annotations have a "strict" attribute
which defines whether or not the targeted member
must match the expected type or signature exactly.
this attribute is used in the following documentation as well.
*/
public enum MemberUsage {

	/**
	when strict: the field must be static, and be of type ? extends (handler)<T>.
	when not strict: the field must be static, and be of type ? extends (handler)<any type>.
	the handler will be obtained from the field exactly once when a request for a handler is made,
	and the handler will be used to handle things from that point onwards.
	*/
	FIELD_CONTAINS_HANDLER,

	/**
	the field must be static, and be of type ? extends AutoFactory<(handler)<?>>.
	this applies to both strict and non-strict modes.
	the factory will be obtained from the field exactly once when a request for a handler is made,
	then the factory will be forced to create a handler exactly once {@link AutoFactory#forceCreate(FactoryContext)},
	then the handler will be used to handle things from that point onwards.
	*/
	FIELD_CONTAINS_FACTORY,

	/**
	when strict: the method must be static, takes no parameters,
	and have a return type of ? extends (handler)<T>.
	when not strict: the method must be static, take no parameters,
	and have a return type of ? extends (handler)<any type>.
	the method will be invoked exactly once to create a handler when a request for a handler is made,
	and the handler will be used to handle things from that point onwards.
	*/
	METHOD_RETURNS_HANDLER,

	/**
	the method must be static, takes no parameters,
	and have a return type of ? extends AutoFactory<(handler)<?>>.
	this applies to both strict and non-strict modes.
	the method will be invoked exactly once to create a factory when a request for a handler is made,
	then the factory will be forced to create a handler exactly once {@link AutoFactory#forceCreate(FactoryContext)},
	then the handler will be used to handle things from that point onwards.
	*/
	METHOD_RETURNS_FACTORY,

	/**
	when strict: the method must be static, and its signature must be assignable to that of the handler,
	including type parameters, but excluding parameter and type parameter names and annotations.
	when not strict: the method must be static, and its signature must match that of the handler,
	including raw types, but excluding all generic types, type parameters, names, and annotations.
	the method will be invoked every time something needs to be handled.
	*/
	METHOD_IS_HANDLER,

	/**
	the method is static, and its signature must be assignable to that of a factory,
	including raw types, but excluding all generic types, type parameters, names, and annotations.
	the method will be invoked exactly once to create a handler at the time a request for a handler is made,
	and the handler will be used to handle things from that point onwards.
	*/
	METHOD_IS_FACTORY;

	/**
	exception policies:

	for {@link #METHOD_RETURNS_HANDLER}, {@link #METHOD_RETURNS_FACTORY}, and
	{@link #METHOD_IS_FACTORY}: the method may declare that it throws any exceptions.
	if the method throws a {@link FactoryException} or {@link Error},
	then the exception will be re-thrown as-is.
	if the method throws any other type of exception,
	then it will be wrapped in a {@link FactoryException} before being re-thrown.

	for {@link #METHOD_IS_HANDLER}: the method may declare that it throws any exceptions,
	but any exceptions thrown by the method will be re-thrown as-is, without being wrapped.
	this can include checked exceptions! so be cautions when
	using this usage on a method which throws checked exceptions!

	for all usages, if the field contains null, or the method returns null,
	a {@link FactoryException} is thrown when a request for a handler is made.
	*/

	/**
	notes on constructors: it is worth repeating that constructors count as methods,
	and also count as static unless their declaring class is a non-static inner class.
	as such, constructors can be targeted as members by all of the above annotations
	when using one of the METHOD usages. the constructor's declaring class counts as its return type.
	this can be useful when, for example, you have a class which implements (handler)<T>,
	and whose constructor takes a FactoryContext<T> (or FactoryContext<?>) as its sole parameter.
	in this case, {@link #METHOD_IS_FACTORY} can be used to
	instantiate the class and start using it as a handler.
	*/
}