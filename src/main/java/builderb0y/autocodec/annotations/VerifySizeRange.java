package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

import builderb0y.autocodec.verifiers.SizeRangeVerifier;

/**
this annotation ensures that the "size" of an object
is between {@link #min()} and {@link #max()}.
if {@link #minInclusive()} is set to true,
the size can also *equal* {@link #min()}.
otherwise, the size must be strictly greater than {@link #min()}.
likewise, if {@link #maxInclusive()} is set to true,
then the size can equal {@link #max()}.
otherwise, the size must be strictly less than {@link #max()}.

the "size" of an object depends on what type
of object this annotation is applied to.
	when applied to {@link CharSequence} or a subclass of {@link CharSequence},
	the size of the object is {@link CharSequence#length()}.

	when applied to {@link Collection} or a subclass of {@link Collection},
	the size of the object is {@link Collection#size()}.

	when applied to {@link Map} or a subclass of {@link Map},
	the size of the object is {@link Map#size()}.

	when applied to an array, the size of the object is the array's length.
	note that the annotation should be applied to the array itself, not its component type.
	in other words, @VerifySizeRange(...) int[] is incorrect.
	it should be int @VerifySizeRange(...) [] instead.

in all cases, this annotation can be applied to nested sized types.
for example, @VerifySizeRange(...) List<@VerifySizeRange(...) String @VerifySizeRange []>
is a perfectly valid declaration.
an uncommon bit of trivia for multidimensional arrays specifically is
that annotation orders are reversed for all but the root component type.
meaning that if you had @A int @B [] @C [],
then @A applies to type int as expected, but @B applies to type int[][],
and @C applies to type int[] instead of the other way around.
this fact should be kept in mind when annotating multidimensional arrays
with @VerifySizeRange or other annotations which interact with arrays.

see also: {@link SizeRangeVerifier}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifySizeRange {

	public int min() default 0;

	public boolean minInclusive() default true;

	public int max() default Integer.MAX_VALUE;

	public boolean maxInclusive() default true;
}