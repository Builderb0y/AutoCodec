package builderb0y.autocodec.annotations;

import java.lang.annotation.*;

import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
if type owner is annotated with A and @Mirror(A.class),
then owner acts as a "mirror" for A,
and anything annotated with owner will be implicitly annotated with A too
according to {@link ReifiedType#getAnnotations()}.
this can be useful if you want to annotate many things with the same annotations.
just remember to annotate the owner with @Retention(RetentionPolicy.RUNTIME).
if A is repeatable and multiple instances of it are applied to owner,
all of them will be mirrored.

Mirror is transitive, in the sense that if A mirrors B, and B mirrors C, then A mirrors C.

example usage: {@code
	@MultiLine
	@VerifyNullable
	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface A {}
	...
	ReifiedType<String> type = new ReifiedType<@A String>() {};
	assert type.getAnnotations().has(A.class)
	assert type.getAnnotations().has(MultiLine.class)
	assert type.getAnnotations().has(VerifyNullable.class)
}
*/
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mirror {

	/** the list of annotation classes to be mirrored. */
	public abstract Class<? extends Annotation>[] value();
}