package builderb0y.autocodec.coders;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

/**
analogous to {@link Codec}; this class acts as a convenience
combo of {@link AutoEncoder} and {@link AutoDecoder}.
the simplest implementation is {@link EncoderDecoderCoder},
which simply delegates to an {@link AutoEncoder} and an {@link AutoDecoder}.
*/
public interface AutoCoder<T_Decoded> extends AutoEncoder<T_Decoded>, AutoDecoder<T_Decoded> {

	public static final @NotNull ObjectArrayFactory<AutoCoder<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoCoder.class).generic();

	public static <T_Decoded> @NotNull AutoCoder<T_Decoded> of(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) {
		return new EncoderDecoderCoder<>(encoder, decoder);
	}

	/**
	if this AutoCoder encodes or decodes to or from an object with known keys,
	then this method returns those keys.
	if this AutoCoder encodes or decodes to or from into any other type of encoded value,
	or if this AutoCoder encodes or decodes to or from into an object but
	the keys are not known in advance, then this method returns null.

	this method is used in the implementation of {@link MapCodec}'s,
	which are sometimes desired over regular {@link Codec}'s.
	*/
	@Override
	public default @Nullable Stream<String> getKeys() {
		return null;
	}

	public static abstract class NamedCoder<T_Decoded> extends NamedHandler<T_Decoded> implements AutoCoder<T_Decoded> {

		public NamedCoder(@NotNull ReifiedType<T_Decoded> handledType) {
			super(handledType);
		}

		public NamedCoder(@NotNull String toString) {
			super(toString);
		}
	}

	public static interface CoderFactory extends AutoFactory<AutoCoder<?>> {

		/**
		returns an AutoCoder which can encode and decode instances of T_HandledType,
		or null if this factory does not know how to encode and decode instances of T_HandledType.
		throws {@link FactoryException} if this factory knows how to encode/decode instances
		of T_HandledType, but some other problem occurs which prevents it from doing so.

		this method used to enforce that it returns AutoCoder<T_HandledType>,
		but the problem with that is it usually just resulted
		in a lot of unchecked casts for implementors.
		java's generic system just wasn't designed for these kinds of things.

		this method is annotated with {@link OverrideOnly}
		because it performs no logging on its own.
		use {@link FactoryContext#tryCreateCoder(CoderFactory)}
		to create a coder using this factory and log it at the same time.
		*/
		@Override
		@OverrideOnly
		public abstract <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException;
	}

	public static abstract class NamedCoderFactory extends NamedFactory<AutoCoder<?>> implements CoderFactory {

		public NamedCoderFactory() {}

		public NamedCoderFactory(@NotNull String toString) {
			super(toString);
		}
	}
}