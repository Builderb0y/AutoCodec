package builderb0y.autocodec.imprinters;

import java.util.stream.Stream;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.MapDecoder;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.AutoHandler;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.KeyHolder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.verifiers.AutoVerifier;

public interface AutoImprinter<T_Decoded> extends AutoHandler, KeyHolder {

	public static final @NotNull ObjectArrayFactory<AutoImprinter<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoImprinter.class).generic();

	/**
	mutates the instance of {@link T_Decoded} stored on {@link ImprintContext#object}
	using the data in {@link ImprintContext#input}.
	throws {@link ImprintException} if that data is malformed.
	note that the imprinter should not make assumptions about
	whether or not the object it just decoded is "valid".
	for example, a {@link ImprintException} should NOT be thrown
	if the decoded object is null, an empty array, or a negative number.
	checks like these are to be performed by the {@link AutoVerifier} instead.
	only one subclass of {@link AutoImprinter} is expected to perform such checks,
	and that's {@link VerifyingImprinter}.

	this method is annotated with {@link OverrideOnly}
	because it performs no logging on its own.
	use {@link DecodeContext#imprintWith(AutoImprinter, Object)}
	to imprint and log what is being imprinted.
	*/
	@OverrideOnly
	public abstract <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Decoded> context) throws ImprintException;

	/**
	if this AutoImprinter imprints from an object with known keys,
	then this method returns those keys.
	if this AutoImprinter imprints from any other type of encoded value,
	or if this AutoImprinter decodes from an object but the keys are not known in advance,
	then this method returns null.

	this method is used in the implementation of {@link MapDecoder}'s,
	which are sometimes desired over regular {@link Decoder}'s.
	*/
	@Override
	public default @Nullable Stream<@NotNull String> getKeys() {
		return null;
	}

	public static abstract class NamedImprinter<T_Decoded> extends NamedHandler<T_Decoded> implements AutoImprinter<T_Decoded> {

		public NamedImprinter(@NotNull ReifiedType<T_Decoded> type) {
			super(type);
		}

		public NamedImprinter(@NotNull String toString) {
			super(toString);
		}
	}

	public static interface ImprinterFactory extends AutoFactory<AutoImprinter<?>> {

		/**
		returns an AutoImprinter which can imprint instances of T_HandledType.
		or null if this factory does not know how to imprint instances of T_HandledType.
		throws {@link FactoryException} if this factory knows how to imprint instances
		of T_HandledType, but some other problem occurs which prevents it from doing so.

		this method used to enforce that it returns AutoImprinter<T_HandledType>,
		but the problem with that is it usually just resulted
		in a lot of unchecked casts for implementors.
		java's generics system just wasn't designed for these kinds of things.

		this method is annotated with {@link OverrideOnly}
		because it performs no logging on its own.
		use {@link FactoryContext#tryCreateImprinter(ImprinterFactory)}
		to create an imprinter using this factory and log it at the same time.
		*/
		@Override
		@OverrideOnly
		public abstract <T_HandledType> @Nullable AutoImprinter<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException;
	}

	public static abstract class NamedImprinterFactory extends NamedFactory<AutoImprinter<?>> implements ImprinterFactory {

		public NamedImprinterFactory() {}

		public NamedImprinterFactory(@NotNull String toString) {
			super(toString);
		}
	}
}