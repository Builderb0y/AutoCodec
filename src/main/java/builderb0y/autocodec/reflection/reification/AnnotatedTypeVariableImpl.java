package builderb0y.autocodec.reflection.reification;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

@Internal
public class AnnotatedTypeVariableImpl implements AnnotatedTypeVariable {

	public final TypeVariable<?> typeVariable;

	public AnnotatedTypeVariableImpl(TypeVariable<?> typeVariable) {
		this.typeVariable = typeVariable;
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		return this.typeVariable.getAnnotatedBounds();
	}

	@Override
	public AnnotatedType getAnnotatedOwnerType() {
		return null;
	}

	@Override
	public Type getType() {
		return this.typeVariable;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	@SuppressWarnings("ZeroLengthArrayAllocation")
	public Annotation[] getAnnotations() {
		return new Annotation[0];
	}

	@Override
	@SuppressWarnings("ZeroLengthArrayAllocation")
	public Annotation[] getDeclaredAnnotations() {
		return new Annotation[0];
	}

	@Override
	public boolean isAnnotationPresent(@NotNull Class<? extends Annotation> annotationClass) {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return (T[])(Array.newInstance(annotationClass, 0));
	}

	@Override
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return (T[])(Array.newInstance(annotationClass, 0));
	}

	public static final int HASH_XOR = Objects.hash() ^ Objects.hash((Object)(null));

	@Override
	public int hashCode() {
		return this.typeVariable.hashCode() ^ HASH_XOR;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || (
			obj instanceof AnnotatedTypeVariable that &&
			this.getType().equals(that.getType()) &&
			that.getAnnotations().length == 0 &&
			that.getAnnotatedOwnerType() == null
		);
	}

	@Override
	public String toString() {
		return this.typeVariable.toString();
	}
}