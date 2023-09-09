package builderb0y.autocodec.verifiers;

import java.util.Set;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.VerifyFloatRange;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;

public class FloatRangeVerifier implements AutoVerifier<Number> {

	public final double min, max;
	public final boolean minInclusive, maxInclusive;
	public final boolean allowNaN;

	public FloatRangeVerifier(double min, double max, boolean minInclusive, boolean maxInclusive, boolean allowNaN) {
		this.min          = min;
		this.max          = max;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
		this.allowNaN     = allowNaN;
	}

	public FloatRangeVerifier(VerifyFloatRange annotation) {
		this(annotation.min(), annotation.max(), annotation.minInclusive(), annotation.maxInclusive(), annotation.allowNaN());
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, Number> context) throws VerifyException {
		if (context.object == null) return; //assume NotNullVerifier checks for this.
		double value = context.object.doubleValue();
		if (Double.isNaN(value)) {
			if (this.allowNaN) {
				return;
			}
			else {
				throw new VerifyException(() -> context.pathToStringBuilder().append(" cannot be NaN").toString());
			}
		}
		else if (
			(
				this.minInclusive
				? (Double.compare(value, this.min) >= 0)
				: (Double.compare(value, this.min) >  0)
			)
			&& (
				this.maxInclusive
				? (Double.compare(value, this.max) <= 0)
				: (Double.compare(value, this.max) <  0)
			)
		) {
			return;
		}
		else {
			throw new VerifyException(() -> {
				StringBuilder message = context.pathToStringBuilder();
				boolean haveMin = this.min != Double.NEGATIVE_INFINITY || !this.minInclusive;
				boolean haveMax = this.max != Double.POSITIVE_INFINITY || !this.maxInclusive;
				assert haveMin || haveMax : "No bounds, but still failed?";
				message.append(" must be ");
				if (haveMin) {
					message.append("greater than ");
					if (this.minInclusive) message.append("or equal to ");
					message.append(this.min);
				}
				if (haveMin && haveMax) {
					message.append(" and ");
				}
				if (haveMax) {
					message.append("less than ");
					if (this.maxInclusive) message.append("or equal to ");
					message.append(this.max);
				}
				return message.toString();
			});
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + (this.minInclusive ? '[' : '(') + this.min + ", " + (this.maxInclusive ? ']' : ')') + (this.allowNaN ? " + NaN" : "");
	}

	public static class Factory extends NamedVerifierFactory {

		public static final Set<Class<?>> PRIMITIVE_NUMBER_CLASSES = Set.of(
			byte.class,
			short.class,
			int.class,
			long.class,
			float.class,
			double.class
		);
		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			Class<? super T_HandledType> clazz = context.type.getUpperBoundOrSelf().getRawClass();
			if (clazz != null && (PRIMITIVE_NUMBER_CLASSES.contains(clazz) || Number.class.isAssignableFrom(clazz))) {
				VerifyFloatRange range = context.type.getAnnotations().getFirst(VerifyFloatRange.class);
				if (range != null) {
					return new FloatRangeVerifier(range);
				}
			}
			return null;
		}
	}
}