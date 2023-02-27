package builderb0y.autocodec.annotations;

import java.lang.annotation.*;

/**
if type owner is annotated with A and @Mirror(A.class),
then owner acts as a "mirror" for A,
and anything annotated with owner will be implicitly annotated with A too.
if A is repeatable and multiple instances of it are applied to owner,
all of them will be mirrored.

Mirror is transitive, in the sense that if A mirrors B, and B mirrors C, then A mirrors C.
*/
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mirror {

	/** the list of annotation classes to be mirrored. */
	public abstract Class<? extends Annotation>[] value();
}