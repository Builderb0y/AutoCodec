package builderb0y.autocodec.reflection;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.AddPseudoField;
import builderb0y.autocodec.util.ArrayFactories;
import builderb0y.autocodec.util.ObjectArrayFactory;

/**
a holder for the information declared or derived from a {@link AddPseudoField} annotation.

analogous to {@link Field} or {@link RecordComponent}.
*/
public class PseudoField implements AnnotatedElement, Member {

	public static final @NotNull ObjectArrayFactory<PseudoField> ARRAY_FACTORY = ArrayFactories.PSEUDO_FIELD;

	public static final @NotNull ClassValue<@NotNull PseudoField @NotNull []> CACHE = new ClassValue<>() {

		@Override
		public PseudoField[] computeValue(Class<?> type) {
			return parse(type);
		}
	};

	/**
	the {@link Class} that this pseudo-field was declared in,
	which is another way of saying the class that the
	{@link AddPseudoField} annotation was applied to.

	analogous to {@link Field#getDeclaringClass()}
	or {@link RecordComponent#getDeclaringRecord()}.
	*/
	public final @NotNull Class<?> declaringClass;

	/**
	the {@link AddPseudoField#name()} of this pseudo-field.
	this name is guaranteed to be interned {@link String#intern()}.

	analogous to {@link Field#getName()}
	or {@link RecordComponent#getName()}.
	*/
	public final @NotNull String name;

	/**
	the {@link Method} which can be used to get the value of this pseudo-field.
	the getter method will have all the properties listed
	in the documentation for {@link AddPseudoField#getter()}.
	the value of this pseudo-field can be obtained via: {@code
		Object obj = ...;
		assert pseudoFieldInfo.declaringClass.isInstance(obj);

		Object value = pseudoFieldInfo.getter.invoke(obj);
	}

	analogous to {@link RecordComponent#getAccessor()}.
	*/
	public final @NotNull Method getter;

	/**
	if the annotation which declared this pseudo-field specifies a {@link AddPseudoField#setter()},
	returns the {@link Method} which can be used to set the value of this pseudo-field.
	the setter method will have all the properties listed
	in the documentation for {@link AddPseudoField#setter()}.
	the value of this pseudo-field can be set via: {@code
		Object obj = ...;
		Object value = ...;
		assert pseudoFieldInfo.getDeclaringClass().isInstance(obj);
		assert pseudoFieldInfo.rawType.isInstance(value);

		pseudoFieldInfo.getSetter().invoke(obj, value);
	}
	if the annotation which declared this pseudo-field
	does NOT specify a setter, this method returns null.
	*/
	public final @Nullable Method setter;

	/**
	a {@link Class} which represents the type of this pseudo-field.
	this value is obtained from the {@link #getter}'s {@link Method#getReturnType()}.

	analogous to {@link Field#getType()}
	or {@link RecordComponent#getType()}.
	*/
	public final @NotNull Class<?> rawType;

	/**
	a {@link Type} which represents the type of this pseudo-field.
	this value is obtained from the {@link #getter}'s {@link Method#getGenericReturnType()}.

	analogous to {@link Field#getGenericType()}
	or {@link RecordComponent#getGenericType()}.
	*/
	public final @NotNull Type genericType;

	/**
	a {@link Type} which represents the type of this pseudo-field.
	this value is obtained from the {@link #getter}'s {@link Method#getAnnotatedReturnType()}.

	analogous to {@link Field#getGenericType()}
	or {@link RecordComponent#getGenericType()}.
	*/
	public final @NotNull AnnotatedType annotatedType;

	public PseudoField(
		@NotNull  Class<?> declaringClass,
		@NotNull  String   name,
		@NotNull  Method   getter,
		@Nullable Method   setter
	) {
		this.declaringClass = declaringClass;
		this.name           = name.intern();
		this.getter         = getter;
		this.setter         = setter;
		this.rawType        = getter.getReturnType();
		this.genericType    = getter.getGenericReturnType();
		this.annotatedType  = getter.getAnnotatedReturnType();
	}

	/**
	returns an array containing all the pseudo-fields declared
	on (owner), in the order in which they are declared.
	the returned array is not shared, and can be safely modified by the caller.
	*/
	public static @NotNull PseudoField @NotNull [] getPseudoFields(@NotNull Class<?> owner) {
		return getPseudoFieldsNoClone(owner).clone();
	}

	/**
	returns an array containing all the pseudo-fields declared
	on (owner), in the order in which they are declared.
	the returned array is shared, and MUST NOT BE MODIFIED BY THE CALLER!
	*/
	public static @NotNull PseudoField @NotNull [] getPseudoFieldsNoClone(@NotNull Class<?> owner) {
		return CACHE.get(owner);
	}

	/**
	parses all the {@link AddPseudoField} annotations on (owner),
	and converts them into PseudoField objects.
	*/
	public static @NotNull PseudoField @NotNull [] parse(@NotNull Class<?> owner) {
		AddPseudoField[] annotations = owner.getDeclaredAnnotationsByType(AddPseudoField.class);
		int length = annotations.length;
		if (length == 0) return ARRAY_FACTORY.empty();
		Method[] methods = owner.getDeclaredMethods();
		int methodCount = preprocessMethods(methods);
		PseudoField[] fields = new PseudoField[length];
		for (int index = 0; index < length; index++) {
			fields[index] = parse(owner, methods, methodCount, annotations[index]);
		}
		return fields;
	}

	/**
	removes methods from the provided array which are
	guaranteed to not be useful as a getter or setter.
	returns the number of methods remaining.
	after this method returns, callers should pretend that
	the array's length is equal to this method's return value.
	the array's contents at indexes greater than or
	equal to this method's return value are undefined.
	*/
	public static int preprocessMethods(@NotNull Method @NotNull [] methods) {
		int writeIndex = 0;
		for (int readIndex = 0, length = methods.length; readIndex < length; readIndex++) {
			Method method = methods[readIndex];
			if (
				!Modifier.isStatic(method.getModifiers()) &&
				!method.isBridge() &&
				switch (method.getParameterCount()) {
					case 0 -> method.getReturnType() != void.class; //getter
					case 1 -> method.getReturnType() == void.class; //setter
					default -> false; //not a getter or setter.
				}
			) {
				methods[writeIndex++] = method;
			}
		}
		return writeIndex;
	}

	/**
	parses a single {@link AddPseudoField} annotation which was declared on (owner),
	and converts it to a PseudoField object.

	@param methods equal to owner.{@link Class#getDeclaredMethods()},
	after being passed into {@link #preprocessMethods(Method[])}.
	passed in as an extra parameter to avoid more cloning than necessary.

	@param methodCount the number of methods in
	the (methods) parameter which are to be used.
	*/
	public static @NotNull PseudoField parse(
		@NotNull Class<?> owner,
		@NotNull Method @NotNull [] methods,
		int methodCount,
		@NotNull AddPseudoField annotation
	) {
		String value = annotation.value().intern();
		String name = annotation.name().intern();
		if (name.isEmpty()) name = value;
		if (name.isEmpty()) throw new AnnotationFormatError("Must provide value() or name() for " + annotation + " on " + owner);
		String getterName = annotation.getter().intern();
		if (getterName.isEmpty()) getterName = value;
		if (getterName.isEmpty()) throw new AnnotationFormatError("Must provide value() or getterName() for " + annotation + " on " + owner);
		Method getter = null;
		for (int methodIndex = 0; methodIndex < methodCount; methodIndex++) {
			Method method = methods[methodIndex];
			if (
				//static and bridge methods already removed by {@link #preprocessMethods(Method[])}.
				method.getName() == getterName && //Method.name is guaranteed to be interned.
				method.getParameterCount() == 0
				//preprocessMethods() ensures that methods with no parameters return non-void.
			) {
				if (getter == null) {
					getter = method;
				}
				else {
					throw new AnnotationFormatError("Multiple candidate getter methods found for " + annotation + " on " + owner);
				}
			}
		}
		if (getter == null) {
			throw new AnnotationFormatError("Could not find getter for " + annotation + " on " + owner);
		}
		Class<?> type = getter.getReturnType();
		String setterName = annotation.setter().intern();
		String setterSearch = setterName.isEmpty() ? value : setterName;
		Method setter = null;
		if (!setterSearch.isEmpty()) {
			for (int methodIndex = 0; methodIndex < methodCount; methodIndex++) {
				Method method = methods[methodIndex];
				if (
					//static and bridge methods already removed by {@link #preprocessMethods(Method[])}.
					method.getName() == setterSearch && //Method.name is guaranteed to be interned.
					method.getParameterCount() == 1 &&
					method.getParameterTypes()[0] == type
					//preprocessMethods() ensures that methods with 1 parameter return void.
				) {
					if (setter == null) {
						setter = method;
					}
					else {
						throw new AnnotationFormatError("Multiple candidate setter methods found for " + annotation + " on " + owner);
					}
				}
			}
			if (!setterName.isEmpty() && setter == null) {
				throw new AnnotationFormatError("Could not find setter for " + annotation + " on " + owner);
			}
		}
		return new PseudoField(owner, name, getter, setter);
	}

	@Override
	public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) {
		return this.getter.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return this.getter.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return this.getter.getDeclaredAnnotations();
	}

	@Override
	public boolean isAnnotationPresent(@NotNull Class<? extends Annotation> annotationClass) {
		return this.getter.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return this.getter.getAnnotationsByType(annotationClass);
	}

	@Override
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
		return this.getter.getDeclaredAnnotation(annotationClass);
	}

	@Override
	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return this.getter.getDeclaredAnnotationsByType(annotationClass);
	}

	@Override
	public @NotNull Class<?> getDeclaringClass() {
		return this.declaringClass;
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	@SuppressWarnings("VariableNotUsedInsideIf")
	public int getModifiers() {
		return this.setter != null ? Modifier.PUBLIC : (Modifier.PUBLIC | Modifier.FINAL);
	}

	@Override
	public boolean isSynthetic() {
		return false;
	}

	@Override
	public int hashCode() {
		return this.declaringClass.hashCode() ^ this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || (
			obj instanceof PseudoField that &&
			this.declaringClass == that.declaringClass &&
			this.name == that.name //name is interned, so == is ok.
		);
	}

	@SuppressWarnings("VariableNotUsedInsideIf")
	public String modifierString() {
		return this.setter != null ? "public" : "public final";
	}

	@Override
	public String toString() {
		return this.modifierString() + ' ' + this.rawType.getTypeName() + ' ' + this.declaringClass.getTypeName() + '.' + this.name;
	}

	public String toGenericString() {
		return this.modifierString() + ' ' + this.genericType.getTypeName() + ' ' + this.declaringClass.getTypeName() + '.' + this.name;
	}

	public String toAnnotatedString() {
		return this.modifierString() + ' ' + this.annotatedType + ' ' + this.declaringClass.getTypeName() + '.' + this.name;
	}
}