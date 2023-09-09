package builderb0y.autocodec.verifiers;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.VerifySizeRange;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.reflection.reification.TypeClassification;

public class SizeRangeVerifier<T_Collection> implements AutoVerifier<T_Collection> {

	public final int min, max;
	public final boolean minInclusive, maxInclusive;
	public final @NotNull SizeGetter<? super T_Collection> sizeGetter;

	public SizeRangeVerifier(
		int min,
		int max,
		boolean minInclusive,
		boolean maxInclusive,
		@NotNull SizeGetter<? super T_Collection> sizeGetter
	) {
		this.min = min;
		this.max = max;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
		this.sizeGetter   = sizeGetter;
	}

	public SizeRangeVerifier(VerifySizeRange annotation, @NotNull SizeGetter<? super T_Collection> sizeGetter) {
		this(annotation.min(), annotation.max(), annotation.minInclusive(), annotation.maxInclusive(), sizeGetter);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, T_Collection> context) throws VerifyException {
		if (context.object == null) return; //assume NotNullVerifier checks for this.
		int size = this.sizeGetter.get(context.object);
		if (
			(this.minInclusive ? (size >= this.min) : (size > this.min)) &&
			(this.maxInclusive ? (size <= this.max) : (size < this.max))
		) {
			return;
		}
		else {
			throw new VerifyException(() -> {
				StringBuilder message = context.pathToStringBuilder();
				boolean haveMin = this.min != 0 || !this.minInclusive;
				boolean haveMax = this.max != Integer.MAX_VALUE || !this.maxInclusive;
				message.append(" must have ");
				if (haveMin) {
					message
					.append(this.minInclusive ? "at least " : "more than ")
					.append(this.min)
					.append(' ')
					.append(this.min == 1 ? this.sizeGetter.singleElement : this.sizeGetter.multipleElements);
				}
				if (haveMin && haveMax) {
					message.append(" and ");
				}
				if (haveMax) {
					message
					.append(this.maxInclusive ? "at most " : "less than ")
					.append(this.max)
					.append(' ')
					//I don't know why you'd ever want less than (or equal to) 1 element,
					//but handle this case sanely anyway.
					.append(this.max == 1 ? this.sizeGetter.singleElement : this.sizeGetter.multipleElements);
				}
				return message.toString();
			});
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + (this.minInclusive ? '[' : '(') + this.min + ", " + (this.maxInclusive ? ']' : ')');
	}

	public static abstract class SizeGetter<T> {

		public static final SizeGetter<CharSequence>  STRING     = of("character", "characters", CharSequence::length   );
		public static final SizeGetter<Collection<?>> COLLECTION = of("element",   "elements",     Collection::size     );
		public static final SizeGetter<Map<?, ?>>     MAP        = of("entry",     "entries",             Map::size     );
		public static final SizeGetter<Object>        ARRAY      = of("element",   "elements",          Array::getLength);

		public final @NotNull String singleElement, multipleElements;

		public SizeGetter(@NotNull String singleElement, @NotNull String multipleElements) {
			this.  singleElement  =   singleElement;
			this.multipleElements = multipleElements;
		}

		public static <T> @NotNull SizeGetter<T> of(@NotNull String singleElement, @NotNull String multipleElements, @NotNull ToIntFunction<T> getter) {
			return new Impl<>(singleElement, multipleElements, getter);
		}

		public abstract int get(@NotNull T collection);

		public static class Impl<T> extends SizeGetter<T> {

			public final @NotNull ToIntFunction<T> getter;

			public Impl(@NotNull String singleElement, @NotNull String multipleElements, @NotNull ToIntFunction<T> getter) {
				super(singleElement, multipleElements);
				this.getter = getter;
			}

			@Override
			public int get(@NotNull T collection) {
				return this.getter.applyAsInt(collection);
			}
		}
	}

	public static class Factory extends NamedVerifierFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			VerifySizeRange range = context.type.getAnnotations().getFirst(VerifySizeRange.class);
			if (range == null) return null;

			SizeGetter<?> sizeGetter;
			if (context.type.getClassification() == TypeClassification.ARRAY) {
				sizeGetter = SizeGetter.ARRAY;
			}
			else {
				Class<? super T_HandledType> clazz = context.type.getUpperBoundOrSelf().getRawClass();
				if (clazz == null) return null;

				if (CharSequence.class.isAssignableFrom(clazz)) {
					sizeGetter = SizeGetter.STRING;
				}
				else if (Collection.class.isAssignableFrom(clazz)) {
					sizeGetter = SizeGetter.COLLECTION;
				}
				else if (Map.class.isAssignableFrom(clazz)) {
					sizeGetter = SizeGetter.MAP;
				}
				else {
					return null;
				}
			}

			return new SizeRangeVerifier<>(range, sizeGetter);
		}
	}
}