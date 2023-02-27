package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.verifiers.FloatRangeVerifier;

/**
when applied to a subclass of {@link Number}
this annotation will ensure that the numeric value
of the relevant type is between {@link #min()} and {@link #max()}.
the numeric value will be compared according to {@link Number#doubleValue()}.
if {@link #minInclusive()} is set to true, the numeric value can also *equal* {@link #min()}.
otherwise, the numeric value must be strictly greater than {@link #min()}.
likewise, if {@link #maxInclusive()} is set to true, the numeric value can equal {@link #max()}.
otherwise, the numeric value must be strictly less than {@link #max()}.
if {@link #allowNaN()} is set to true, the number could also be NaN {@link Double#isNaN(double)}.
otherwise, the value must be strictly non-NaN.

see also: {@link FloatRangeVerifier}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyFloatRange {

	public double min() default Double.NEGATIVE_INFINITY;

	public boolean minInclusive() default true;

	public double max() default Double.POSITIVE_INFINITY;

	public boolean maxInclusive() default true;

	public boolean allowNaN() default false;
}