package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.verifiers.IntRangeVerifier;

/**
when applied to a subclass of {@link Number}
this annotation will ensure that the numeric value
of the relevant type is between {@link #min()} and {@link #max()}.
the numeric value will be compared according to {@link Number#longValue()}.
if {@link #minInclusive()} is set to true, the numeric value can also *equal* {@link #min()}.
otherwise, the numeric value must be strictly greater than {@link #min()}.
likewise, if {@link #maxInclusive()} is set to true, the numeric value can equal {@link #max()}.
otherwise, the numeric value must be strictly less than {@link #max()}.

see also: {@link IntRangeVerifier}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyIntRange {

	public long min() default Long.MIN_VALUE;

	public boolean minInclusive() default true;

	public long max() default Long.MAX_VALUE;

	public boolean maxInclusive() default true;
}