package builderb0y.autocodec.decoders;

import java.util.stream.Stream;

import com.mojang.serialization.*;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.AutoHandler;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.KeyHolder;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.verifiers.AutoVerifier;

/** analogous to {@link Decoder}. */
public interface AutoDecoder<T_Decoded> extends AutoHandler, KeyHolder {

	public static final @NotNull ObjectArrayFactory<AutoDecoder<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoDecoder.class).generic();

	/**
	takes some data stored on {@link DecodeContext#input},
	and decodes it into an instances of {@link T_Decoded}.
	throws {@link DecodeException} if the input data is malformed.
	note that the decoder should not make assumptions about
	whether or not the object it just decoded is "valid".
	for example, a {@link DecodeException} should NOT be thrown
	if the decoded object is null, an empty array, or a negative number.
	checks like these are to be performed by the {@link AutoVerifier} instead.

	this method is annotated with {@link OverrideOnly}
	because it performs no logging on its own.
	use {@link DecodeContext#decodeWith(AutoDecoder)}
	to decode and log what is being decoded.
	*/
	@OverrideOnly
	public abstract <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException;

	/**
	if this AutoDecoder decodes from an object with known keys,
	then this method returns those keys.
	if this AutoDecoder decodes from any other type of encoded value,
	or if this AutoDecoder decodes from an object but the keys are not known in advance,
	then this method returns null.

	this method is used in the implementation of {@link MapDecoder}'s,
	which are sometimes desired over regular {@link Decoder}'s.
	*/
	@Override
	public default @Nullable Stream<@NotNull String> getKeys() {
		return null;
	}

	public static abstract class NamedDecoder<T_Decoded> extends NamedHandler<T_Decoded> implements AutoDecoder<T_Decoded> {

		public NamedDecoder(@NotNull ReifiedType<T_Decoded> type) {
			super(type);
		}

		public NamedDecoder(@NotNull String toString) {
			super(toString);
		}
	}

	public static interface DecoderFactory extends AutoFactory<AutoDecoder<?>> {

		/**
		returns an AutoDecoder which can decode instances of T_HandledType.
		or null if this factory does not know how to decode instances of T_HandledType.
		throws {@link FactoryException} if this factory knows how to decode instances
		of T_HandledType, but some other problem occurs which prevents it from doing so.

		this method used to enforce that it returns AutoDecoder<T_HandledType>,
		but the problem with that is it usually just resulted
		in a lot of unchecked casts for implementors.
		java's generics system just wasn't designed for these kinds of things.

		this method is annotated with {@link OverrideOnly}
		because it performs no logging on its own.
		use {@link FactoryContext#tryCreateDecoder(DecoderFactory)}
		to create a decoder using this factory and log it at the same time.
		*/
		@Override
		@OverrideOnly
		public abstract <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException;
	}

	public static abstract class NamedDecoderFactory extends NamedFactory<AutoDecoder<?>> implements DecoderFactory {

		public NamedDecoderFactory() {}

		public NamedDecoderFactory(@NotNull String toString) {
			super(toString);
		}
	}
}