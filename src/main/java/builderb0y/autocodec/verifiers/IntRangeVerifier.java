package builderb0y.autocodec.verifiers;

import java.util.Set;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.VerifyIntRange;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;

public class IntRangeVerifier implements AutoVerifier<Number> {

	public final long min, max;
	public final boolean minInclusive, maxInclusive;

	public IntRangeVerifier(long min, long max, boolean minInclusive, boolean maxInclusive) {
		this.min = min;
		this.max = max;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
	}

	public IntRangeVerifier(VerifyIntRange annotation) {
		this(annotation.min(), annotation.max(), annotation.minInclusive(), annotation.maxInclusive());
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, Number> context) throws VerifyException {
		if (context.object == null) return; //assume NotNullVerifier checks for this.
		long value = context.object.longValue();
		if (
			(this.minInclusive ? (value >= this.min) : (value > this.min)) &&
			(this.maxInclusive ? (value <= this.max) : (value < this.max))
		) {
			return;
		}
		else {
			StringBuilder message = new StringBuilder(128);
			context.appendPathTo(message);
			boolean haveMin = this.min != Long.MIN_VALUE || !this.minInclusive;
			boolean haveMax = this.max != Long.MAX_VALUE || !this.maxInclusive;
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
			throw new VerifyException(message.toString());
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + (this.minInclusive ? '[' : '(') + this.min + ", " + (this.maxInclusive ? ']' : ')');
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
		public @Nullable <T_HandledType> AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			Class<? super T_HandledType> clazz = context.type.getUpperBoundOrSelf().getRawClass();
			if (clazz != null && (PRIMITIVE_NUMBER_CLASSES.contains(clazz) || Number.class.isAssignableFrom(clazz))) {
				VerifyIntRange range = context.type.getAnnotations().getFirst(VerifyIntRange.class);
				if (range != null) {
					return new IntRangeVerifier(range);
				}
			}
			return null;
		}
	}
}