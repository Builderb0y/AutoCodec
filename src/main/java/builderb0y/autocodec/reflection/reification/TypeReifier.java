package builderb0y.autocodec.reflection.reification;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.util.AutoCodecUtil;

/** internal class for converting {@link AnnotatedType}'s to {@link ReifiedType}'s. */
@Internal
public class TypeReifier {

	public final @NotNull Map<@NotNull TypeVariable<Class<?>>, @NotNull ReifiedType<?>> variableLookup;

	public final @NotNull Map<@NotNull AnnotatedType, @NotNull ReifiedType<?>> seen;

	/**
	if true, when an {@link AnnotatedTypeVariable} is encountered,
	the result is a ReifiedType whose {@link ReifiedType#getClassification()}
	is {@link TypeClassification#UNRESOLVABLE_VARIABLE}.

	if false, the above case will instead throw a {@link TypeReificationException}.

	this field is set to false when reifying a type from scratch
	(for example, {@link ReifiedType#ReifiedType()} or {@link ReifiedType#from(AnnotatedType)}),
	and true when resolving against an existing type
	(for example, {@link ReifiedType#resolveDeclaration(AnnotatedType)}).
	*/
	public final boolean allowUnresolvableVariables;

	public TypeReifier(
		@Nullable Map<@NotNull TypeVariable<Class<?>>, @NotNull ReifiedType<?>> variableLookup,
		boolean allowUnresolvableVariables
	) {
		this.variableLookup = variableLookup != null ? variableLookup : Collections.emptyMap();
		this.seen = new HashMap<>(32);
		this.allowUnresolvableVariables = allowUnresolvableVariables;
	}

	public static <T> @NotNull ReifiedType<T> copyForAnnotations(@NotNull ReifiedType<T> from, boolean keepSources) {
		ReifiedType<T> to = ReifiedType.blank();
		if (keepSources) to.annotationSources.addAll(from.annotationSources);
		//skip annotations, declaredAnnotations.
		to.classification       = from.classification;
		to.rawClass             = from.rawClass;
		to.inheritanceLookup    = from.inheritanceLookup;
		to.variableLookup       = from.variableLookup;
		to.inheritanceHierarchy = from.inheritanceHierarchy;
		to.superClassType       = from.superClassType;
		to.superInterfaceTypes  = from.superInterfaceTypes;
		to.sharedType           = from.sharedType;
		to.parameters           = from.parameters;
		to.unresolvableVariable = from.unresolvableVariable;
		return to;
	}

	public static <T> @NotNull ReifiedType<T> maybeCopyForBoxing(@NotNull ReifiedType<T> from, @NotNull Function<@NotNull Class<T>, @NotNull Class<T>> mapper) {
		@SuppressWarnings("unchecked")
		Class<T> rawFrom = (Class<T>)(from.getRawClass());
		if (rawFrom == null) return from;
		Class<T> rawTo = mapper.apply(rawFrom);
		if (rawTo == rawFrom) return from;

		ReifiedType<T> to = ReifiedType.blank();
		to.classification       = from.classification;
		to.rawClass             = rawTo;
		to.annotationSources    = from.annotationSources;
		to.annotations          = from.annotations;
		to.variableLookup       = from.variableLookup;
		//skip sharedType, parameters, and unresolvableVariable as
		//neither primitive types nor box types should have them.
		//skip inheritanceLookup, inheritanceHierarchy, superClassType, and superInterfaceTypes,
		//as the raw type (and thus inheritance hierarchy) has changed.
		return to;
	}

	public @NotNull ReifiedType<?> unresolvable(@NotNull TypeVariable<?> variable, @NotNull AnnotatedType annotatedBound) {
		ReifiedType<?> reifiedBound = this.reify(annotatedBound);
		ReifiedType<?> type = ReifiedType.blank();
		type.classification = TypeClassification.UNRESOLVABLE_VARIABLE;
		type.sharedType = reifiedBound;
		type.unresolvableVariable = variable;
		return type;
	}

	public @NotNull ReifiedType<?> reify(@NotNull AnnotatedType type) {
		ReifiedType<?> seen = this.seen.get(type);
		if (seen != null) {
			return seen;
		}
		if (type instanceof AnnotatedTypeVariable variable) {
			TypeVariable<?> typeVariable = (TypeVariable<?>)(variable.getType());
			ReifiedType<?> resolution = this.variableLookup.get(typeVariable);
			if (resolution == null) {
				if (this.allowUnresolvableVariables) {
					resolution = this.unresolvable(typeVariable, variable.getAnnotatedBounds()[0]);
				}
				else {
					throw new TypeReificationException("Missing generic information to fully resolve " + variable);
				}
			}
			resolution = copyForAnnotations(resolution, true);
			//todo: try to understand why moving typeVariable to the end of the list works in this specific case.
			resolution.annotationSources.remove(typeVariable);
			resolution.annotationSources.add(type);
			resolution.annotationSources.add(typeVariable);
			return resolution;
		}
		ReifiedType<?> result = ReifiedType.blank();
		this.seen.put(type, result);
		result.annotationSources.add(type);
		if (type instanceof AnnotatedParameterizedType parameterized) {
			this.populateParameterized(parameterized, result);
		}
		else if (type instanceof AnnotatedArrayType array) {
			this.populateArray(array, result);
		}
		else if (type instanceof AnnotatedWildcardType wildcard) {
			this.populateWildcard(wildcard, result);
		}
		else if (type.getType() instanceof Class<?> clazz) {
			this.populateRaw(type, clazz, result);
		}
		else {
			throw new TypeReificationException("Unknown type: " + type);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void populateParameterized(
		@NotNull AnnotatedParameterizedType type,
		@NotNull ReifiedType<?> result
	) {
		Class<?> raw = (
			(Class<?>)(
				(
					(ParameterizedType)(
						type.getType()
					)
				)
				.getRawType()
			)
		);
		result.rawClass = (Class)(raw);
		result.annotationSources.add(raw);
		TypeVariable<Class<?>>[] from = (TypeVariable[])(raw.getTypeParameters());
		AnnotatedType[] to = type.getAnnotatedActualTypeArguments();
		int length = from.length;
		if (to.length != length) {
			throw new AssertionError("Mismatched type variable counts: " + Arrays.toString(from) + " -> " + Arrays.toString(to));
		}
		//note: length CAN be 0 if the underlying type is
		//parameterized by necessity to support an owner type.
		if (length == 0) {
			result.classification = TypeClassification.RAW;
		}
		else {
			result.classification = TypeClassification.PARAMETERIZED;
			Map<TypeVariable<Class<?>>, ReifiedType<?>> newLookup = new HashMap<>(length);
			ReifiedType<?>[] resultParameters = new ReifiedType<?>[length];
			for (int index = 0; index < length; index++) {
				ReifiedType<?> resultParameter = this.reify(to[index]);
				if (resultParameter.isPrimitive()) {
					throw new TypeReificationException("Primitive type parameter: " + resultParameter);
				}
				resultParameter = copyForAnnotations(resultParameter, true);
				resultParameter.annotationSources.add(from[index]);
				resultParameters[index] = resultParameter;
				newLookup.put(from[index], resultParameter);
			}
			result.parameters = resultParameters;
			result.variableLookup = newLookup;
		}
		this.populateOwner(type, result, raw);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void populateArray(
		@NotNull AnnotatedArrayType array,
		@NotNull ReifiedType<?> result
	) {
		result.classification = TypeClassification.ARRAY;
		result.sharedType = this.reify(array.getAnnotatedGenericComponentType());
		Class<?> componentClass = result.sharedType.getRawClass();
		if (componentClass != null) result.rawClass = (Class)(componentClass.arrayType());
	}

	public void populateWildcard(
		@NotNull AnnotatedWildcardType wildcard,
		@NotNull ReifiedType<?> result
	) {
		AnnotatedType[] lowerBounds = wildcard.getAnnotatedLowerBounds();
		if (lowerBounds.length != 0) {
			if (lowerBounds.length != 1) {
				throw new TypeReificationException("Multiple lower bounds.");
			}
			result.classification = TypeClassification.WILDCARD_SUPER;
			result.sharedType = this.reify(lowerBounds[0]);
		}
		else {
			AnnotatedType[] upperBounds = wildcard.getAnnotatedUpperBounds();
			if (upperBounds.length != 1) {
				throw new TypeReificationException(upperBounds.length == 0 ? "No wildcard bounds." : "Multiple upper bounds.");
			}
			result.classification = TypeClassification.WILDCARD_EXTENDS;
			result.sharedType = this.reify(upperBounds[0]);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void populateRaw(
		@NotNull AnnotatedType annotated,
		@NotNull Class<?> clazz,
		@NotNull ReifiedType<?> result
	) {
		if (clazz.isArray()) {
			throw new TypeReificationException("Arrays should use AnnotatedArrayType, not AnnotatedType for " + clazz);
		}
		result.rawClass = (Class)(clazz);
		result.annotationSources.add(clazz);
		TypeVariable<Class<?>>[] parameters = (TypeVariable[])(clazz.getTypeParameters());
		int length = parameters.length;
		if (length == 0) {
			result.classification = TypeClassification.RAW;
		}
		else {
			if (!this.allowUnresolvableVariables) {
				throw new TypeReificationException("Missing generic information to fully resolve " + Arrays.toString(parameters) + " on " + clazz);
			}
			result.classification = TypeClassification.PARAMETERIZED;
			Map<TypeVariable<Class<?>>, ReifiedType<?>> newLookup = new HashMap<>(length);
			ReifiedType<?>[] resultParameters = new ReifiedType<?>[length];
			for (int index = 0; index < length; index++) {
				ReifiedType<?> resultParameter = this.unresolvable(parameters[index], parameters[index].getAnnotatedBounds()[0]);
				if (resultParameter.isPrimitive()) {
					throw new TypeReificationException("Primitive type parameter: " + resultParameter);
				}
				resultParameter = copyForAnnotations(resultParameter, true);
				resultParameter.annotationSources.add(parameters[index]);
				resultParameters[index] = resultParameter;
				newLookup.put(parameters[index], resultParameter);
			}
			result.parameters = resultParameters;
			result.variableLookup = newLookup;
		}
		this.populateOwner(annotated, result, clazz);
	}

	public void populateOwner(
		@NotNull AnnotatedType type,
		@NotNull ReifiedType<?> reified,
		@NotNull Class<?> rawClass
	) {
		if (AutoCodecUtil.isNonStaticInnerClass(rawClass)) {
			AnnotatedType owner = type.getAnnotatedOwnerType();
			if (owner != null) {
				reified.sharedType = this.reify(owner);
			}
		}
	}

	public static <T> @NotNull ReifiedType<T> create(@NotNull Class<? super T> rawClass, @Nullable ReifiedType<?> owner, @NotNull ReifiedType<?> @Nullable ... parameters) {
		if (rawClass.isArray()) {
			if (owner != null) {
				throw new TypeReificationException(rawClass + " should not have an owner.");
			}
			if (parameters != null && parameters.length != 0) {
				throw new TypeReificationException(rawClass + " should not have type parameters.");
			}
			ReifiedType<T> result = ReifiedType.blank();
			result.classification = TypeClassification.ARRAY;
			result.rawClass = rawClass;
			result.sharedType = create(rawClass.getComponentType(), null, parameters);
			return result;
		}
		else {
			if (owner != null) {
				if (!AutoCodecUtil.isNonStaticInnerClass(rawClass)) {
					throw new TypeReificationException(rawClass + " should not have an owner, but one was given anyway: " + owner);
				}
				if (rawClass.getEnclosingClass() != owner.getRawClass()) {
					throw new TypeReificationException(rawClass + "'s owner is " + rawClass.getEnclosingClass() + ", but the provided owner was " + owner);
				}
			}
			else {
				if (AutoCodecUtil.isNonStaticInnerClass(rawClass)) {
					owner = create(rawClass.getEnclosingClass(), null, (ReifiedType<?>[])(null));
				}
			}
			@SuppressWarnings({ "unchecked", "rawtypes" })
			TypeVariable<Class<?>>[] from = (TypeVariable[])(rawClass.getTypeParameters());
			int parameterCount = from.length;
			if (parameterCount == 0) {
				if (parameters != null && parameters.length != 0) {
					throw new TypeReificationException(rawClass + " should not have type parameters.");
				}
				ReifiedType<T> result = ReifiedType.blank();
				result.classification = TypeClassification.RAW;
				result.rawClass = rawClass;
				result.annotationSources.add(rawClass);
				result.sharedType = owner;
				return result;
			}
			else {
				if (parameters != null) {
					if (parameters.length != parameterCount) {
						throw new TypeReificationException(rawClass + " has " + parameterCount + " parameters, but " + parameters.length + " were given.");
					}
				}
				else {
					parameters = ReifiedType.ARRAY_FACTORY.apply(parameterCount);
					Arrays.fill(parameters, ReifiedType.WILDCARD);
				}
				ReifiedType<?>[] to = new ReifiedType<?>[parameterCount];
				Map<TypeVariable<Class<?>>, ReifiedType<?>> newLookup = new HashMap<>(parameterCount);
				for (int index = 0; index < parameterCount; index++) {
					ReifiedType<?> parameter = TypeReifier.copyForAnnotations(parameters[index].boxed(), true);
					parameter.annotationSources.remove(from[index]);
					parameter.annotationSources.add(from[index]);
					to[index] = parameter;
					newLookup.put(from[index], parameter);
				}
				ReifiedType<T> result = ReifiedType.blank();
				result.classification = TypeClassification.PARAMETERIZED;
				result.rawClass = rawClass;
				result.parameters = to;
				result.variableLookup = newLookup;
				result.sharedType = owner;
				return result;
			}
		}
	}
}