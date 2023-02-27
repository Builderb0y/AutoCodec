package builderb0y.autocodec.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.AnnotationContainer;
import builderb0y.autocodec.reflection.PseudoField;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
{@link ObjectArrayFactory}'s for classes that I can't modify,
but want array factories for anyway.
plus a couple other classes that I can modify
that are related to ones that I can't modify.
*/
public class ArrayFactories {

	public static final @NotNull ObjectArrayFactory<Annotation> ANNOTATION = new ObjectArrayFactory<>(Annotation.class);
	public static final @NotNull ObjectArrayFactory<AnnotatedElement> ANNOTATED_ELEMENT = new ObjectArrayFactory<>(AnnotatedElement.class);
	public static final @NotNull ObjectArrayFactory<AnnotationContainer> ANNOTATION_CONTAINER = new ObjectArrayFactory<>(AnnotationContainer.class);

	public static final @NotNull ObjectArrayFactory<Type> TYPE = new ObjectArrayFactory<>(Type.class);
	public static final @NotNull ObjectArrayFactory<Class<?>> CLASS = new ObjectArrayFactory<>(Class.class).generic();
	public static final @NotNull ObjectArrayFactory<AnnotatedType> ANNOTATED_TYPE = new ObjectArrayFactory<>(AnnotatedType.class);
	public static final @NotNull ObjectArrayFactory<ReifiedType<?>> REIFIED_TYPE = new ObjectArrayFactory<>(ReifiedType.class).generic();

	public static final @NotNull ObjectArrayFactory<Field> FIELD = new ObjectArrayFactory<>(Field.class);
	public static final @NotNull ObjectArrayFactory<RecordComponent> RECORD_COMPONENT = new ObjectArrayFactory<>(RecordComponent.class);
	public static final @NotNull ObjectArrayFactory<PseudoField> PSEUDO_FIELD = new ObjectArrayFactory<>(PseudoField.class);
	public static final @NotNull ObjectArrayFactory<Method> METHOD = new ObjectArrayFactory<>(Method.class);
	public static final @NotNull ObjectArrayFactory<Constructor<?>> CONSTRUCTOR = new ObjectArrayFactory<>(Constructor.class).generic();
}