package builderb0y.autocodec.reflection.reification;

import java.lang.reflect.*;

/**
bridge between the various sub-interfaces
of {@link AnnotatedType} and ReifiedType's.

while ReifiedType is intended to provide the
functionality of *all* sub-interfaces of AnnotatedType,
and this functionality is intended to be used directly
regardless of what underlying AnnotatedType it came from,
some circumstances still require knowing
a bit more about that underlying type.

note that ReifiedType and TypeClassification can differ
in behavior or functionality somewhat compared to the
hierarchy and functionality of {@link AnnotatedType}.
in particular:
	{@link AnnotatedWildcardType} normally provides both
	{@link AnnotatedWildcardType#getAnnotatedUpperBounds()} and
	{@link AnnotatedWildcardType#getAnnotatedLowerBounds()},
	but TypeClassification has separate constants for
	{@link #WILDCARD_EXTENDS} (upper bounds) and
	{@link #WILDCARD_SUPER} (lower bounds).

	{@link AnnotatedWildcardType} can have more than one upper or lower bound,
	despite this not being possible to specify at a language level.
	ReifiedType on the other hand only supports at most one
	{@link ReifiedType#getUpperBound()} or {@link ReifiedType#getLowerBound()}.

	{@link AnnotatedWildcardType#getAnnotatedUpperBounds()}
	specifies that if no upper bound is explicitly declared,
	whether that be because the source literal is a lower bound (? super Integer)
	or because the source literal is a pure wildcard (?),
	then the implicit upper bound is the unannotated type {@link Object}.
	by contrast, the implicit {@link ReifiedType#getUpperBound()} is only {@link Object}
	if the type is a pure wildcard. in this case, {@link ReifiedType#getClassification()}
	returns {@link #WILDCARD_EXTENDS}. if {@link ReifiedType#getClassification()} returns
	{@link #WILDCARD_SUPER}, then {@link ReifiedType#getUpperBound()} returns null.

	{@link AnnotatedTypeVariable} only has a corresponding
	TypeClassification when it can't be fully resolved.
	the entire point of type reification is to
	resolve type variables before exposing them,
	but rare edge cases prevent this from being possible in all circumstances.

	{@link AnnotatedParameterizedType} is sometimes used for a
	non-parameterized class, if it has a parameterized owner.
	for example, given the class structure: {@code
		public class Outer<T> {
			public non-static class Inner {}
		}
	}
	the type {@code Outer<String>.Inner} would use a
	{@link AnnotatedParameterizedType} whose
	{@link AnnotatedParameterizedType#getAnnotatedOwnerType()}
	is {@code Outer<String>}, but whose
	{@link AnnotatedParameterizedType#getAnnotatedActualTypeArguments() actual type arguments}
	are a zero-length array.
	by contrast, the TypeClassification of {@code Outer<String>.Inner}
	is {@link #RAW} instead, but {@link ReifiedType#getOwner()} would
	still return {@code Outer<String>} in this case.
	if Inner had its own type parameters, then the TypeClassification of
	{@code Outer<String>.Inner<String>} would be {@link #PARAMETERIZED}.

	array handling is actually consistent between
	{@link AnnotatedArrayType} and ReifiedType,
	but it is worth mentioning that the TypeClassification of an array
	type is always {@link #ARRAY}, and never {@link #RAW}, even though
	classes themselves can represent arrays too. {@link Class#isArray()}
*/
public enum TypeClassification {

	/**
	represents a simple class with no type parameters.
	for example, Number.

	@see Class
	@see AnnotatedType
	*/
	RAW,

	/**
	represents a class with type parameters.
	for example, List<Number>.

	@see ParameterizedType
	@see AnnotatedParameterizedType
	*/
	PARAMETERIZED,

	/**
	represents an array type.
	for example, Number[].

	@see GenericArrayType
	@see AnnotatedArrayType
	*/
	ARRAY,

	/**
	represents a wildcard which extends another type.
	for example, ? extends Number.

	@see WildcardType
	@see AnnotatedWildcardType
	*/
	WILDCARD_EXTENDS,

	/**
	represents a wildcard which supers another type.
	for example, ? super Number.

	@see WildcardType
	@see AnnotatedWildcardType
	*/
	WILDCARD_SUPER,

	/**
	represents a type variable which cannot be fully resolved.

	@see TypeVariable
	@see AnnotatedTypeVariable
	*/
	UNRESOLVABLE_VARIABLE;
}