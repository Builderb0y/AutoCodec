package builderb0y.autocodec.reflection.reification;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.*;
import java.util.*;

import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryList;
import builderb0y.autocodec.reflection.AnnotationContainer;
import builderb0y.autocodec.util.*;
import builderb0y.autocodec.util.HashStrategies.NamedHashStrategy;
import builderb0y.autocodec.util.TypeFormatter.TypeFormatterAppendable;

/**
similar in concept to {@link TypeToken},
but type resolution on ReifiedTypes will preserve annotation data.

obtaining a ReifiedType:
	option 1: factory methods:
		{@link #from(Class)}
		{@link #from(AnnotatedType)}.
		{@link #parameterize(Class, ReifiedType[])},
		{@link #parameterizeWithOwner(ReifiedType, Class, ReifiedType[])},
		{@link #withOwner(ReifiedType, Class)},
		{@link #arrayOf(ReifiedType)},
		{@link #wildcardExtends(ReifiedType)},
		and {@link #wildcardSuper(ReifiedType)}.

	option 2: instantiating a ReifiedType via an anonymous subclass.
		for example: {@code new ReifiedType<@VerifyNullable String>() {}}.

using a ReifiedType:
	the primary task that ReifiedType's are designed for is type resolution.
	type resolution can be performed by several methods:
		{@link #resolveAncestor(Class)},
		{@link #resolveParameters(Class)},
		{@link #resolveParameter(Class)},
		{@link #resolveVariable(TypeVariable)},
		and {@link #resolveDeclaration(AnnotatedType)},
	see the docs on all of these methods for information about what they all do.

important: instances of ReifiedType should be treated as if they were immutable,
even though all the backing fields are non-final.
in general, these backing fields should not be used at all,
and the associated getter method should be used instead.
the backing fields are made public for other APIs
which want to build on top of the ReifiedType system.
*/
@SuppressWarnings("CallToSimpleGetterFromWithinClass")
public class ReifiedType<T> implements TypeFormatterAppendable {

	public static final @NotNull ObjectArrayFactory<ReifiedType<?>> ARRAY_FACTORY = ArrayFactories.REIFIED_TYPE;

	/**
	a cache used for {@link #from(Class)}.
	I figure that method is probably called often enough to deserve a cache.
	*/
	@Internal
	public static final ClassValue<ReifiedType<?>> FROM_CACHE = new ClassValue<>() {

		@Override
		public ReifiedType<?> computeValue(Class<?> type) {
			return TypeReifier.create(type, null, ARRAY_FACTORY.empty());
		}
	};

	/**
	a cache used for anonymous direct subclasses of ReifiedType.
	for example, {@code new ReifiedType<@VerifyNullable String>() {}}.
	without this cache we would need to re-parse generic
	type information every time the class is instantiated.
	granted, I don't expect subclasses to be used all that often,
	but I figure if {@link FactoryList} has a {@link FactoryList#cache},
	then ReifiedType should too.
	*/
	@Internal
	public static final ClassValue<ReifiedType<?>> LITERAL_CACHE = new ClassValue<>() {

		@Override
		public ReifiedType<?> computeValue(Class<?> type) {
			if (type.getSuperclass() == ReifiedType.class) {
				return (
					from(
						(
							(AnnotatedParameterizedType)(
								type.getAnnotatedSuperclass() //ReifiedType<T>
							)
						)
						.getAnnotatedActualTypeArguments() //[T]
						[0] //T
					)
				);
			}
			else {
				throw new TypeReificationException("type must be *direct* subclass of ReifiedType");
			}
		}
	};

	public static final Hash.@NotNull Strategy<@Nullable ReifiedType<?>>
		RAW_TYPE_STRATEGY = new NamedHashStrategy<>("ReifiedType.RAW_TYPE_STRATEGY") {

			@Override
			public int hashCode(ReifiedType<?> o) {
				return o == null ? 0 : System.identityHashCode(o.getRawClass());
			}

			@Override
			public boolean equals(ReifiedType<?> a, ReifiedType<?> b) {
				if (a == b) return true;
				if (a == null || b == null) return false;
				return a.getRawClass() == b.getRawClass();
			}
		},
		GENERIC_TYPE_STRATEGY = new RecursiveHashStrategy("ReifiedType.GENERIC_TYPE_STRATEGY"),
		BOXED_GENERIC_TYPE_STRATEGY = new RecursiveHashStrategy("ReifiedType.BOXED_GENERIC_TYPE_STRATEGY") {

			@Override
			public int implHashCode(@NotNull ReifiedType<?> type) {
				return super.implHashCode(type.boxed());
			}

			@Override
			public boolean implEquals(@NotNull ReifiedType<?> a, @NotNull ReifiedType<?> b) {
				return super.implEquals(a.boxed(), b.boxed());
			}
		},
		ORDERED_ANNOTATIONS_STRATEGY = new RecursiveHashStrategy("ReifiedType.ORDERED_ANNOTATIONS_STRATEGY") {

			@Override
			public int implHashCode(@NotNull ReifiedType<?> type) {
				return super.implHashCode(type) + type.getAnnotations().hashCodeOrdered();
			}

			@Override
			public boolean implEquals(@NotNull ReifiedType<?> a, @NotNull ReifiedType<?> b) {
				return super.implEquals(a, b) && a.getAnnotations().equalsOrdered(b.getAnnotations());
			}
		},
		UNORDERED_ANNOTATIONS_STRATEGY = new RecursiveHashStrategy("ReifiedType.UNORDERED_ANNOTATIONS_STRATEGY") {

			@Override
			public int implHashCode(@NotNull ReifiedType<?> type) {
				return super.implHashCode(type) + type.getAnnotations().hashCodeUnordered();
			}

			@Override
			public boolean implEquals(@NotNull ReifiedType<?> a, @NotNull ReifiedType<?> b) {
				return super.implEquals(a, b) && a.getAnnotations().equalsUnordered(b.getAnnotations());
			}
		};

	public static final @NotNull ReifiedType<Object> OBJECT = from(Object.class);
	public static final @NotNull ReifiedType<Void> VOID = from(void.class);

	public static final @NotNull ReifiedType<?> WILDCARD = wildcardExtends(OBJECT);

	/**
	sentinel value for {@link #superClassType} to indicate
	that our {@link #getRawClass()} does not have a super class.
	this field will NOT be returned by {@link #getSuperClassType()}.
	do NOT touch this field or try to use it in other code.
	*/
	@Internal
	public static final @NotNull ReifiedType<?> NO_SUPER_CLASS = new ReifiedType<>();
	/**
	sentinel value for {@link #superInterfaceTypes} to indicate
	that our {@link #getRawClass()} does not have any super interfaces.
	this field will NOT be returned by {@link #getSuperInterfaceTypes()}.
	do NOT touch this field or try to use it in other code.
	*/
	@Internal
	public static final @NotNull ReifiedType<?> @NotNull [] NO_SUPER_INTERFACES = {};

	//////////////////////////////// fields and factory methods ////////////////////////////////

	//late-initialized.
	public @NotNull List<@NotNull AnnotatedElement> annotationSources;
	//lazy-initialized.
	public @Nullable AnnotationContainer annotations;
	//late-initialized.
	public TypeClassification classification;
	//late-initialized.
	public @Nullable Class<? super T> rawClass;
	//late-initialized.
	public @Nullable Map<@NotNull TypeVariable<Class<?>>, @NotNull ReifiedType<?>> variableLookup;
	//lazy-initialized.
	public @Nullable Map<@NotNull Class<?>, @NotNull ReifiedType<?>> inheritanceLookup;
	//lazy-initialized.
	public @Nullable List<@NotNull ReifiedType<?>> inheritanceHierarchy;
	//lazy-initialized.
	public @Nullable ReifiedType<? super T> superClassType;
	//lazy-initialized.
	public @NotNull ReifiedType<? super T>[] superInterfaceTypes;
	/**
	when our {@link #getClassification()} is {@link TypeClassification#RAW} or {@link TypeClassification#PARAMETERIZED},
	this field represents our {@link #getOwner()}.

	when our {@link #getClassification()} is {@link TypeClassification#ARRAY},
	this field represents our {@link #getArrayComponentType()}.

	when our {@link #getClassification()} is {@link TypeClassification#WILDCARD_EXTENDS} or {@link TypeClassification#UNRESOLVABLE_VARIABLE},
	this field represents our {@link #getUpperBound()}.

	when our {@link #getClassification()} is {@link TypeClassification#WILDCARD_SUPER},
	this field represents our {@link #getLowerBound()}.
	*/
	//late-initialized.
	public ReifiedType<?> sharedType;
	//late-initialized.
	public @NotNull ReifiedType<?>[] parameters;
	public TypeVariable<?> unresolvableVariable;

	/**
	sole constructor intended to be invoked by an anonymous subclass.

	this constructor is protected because it is easy to expect {@link
		new ReifiedType<Integer>()
	}
	to be sufficient to create a ReifiedType whose runtime type
	of {@link T} represents type {@link Integer}, but it isn't.
	the correct way to create a ReifiedType like that is: {@code
		new ReifiedType<Integer>() {}
	}
	this will create an anonymous subclass whose
	{@link Class#getAnnotatedSuperclass()}
	contains the information necessary to reify
	the type of {@link T} to {@link Integer}.
	without a subclass, this information is impossible to reify.
	note that non-anonymous subclasses will also work,
	though why you'd want to do this I don't know.
	note also that indirect subclasses (A extends B extends ReifiedType)
	will NOT work. but I don't know why you'd want to do that either.

	if you *really* want an instance of ReifiedType which does
	NOT have the necessary information to reify {@link T},
	consider using {@link #blank()} instead.
	*/
	protected ReifiedType() {
		if (this.getClass() == ReifiedType.class) {
			this.annotationSources = new ArrayList<>(4);
		}
		else if (this.getClass().getSuperclass() == ReifiedType.class) {
			@SuppressWarnings("unchecked")
			ReifiedType<T> resolution = (ReifiedType<T>)(LITERAL_CACHE.get(this.getClass()));
			this.annotationSources    = resolution.annotationSources;
			this.classification       = resolution.classification;
			this.rawClass             = resolution.rawClass;
			this.variableLookup       = resolution.variableLookup;
			this.inheritanceLookup    = resolution.inheritanceLookup;
			this.inheritanceHierarchy = resolution.inheritanceHierarchy;
			this.superClassType       = resolution.superClassType;
			this.superInterfaceTypes  = resolution.superInterfaceTypes;
			this.sharedType           = resolution.sharedType;
			this.parameters           = resolution.parameters;
		}
		else {
			throw new TypeReificationException("Only one level of subclass is allowed.");
		}
	}

	/**
	returns a ReifiedType which is uninitialized.
	attempting to use the returned ReifiedType
	before initializing it externally will most
	likely result in exceptions being thrown.

	this method is provided solely for APIs which
	want to build on top of the ReifiedType system.
	it is also used internally by {@link TypeReifier}.
	*/
	public static <T> @NotNull ReifiedType<T> blank() {
		return new ReifiedType<>();
	}

	/** returns a ReifiedType which represents the provided AnnotatedType. */
	public static @NotNull ReifiedType<?> from(@NotNull AnnotatedType type) {
		return new TypeReifier(Collections.emptyMap(), false).reify(type);
	}

	/**
	returns a ReifiedType whose {@link #getRawClass()} is the provided class.

	if the given class has one or more {@link Class#getTypeParameters()},
	this method throws a {@link TypeReificationException}.

	if the provided class is a non-static inner class,
	then the returned ReifiedType will have an {@link #getOwner()}
	which represents the {@link Class#getEnclosingClass()} of the provided class.
	if the owner has type parameters, these will be filled in with wildcards.
	in other words, given the following class structure: {@code
		public class Outer<A> {
			public class Inner {}
		}
	},
	from(Inner.class) will return a ReifiedType<Outer<?>.Inner>.
	*/
	public static <T> @NotNull ReifiedType<T> from(@NotNull Class<T> clazz) {
		return FROM_CACHE.get(clazz).uncheckedCast();
	}

	/**
	returns a ReifiedType whose {@link #getClassification()} is {@link TypeClassification#PARAMETERIZED},
	whose {@link #getRawClass()} is the provided rawClass,
	and whose {@link #getParameters()} are the provided parameters.

	throws {@link TypeReificationException} if the provided
	rawClass does not declare any {@link Class#getTypeParameters()},
	or if it declares a different number of type parameters than what is given.

	if the provided class is a non-static inner class,
	then the returned ReifiedType will have an {@link #getOwner()}
	which represents the {@link Class#getEnclosingClass()} of the provided class.
	if the owner has type parameters too, these will also be filled in with wildcards.
	in other words, given the following class structure: {@code
		public class Outer<A> {
			public class Inner<B> {}
		}
	},
	from(Inner.class) will return a ReifiedType<Outer<?>.Inner<...>>.
	*/
	public static <T> @NotNull ReifiedType<T> parameterize(@NotNull Class<? super T> rawClass, @NotNull ReifiedType<?> @NotNull ... parameters) {
		return TypeReifier.create(rawClass, null, parameters);
	}

	/**
	returns a ReifiedType whose {@link #getRawClass()} is the provided rawClass,
	and whose {@link #getOwner()} is the provided owner.

	if the given class has 1 or more {@link Class#getTypeParameters()},
	this method throws an {@link TypeReificationException}.

	throws {@link TypeReificationException} if the provided
	owner's {@link ReifiedType#getRawClass()} does not
	match the rawClass's {@link Class#getEnclosingClass()}.
	*/
	public static <T> @NotNull ReifiedType<T> withOwner(@NotNull ReifiedType<?> owner, @NotNull Class<? super T> rawClass) {
		return TypeReifier.create(rawClass, owner, ARRAY_FACTORY.empty());
	}

	/**
	returns a ReifiedType whose {@link #getClassification()} is {@link TypeClassification#PARAMETERIZED},
	whose {@link #getRawClass()} is the provided rawClass,
	whose {@link #getOwner()} is the provided owner,
	and whose {@link #getParameters()} are the provided parameters.

	throws {@link TypeReificationException} if the provided
	owner's {@link ReifiedType#getRawClass()} does not
	match the rawClass's {@link Class#getEnclosingClass()}.

	also throws {@link TypeReificationException} if the provided
	rawClass does not declare any {@link Class#getTypeParameters()},
	or if it declares a different number of type parameters than what is given.
	*/
	public static <T> @NotNull ReifiedType<T> parameterizeWithOwner(@NotNull ReifiedType<?> owner, @NotNull Class<? super T> rawClass, @NotNull ReifiedType<?> @NotNull ... parameters) {
		return TypeReifier.create(rawClass, owner, parameters);
	}

	/**
	returns a ReifiedType whose {@link #getRawClass()} is the provided class.

	if the provided rawClass has 1 or more {@link Class#getTypeParameters()},
	then these will be filled in with {@link #WILDCARD},
	and the returned ReifiedType's {@link #getClassification()}
	will be {@link TypeClassification#PARAMETERIZED}.
	otherwise, the returned ReifiedType's {@link #getClassification()}
	will be {@link TypeClassification#RAW}.

	if the provided class is a non-static inner class,
	then the returned ReifiedType will have an {@link #getOwner()}
	which represents the {@link Class#getEnclosingClass()} of the provided class.
	if the owner has type parameters too, then these will also be filled in with wildcards.
	in other words, given the following class structure: {@code
		public class Outer<A> {
			public class Inner<B> {}
		}
	},
	parameterizeWithWildcards(Inner.class) will return a ReifiedType<Outer<?>.Inner<?>>.
	*/
	public static <T> @NotNull ReifiedType<T> parameterizeWithWildcards(@NotNull Class<? super T> rawClass) {
		return TypeReifier.create(rawClass, null, (ReifiedType<?>[])(null));
	}

	/**
	returns a ReifiedType whose {@link #getClassification()} is {@link TypeClassification#ARRAY},
	and whose {@link #getArrayComponentType()} is the provided type.
	*/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> @NotNull ReifiedType<T[]> arrayOf(@NotNull ReifiedType<T> type) {
		ReifiedType<T[]> result = blank();
		result.classification = TypeClassification.ARRAY;
		result.sharedType = type;
		Class<?> componentClass = type.getRawClass();
		if (componentClass != null) result.rawClass = (Class)(componentClass.arrayType());
		return result;
	}

	/**
	returns a ReifiedType whose {@link #getClassification()}
	is {@link TypeClassification#WILDCARD_EXTENDS},
	and whose {@link #getUpperBound()} is the provided upperBound.
	*/
	public static <T> @NotNull ReifiedType<? extends T> wildcardExtends(@NotNull ReifiedType<T> upperBound) {
		ReifiedType<? extends T> result = blank();
		result.classification = TypeClassification.WILDCARD_EXTENDS;
		result.sharedType = upperBound;
		return result;
	}

	/**
	returns a ReifiedType whose {@link #getClassification()}
	is {@link TypeClassification#WILDCARD_SUPER},
	and whose {@link #getLowerBound()} is the provided lowerBound.
	*/
	public static <T> @NotNull ReifiedType<? super T> wildcardSuper(@NotNull ReifiedType<T> lowerBound) {
		ReifiedType<? super T> result = blank();
		result.classification = TypeClassification.WILDCARD_SUPER;
		result.sharedType = lowerBound;
		return result;
	}

	/**
	returns a ReifiedType whose {@link #getClassification()}
	is {@link TypeClassification#UNRESOLVABLE_VARIABLE},
	whose {@link #getUnresolvableVariable()} is the provided variable,
	and whose {@link #getUpperBound()} is the reification of the
	provided variable's {@link TypeVariable#getAnnotatedBounds()}[0].
	*/
	public static <T> @NotNull ReifiedType<? extends T> unresolvableVariable(@NotNull TypeVariable<?> variable) {
		ReifiedType<? extends T> result = blank();
		result.classification = TypeClassification.UNRESOLVABLE_VARIABLE;
		result.sharedType = from(variable.getAnnotatedBounds()[0]);
		result.unresolvableVariable = variable;
		result.annotationSources.add(variable);
		return result;
	}

	//////////////////////////////// resolution ////////////////////////////////

	/**
	internal method to create and populate the {@link #inheritanceLookup}.
	this lookup is used by {@link #resolveAncestor(Class)} to get the
	ReifiedType associated with arbitrary ancestor {@link Class}'s.
	*/
	public @NotNull Map<@NotNull Class<?>, @NotNull ReifiedType<?>> getInheritanceLookup() {
		Map<Class<?>, ReifiedType<?>> lookup = this.inheritanceLookup;
		if (lookup == null) {
			Class<?> rawClass = this.getRawClass();
			if (rawClass == null) {
				lookup = Collections.emptyMap();
			}
			else {
				lookup = new LinkedHashMap<>(8);
				lookup.put(rawClass, this);
				ReifiedType<?>[] superInterfaces = this.getSuperInterfaceTypes();
				if (superInterfaces != null) {
					for (ReifiedType<?> superInterface : superInterfaces) {
						for (Map.Entry<Class<?>, ReifiedType<?>> entry : superInterface.getInheritanceLookup().entrySet()) {
							lookup.putIfAbsent(entry.getKey(), entry.getValue());
						}
					}
				}
				ReifiedType<? super T> superClass = this.getSuperClassType();
				if (superClass != null) {
					for (Map.Entry<Class<?>, ReifiedType<?>> entry : superClass.getInheritanceLookup().entrySet()) {
						lookup.putIfAbsent(entry.getKey(), entry.getValue());
					}
				}
			}
			assert lookup != null;
			this.inheritanceLookup = lookup;
		}
		return lookup;
	}

	/**
	populates type parameter information and annotations of the given
	raw class under the assumption that {@link T} extends {@link T_Raw}.
	for example, if {@link T} represents an ArrayList<@A String>,
	then resolving List.class would return a ReifiedType representing List<@A String>.
	the type String can be queried via {@code
		{@link #getParameters()}[0].{@link ReifiedType#getRawClass()},
	}
	and the annotation @A can be queried via {@code
		{@link #getParameters()}[0].{@link ReifiedType#getDeclaredAnnotations()}.{@link AnnotationContainer#getFirst(Class)}(A.class)
	}

	if type {@link T} does not extend rawClass, returns null.
	*/
	@SuppressWarnings("unchecked")
	public <T_Raw> @Nullable ReifiedType<? extends T_Raw> resolveAncestor(@NotNull Class<T_Raw> rawClass) {
		if (this.getRawClass() == null) return null;
		return (ReifiedType<? extends T_Raw>)(
			this.getRawClass() == rawClass
			? this
			: this.getInheritanceLookup().get(rawClass)
		);
	}

	/**
	populates type parameter information and annotations of the given
	declaration under the assumption that this ReifiedType represents
	a (possibly parameterized) class, and the declaration AnnotatedType
	was obtained from a field or method declared in that class.

	for example, given the following class structure: {@code
		public class Box<T> {
			public T value;
		}
	},
	if you had a ReifiedType<Box<@A String>>, then {@code
		type.resolveDeclaration(Box.class.getDeclaredField("value").getAnnotatedType())
	}
	would return a ReifiedType<@A String>.
	*/
	public @NotNull ReifiedType<?> resolveDeclaration(@NotNull AnnotatedType declaration) {
		return new TypeReifier(this.variableLookup, true).reify(declaration);
	}

	/**
	shortcut for resolveAncestor(ancestor).getParameters(), with null checks.
	for example, ReifiedType<HashMap<String, Integer>>.resolveParameters(Map.class)
	would return [ReifiedType<String>, ReifiedType<Integer>].
	*/
	public @NotNull ReifiedType<?> @Nullable [] resolveParameters(@NotNull Class<?> ancestor) {
		ReifiedType<?> resolution = this.resolveAncestor(ancestor);
		return resolution != null ? resolution.getParameters() : null;
	}

	/**
	shortcut for resolveAncestor(ancestor).getParameters()[0], with null checks.
	for example, ReifiedType<ArrayList<String>>.resolveParameter(List.class)
	would return a ReifiedType<String>.
	*/
	public @Nullable ReifiedType<?> resolveParameter(@NotNull Class<?> ancestor) {
		ReifiedType<?>[] parameters = this.resolveParameters(ancestor);
		return parameters != null ? parameters[0] : null;
	}

	/**
	resolves the provided variable against our ancestor hierarchy.
	explaining this is challenging, but an example will make things easier:
	ReifiedType<ArrayList<String>>.resolveVariable(List.class.getTypeVariables()[0])
	would return a ReifiedType<String>.
	to resolve more than one type variable at once, consider using {@link #resolveParameters(Class)}.
	*/
	public @Nullable ReifiedType<?> resolveVariable(@NotNull TypeVariable<Class<?>> variable) {
		Class<?> declarer = variable.getGenericDeclaration();
		ReifiedType<?> ancestor = this.resolveAncestor(declarer);
		if (ancestor != null && ancestor.variableLookup != null) {
			return ancestor.variableLookup.get(variable);
		}
		return null;
	}

	/**
	returns a {@link Collection} containing the full inheritance hierarchy of this ReifiedType.
	the ordering of the elements inside the returned Collection is:
		this ReifiedType
		the interfaces implemented by this ReifiedType
		the super type
		the interfaces implemented by the super type
		the super's super type
		the interfaces implemented by the super's super type
		etc.
	if an interface is implemented multiple times,
	then the returned Collection will only contain it once,
	at the first position it occurs.
	for example, given the following class structure: {@code
		interface I {}
		interface I2 extends I {}
		class A implements I {}
		class B extends A implements I2 {}
	},
	the inheritance hierarchy of B is: [ B, I2, I, A, Object ]
	*/
	//todo: migrate to SequencedCollection when that's available. https://openjdk.org/jeps/8280836
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public @NotNull List<@NotNull ReifiedType<? super T>> getInheritanceHierarchy() {
		List<ReifiedType<?>> hierarchy = this.inheritanceHierarchy;
		if (hierarchy == null) {
			hierarchy = this.inheritanceHierarchy = new ArrayList<>(this.getInheritanceLookup().values());
		}
		return (List)(hierarchy);
	}

	//////////////////////////////// properties ////////////////////////////////

	/**
	returns the raw class of type {@link T}.

	if this ReifiedType represents a {@link TypeClassification#RAW} class,
	then this method will return that class.

	if this ReifiedType represents a {@link TypeClassification#PARAMETERIZED} class,
	then this method will return that type's {@link ParameterizedType#getRawType()}.
	for example, the raw type of {@code
		@A List<@B String>
	}
	is List.class.

	if this ReifiedType represents an {@link TypeClassification#ARRAY},
	then this method will return the array type, stripped of generic and annotation information,
	if the {@link #getArrayComponentType()} has a raw class.
	for example, the raw type of {@code
		@A List<@B String> @C []
	}
	is List[].class.
	note that if the array component type does NOT have a raw class, then this method returns null.
	for example: {@code
		class ArrayBox<T> {
			T[] values;
		}
		...
		ReifiedType<?> arrayType = (
			new ReifiedType<ArrayBox<? extends Number>>() {}
			.resolveDeclaration(ArrayBox.class.getDeclaredField("values").getAnnotatedType())
		);
		assert arrayType.getClassification() == TypeClassification.ARRAY;
		assert arrayType.getArrayComponentType() != null;
		assert arrayType.getRawClass() == null;
	}

	if this ReifiedType represents a {@link TypeClassification#WILDCARD_EXTENDS}
	or {@link TypeClassification#WILDCARD_SUPER}, then this method returns null.
	meaning that the raw type of <? extends String> is null.
	however, {@link #getUpperBound()} and {@link #getLowerBound()}
	will likely have a valid raw type.
	*/
	public @Nullable Class<? super T> getRawClass() {
		return this.rawClass;
	}

	/**
	same as {@link #getRawClass()}, but if a raw class is not applicable,
	this method will throw an {@link IllegalStateException} instead of returning null.
	*/
	public @NotNull Class<? super T> requireRawClass() {
		Class<? super T> rawClass = this.rawClass;
		if (rawClass != null) return rawClass;
		else throw new IllegalStateException(this + " does not have a raw class.");
	}

	/**
	returns the (possibly parameterized) super class type of {@link T}.
	for example, the super class of ArrayList<@A String> is AbstractList<@A String>.
	if this ReifiedType represents a type whose {@link #getRawClass()}
	is either null, or itself does not have a {@link Class#getSuperclass()},
	then this method returns null.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable ReifiedType<? super T> getSuperClassType() {
		ReifiedType<? super T> superClassType = this.superClassType;
		if (superClassType == null) {
			Class<?> rawClass = this.getRawClass();
			if (rawClass == null) {
				superClassType = (ReifiedType<? super T>)(NO_SUPER_CLASS);
			}
			else {
				AnnotatedType annotatedSuperClass = rawClass.getAnnotatedSuperclass();
				if (annotatedSuperClass == null) {
					superClassType = (ReifiedType<? super T>)(NO_SUPER_CLASS);
				}
				else {
					superClassType = (ReifiedType<? super T>)(
						new TypeReifier(this.variableLookup, true).reify(annotatedSuperClass)
					);
				}
			}
			assert superClassType != null;
			this.superClassType = superClassType;
		}
		return superClassType == NO_SUPER_CLASS ? null : superClassType;
	}

	/**
	returns the (possibly parameterized) super interfaces of {@link T}.
	for example, the super interfaces of type ArrayList<@A String>
	are List<@A String>, RandomAccess, Cloneable, and Serializable.
	if this ReifiedType represents a type whose {@link #getRawClass()}
	is either null, or itself does not have any {@link Class#getInterfaces()},
	then this method returns null.
	importantly, this behavior differs from {@link Class#getInterfaces()}
	in that if this ReifiedType represents a class which implements nothing,
	null is returned instead of an empty array.

	this method does NOT defensively copy its return value!
	do not modify the returned array!
	*/
	@SuppressWarnings("unchecked")
	public @NotNull ReifiedType<? super T> @Nullable [] getSuperInterfaceTypes() {
		ReifiedType<? super T>[] superInterfaceTypes = this.superInterfaceTypes;
		if (superInterfaceTypes == null) {
			Class<?> rawClass = this.getRawClass();
			if (rawClass == null) {
				superInterfaceTypes = (ReifiedType<? super T>[])(NO_SUPER_INTERFACES);
			}
			else {
				AnnotatedType[] annotatedSuperInterfaces = rawClass.getAnnotatedInterfaces();
				int length;
				if (annotatedSuperInterfaces == null || (length = annotatedSuperInterfaces.length) == 0) {
					superInterfaceTypes = (ReifiedType<? super T>[])(NO_SUPER_INTERFACES);
				}
				else {
					superInterfaceTypes = new ReifiedType[length];
					for (int index = 0; index < length; index++) {
						superInterfaceTypes[index] = (ReifiedType<? super T>)(
							new TypeReifier(this.variableLookup, true).reify(annotatedSuperInterfaces[index])
						);
					}
				}
			}
			assert superInterfaceTypes != null;
			this.superInterfaceTypes = superInterfaceTypes;
		}
		return superInterfaceTypes == NO_SUPER_INTERFACES ? null : superInterfaceTypes;
	}

	/**
	if this ReifiedType has an {@link AnnotatedType#getAnnotatedOwnerType()},
	returns that owner. otherwise, returns null.
	*/
	public @Nullable ReifiedType<?> getOwner() {
		return this.getClassification() == TypeClassification.RAW || this.getClassification() == TypeClassification.PARAMETERIZED ? this.sharedType : null;
	}

	/**
	if {@link T} represents an {@link TypeClassification#ARRAY}, returns that array's
	{@link AnnotatedArrayType#getAnnotatedGenericComponentType()}.
	otherwise, returns null.
	*/
	public @Nullable ReifiedType<?> getArrayComponentType() {
		return this.getClassification() == TypeClassification.ARRAY ? this.sharedType : null;
	}

	/**
	if {@link T} represents a {@link TypeClassification#WILDCARD_EXTENDS},
	returns that wildcard's {@link AnnotatedWildcardType#getAnnotatedUpperBounds()}.
	otherwise, if {@link T} represents an {@link TypeClassification#UNRESOLVABLE_VARIABLE},
	returns that variable's first {@link AnnotatedTypeVariable#getAnnotatedBounds()}.
	otherwise, returns null.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable ReifiedType<? super T> getUpperBound() {
		return this.getClassification() == TypeClassification.WILDCARD_EXTENDS || this.getClassification() == TypeClassification.UNRESOLVABLE_VARIABLE ? (ReifiedType<? super T>)(this.sharedType) : null;
	}

	public @NotNull ReifiedType<? super T> getUpperBoundOrSelf() {
		ReifiedType<? super T> bound = this.getUpperBound();
		return bound != null ? bound : this;
	}

	/**
	if {@link T} represents a {@link TypeClassification#WILDCARD_SUPER},
	returns that wildcard's {@link AnnotatedWildcardType#getAnnotatedLowerBounds()}.
	otherwise, returns null.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable ReifiedType<? extends T> getLowerBound() {
		return this.getClassification() == TypeClassification.WILDCARD_SUPER ? (ReifiedType<? extends T>)(this.sharedType) : null;
	}

	public @NotNull ReifiedType<? extends T> getLowerBoundOrSelf() {
		ReifiedType<? extends T> bound = this.getLowerBound();
		return bound != null ? bound : this;
	}

	public @Nullable ReifiedType<?> getBound() {
		return (
			this.getClassification() == TypeClassification.WILDCARD_EXTENDS ||
			this.getClassification() == TypeClassification.WILDCARD_SUPER
			? this.sharedType
			: null
		);
	}

	public @NotNull ReifiedType<?> getBoundOrSelf() {
		ReifiedType<?> bound = this.getBound();
		return bound != null ? bound : this;
	}

	/**
	if {@link T} represents a parameterized type, returns that type's
	{@link AnnotatedParameterizedType#getAnnotatedActualTypeArguments()}.
	otherwise, returns null.

	if the underlying {@link #getRawClass()}
	has {@link Class#getTypeParameters()},
	but the runtime type of {@link T} is raw,
	then you are in an impossible state.
	the type Map will never be a valid ReifiedType.

	if the runtime type of {@link T} is parameterized by necessity
	to support a generic {@link ParameterizedType#getOwnerType()},
	but the {@link #getRawClass()} does not support parameters,
	then this method returns null.
	for example, given the class structure: {@code
		class Outer<T> {
			class Inner {}
		}
	}
	the type Outer<String>.Inner will NOT have any type parameters, despite the
	underlying type being an {@link AnnotatedParameterizedType} in this case.

	this method does NOT defensively clone its return value!
	do not modify the returned array!
	*/
	public @NotNull ReifiedType<?> @Nullable [] getParameters() {
		return this.parameters;
	}

	/** see {@link TypeClassification} for a description of what this is for. */
	public @NotNull TypeClassification getClassification() {
		return this.classification;
	}

	/**
	if this ReifiedType is unresolvable (see {@link TypeClassification#UNRESOLVABLE_VARIABLE}),
	returns the TypeVariable which failed to be resolved properly.
	otherwise, returns null.
	this method is primarily intended for debugging purposes.
	*/
	public @Nullable TypeVariable<?> getUnresolvableVariable() {
		return this.unresolvableVariable;
	}

	//////////////////////////////// boxing ////////////////////////////////

	/**
	if {@link T} corresponds to a primitive type {@link Class#isPrimitive()},
	returns a copy of this ReifiedType with the same annotations,
	but with the {@link #getRawClass()} changed to
	the corresponding wrapper type of {@link T}.
	if {@link T} does NOT correspond to a primitive type,
	this ReifiedType is returned as-is.

	for example, if this were a ReifiedType<int>,
	this method would return a ReifiedType<Integer>.
	*/
	public @NotNull ReifiedType<T> boxed() {
		return TypeReifier.maybeCopyForBoxing(this, Primitives::wrap);
	}

	/**
	returns true if {@link T} represents a wrapper for a primitive
	type {@link Primitives#isWrapperType(Class)}; false otherwise.
	*/
	public boolean isBoxed() {
		return this.getRawClass() != null && Primitives.isWrapperType(this.getRawClass());
	}

	/**
	if {@link T} corresponds to a wrapper for a primitive type,
	returns a copy of this ReifiedType with the same annotations,
	but with the {@link #getRawClass()} changed to
	the corresponding primitive type of {@link T}.
	if {@link T} does NOT correspond to a wrapper for a
	primitive type, this ReifiedType is returned as-is.

	for example, if this were a ReifiedType<Integer>,
	this method would return a ReifiedType<int>.
	*/
	public @NotNull ReifiedType<T> unboxed() {
		return TypeReifier.maybeCopyForBoxing(this, Primitives::unwrap);
	}

	/**
	returns true if {@link T} represents a primitive
	type {@link Class#isPrimitive()}; false otherwise.
	*/
	public boolean isPrimitive() {
		return this.getRawClass() != null && this.getRawClass().isPrimitive();
	}

	//////////////////////////////// annotations ////////////////////////////////

	/**
	returns all the annotations declared on either this ReifiedType,
	its {@link #getRawClass()}, or superclasses of its
	{@link #getRawClass()} if they are {@link Inherited}.
	for example: {@code
		@A
		public class Foo {}
		...
		ReifiedType<@B Foo> type = ...;
		assert type.getAnnotations().has(A.class);
		assert type.getAnnotations().has(B.class);
	}
	note that if the underlying type was substituted from a {@link AnnotatedTypeVariable},
	the annotations returned by this method will ALSO include
	those defined on the {@link TypeVariable} itself.
	for example: {@code
		public class Box<@A T> {
			public @B T value;
		}
		...
		ReifiedType<Box<@C String>> boxType = ...;
		ReifiedType<?> valueType = boxType.resolveDeclaration(Box.class.getDeclaredField("value").getAnnotatedType());
		assert valueType.getAnnotations().has(A.class);
		assert valueType.getAnnotations().has(B.class);
		assert valueType.getAnnotations().has(C.class);
	}
	*/
	public @NotNull AnnotationContainer getAnnotations() {
		AnnotationContainer annotations = this.annotations;
		if (annotations == null) {
			annotations = this.annotations = AnnotationContainer.fromAll(
				ArrayFactories.ANNOTATED_ELEMENT.collectionToArray(this.annotationSources)
			);
		}
		return annotations;
	}

	/**
	special case of {@link #addAnnotations(Annotation...)}
	where there is only one annotation to add.
	*/
	public @NotNull ReifiedType<T> addAnnotation(@NotNull Annotation annotation) {
		AnnotatedElement element = new AnnotatedElement() {

			@Override
			public <A extends Annotation> @Nullable A getAnnotation(@NotNull Class<A> annotationClass) {
				return this.getDeclaredAnnotation(annotationClass);
			}

			@Override
			public <A extends Annotation> @Nullable A getDeclaredAnnotation(@NotNull Class<A> annotationClass) {
				return annotation.annotationType() == annotationClass ? annotationClass.cast(annotation) : null;
			}

			@Override
			public @NotNull Annotation @NotNull [] getAnnotations() {
				return this.getDeclaredAnnotations();
			}

			@Override
			public @NotNull Annotation @NotNull [] getDeclaredAnnotations() {
				return new Annotation[] { annotation };
			}
		};
		ReifiedType<T> type = TypeReifier.copyForAnnotations(this, true);
		type.annotationSources.add(0, element);
		return type;
	}

	/**
	returns a ReifiedType which represents the same type as this one,
	but has additional provided annotations.
	these annotations will be reflected by {@link #getAnnotations()}.
	*/
	public @NotNull ReifiedType<T> addAnnotations(@NotNull Annotation @NotNull ... annotations) {
		if (annotations.length == 0) return this;
		if (annotations.length == 1) return this.addAnnotation(annotations[0]);
		AnnotatedElement element = new AnnotatedElement() {

			@Override
			public <A extends Annotation> @Nullable A getAnnotation(@NotNull Class<A> annotationClass) {
				return this.getDeclaredAnnotation(annotationClass);
			}

			@Override
			public <A extends Annotation> @Nullable A getDeclaredAnnotation(@NotNull Class<A> annotationClass) {
				for (Annotation annotation : annotations) {
					if (annotation.annotationType() == annotationClass) {
						return annotationClass.cast(annotation);
					}
				}
				return null;
			}

			@Override
			public @NotNull Annotation @NotNull [] getAnnotations() {
				return this.getDeclaredAnnotations();
			}

			@Override
			public @NotNull Annotation @NotNull [] getDeclaredAnnotations() {
				return annotations.clone();
			}
		};
		ReifiedType<T> type = TypeReifier.copyForAnnotations(this, true);
		type.annotationSources.add(0, element);
		return type;
	}

	/** List overload for {@link #addAnnotations(Annotation...)}. */
	public @NotNull ReifiedType<T> addAnnotations(@NotNull List<@NotNull Annotation> annotations) {
		return this.addAnnotations(ArrayFactories.ANNOTATION.collectionToArray(annotations));
	}

	//////////////////////////////// Object methods ////////////////////////////////

		@Override
	public boolean equals(Object obj) {
		return this == obj || (
			obj instanceof ReifiedType<?> that &&
			ORDERED_ANNOTATIONS_STRATEGY.equals(this, that)
		);
	}

	@Override
	public int hashCode() {
		return ORDERED_ANNOTATIONS_STRATEGY.hashCode(this);
	}

	@Override
	public @NotNull String toString() {
		return new TypeFormatter(64).annotations(true).simplify(false).append(this).toString();
	}

	/**
	common logic for {@link #appendTo(TypeFormatter)} when {@link #getClassification()}
	is {@link TypeClassification#RAW} or {@link TypeClassification#PARAMETERIZED}.
	*/
	protected void appendRaw(TypeFormatter formatter, AnnotationContainer annotations) {
		if (annotations.hasAny()) {
			formatter.append(annotations).append(' ');
		}
		Class<? super T> rawClass = this.getRawClass();
		if (rawClass == null) formatter.append("null rawClass");
		else formatter.append(rawClass);
	}

	/**
	common logic for {@link #appendTo(TypeFormatter)} when {@link #getClassification()} is
	{@link TypeClassification#WILDCARD_EXTENDS} or {@link TypeClassification#WILDCARD_SUPER}.
	*/
	protected void appendWildcard(TypeFormatter formatter, AnnotationContainer annotations, String boundName) {
		if (annotations.hasAny()) {
			formatter.append(annotations).append(' ');
		}
		formatter.append("? ").append(boundName).append(' ');
		ReifiedType<?> bound = this.getBound();
		if (bound != null) {
			//disambiguate (? super Number)[] from ? super (Number[]).
			if (bound.getClassification() == TypeClassification.ARRAY) formatter.append('(');
			formatter.append(bound);
			if (bound.getClassification() == TypeClassification.ARRAY) formatter.append(')');
		}
		else {
			formatter.append("null bound");
		}
	}

	@Override
	public void appendTo(TypeFormatter formatter) {
		if (this.classification == null) {
			formatter.append("null classification");
			return;
		}
		AnnotationContainer annotations = formatter.annotations ? this.getAnnotations() : AnnotationContainer.EMPTY_ANNOTATION_CONTAINER;
		switch (this.getClassification()) {
			case RAW -> {
				this.appendRaw(formatter, annotations);
			}
			case PARAMETERIZED -> {
				this.appendRaw(formatter, annotations);
				formatter.append('<');
				ReifiedType<?>[] parameters = this.getParameters();
				if (parameters != null) {
					formatter.append(parameters[0]);
					for (int index = 1, length = parameters.length; index < length; index++) {
						formatter.append(", ").append(parameters[index]);
					}
				}
				else {
					formatter.append("null parameters");
				}
				formatter.append('>');
			}
			case ARRAY -> {
				//bracket order is reversed for arrays.
				//@A String @B [] @C [] implies that:
				//	@A applies to the inner-most component type, String.
				//	@B applies to the outer-most component type, String[][],
				//		not the intermediate component type, String[]
				//	@C applies to the intermediate component type, String[],
				//		not the outer-most component type, String[][].
				//as such, we can't just append our component type first,
				//followed by our own annotations and brackets.
				//we need to append only the inner-most component type first,
				//followed by our annotations and brackets,
				//followed by our component type's annotations and brackets,
				//followed by its component type's annotations and brackets,
				//and so on, until the inner-most component type is reached again,
				//and we must ensure the inner-most component type is not appended more than once.
				ReifiedType<?> innerMostComponentType = this.getArrayComponentType();
				while (innerMostComponentType != null && innerMostComponentType.getClassification() == TypeClassification.ARRAY) {
					innerMostComponentType = innerMostComponentType.getArrayComponentType();
				}
				//disambiguate (? extends Number)[] from ? extends (Number[]).
				boolean wildcard = (
					innerMostComponentType != null && (
						innerMostComponentType.getClassification() == TypeClassification.WILDCARD_EXTENDS ||
						innerMostComponentType.getClassification() == TypeClassification.WILDCARD_SUPER
					)
				);
				if (wildcard) formatter.append('(');
				if (innerMostComponentType != null) formatter.append(innerMostComponentType);
				else formatter.append("null component");
				if (wildcard) formatter.append(')');

				for (
					ReifiedType<?> arrayType = this;
					arrayType != null && arrayType.getClassification() == TypeClassification.ARRAY;
					arrayType = arrayType.getArrayComponentType()
				) {
					AnnotationContainer arrayAnnotations = formatter.annotations ? arrayType.getAnnotations() : AnnotationContainer.EMPTY_ANNOTATION_CONTAINER;
					if (arrayAnnotations.hasAny()) {
						formatter.append(' ').append(arrayAnnotations).append(' ');
					}
					formatter.append("[]");
				}
			}
			case WILDCARD_EXTENDS -> {
				this.appendWildcard(formatter, annotations, "extends");
			}
			case WILDCARD_SUPER -> {
				this.appendWildcard(formatter, annotations, "super");
			}
			case UNRESOLVABLE_VARIABLE -> {
				if (EVIL_TO_STRING_INFINITE_RECURSION_BLOCKER.get().add(this)) try {
					formatter.append("unresolvable ");
					TypeVariable<?> variable = this.unresolvableVariable;
					if (variable != null) formatter.append(variable);
					else formatter.append("null variable");
					formatter.append(" extends ");
					ReifiedType<? super T> bound = this.getUpperBound();
					if (bound != null) formatter.append(bound);
					else formatter.append("null bound");
				}
				finally {
					EVIL_TO_STRING_INFINITE_RECURSION_BLOCKER.get().remove(this);
				}
				else {
					TypeVariable<?> variable = this.unresolvableVariable;
					if (variable != null) formatter.append(variable);
					else formatter.append("null variable");
				}
			}
		}
	}

	@Internal
	protected static final ThreadLocal<Set<ReifiedType<?>>> EVIL_TO_STRING_INFINITE_RECURSION_BLOCKER = ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>(2)));

	/**
	does what it says in the method name.
	useful for when you have already done runtime checks on the {@link #getRawClass()}
	or {@link #getParameters()} as necessary, but still need to suppress compile-time
	checks that this ReifiedType is being used improperly.
	*/
	@SuppressWarnings("unchecked")
	public <T2> ReifiedType<T2> uncheckedCast() {
		return (ReifiedType<T2>)(this);
	}

	public static class RecursiveHashStrategy extends NamedHashStrategy<@Nullable ReifiedType<?>> {

		public RecursiveHashStrategy(@NotNull String toString) {
			super(toString);
		}

		@Override
		public int hashCode(@Nullable ReifiedType<?> type) {
			return type == null ? 0 : this.implHashCode(type);
		}

		public int implHashCode(@NotNull ReifiedType<?> type) {
			return switch (type.getClassification()) {
				case RAW -> (
					type.requireRawClass().hashCode()
				);
				case PARAMETERIZED -> HashCommon.mix(
					type.requireRawClass().hashCode() +
					HashStrategies.orderedArrayHashCode(this, type.getParameters())
				);
				case ARRAY -> HashCommon.mix(
					this.hashCode(type.getArrayComponentType()) +
					"array".hashCode()
				);
				case WILDCARD_EXTENDS -> HashCommon.mix(
					this.hashCode(type.getUpperBound()) +
					"extends".hashCode()
				);
				case WILDCARD_SUPER -> HashCommon.mix(
					this.hashCode(type.getLowerBound()) +
					"super".hashCode()
				);
				case UNRESOLVABLE_VARIABLE -> HashCommon.mix(
					this.hashCode(type.getUpperBound()) +
					"unresolvable".hashCode()
				);
			};
		}

		@Override
		public boolean equals(@Nullable ReifiedType<?> a, @Nullable ReifiedType<?> b) {
			if (a == b) return true;
			if (a == null || b == null) return false;
			return this.implEquals(a, b);
		}

		public boolean implEquals(@NotNull ReifiedType<?> a, @NotNull ReifiedType<?> b) {
			return (
				a.getClassification() == b.getClassification() &&
				switch (a.getClassification()) {
					case RAW -> a.getRawClass() == b.getRawClass();
					case PARAMETERIZED -> a.getRawClass() == b.getRawClass() && HashStrategies.orderedArrayEquals(this, a.getParameters(), b.getParameters());
					case ARRAY -> this.equals(a.getArrayComponentType(), b.getArrayComponentType());
					case WILDCARD_EXTENDS, UNRESOLVABLE_VARIABLE -> this.equals(a.getUpperBound(), b.getUpperBound());
					case WILDCARD_SUPER -> this.equals(a.getLowerBound(), b.getLowerBound());
				}
			);
		}
	}
}