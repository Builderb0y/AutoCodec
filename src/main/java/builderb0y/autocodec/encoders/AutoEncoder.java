package builderb0y.autocodec.encoders;

import java.util.stream.Stream;

import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapEncoder;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.AutoHandler;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.KeyHolder;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

/** analogous to {@link Encoder}. */
public interface AutoEncoder<T_Decoded> extends AutoHandler, KeyHolder {

	public static final @NotNull ObjectArrayFactory<AutoEncoder<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoEncoder.class).generic();

	/**
	takes an instance of {@link T_Decoded} stored on {@link EncodeContext#object},
	and encodes it into data. throws {@link EncodeException} if some
	abnormal conditions prevent the object from being serialized.

	there is no verification when encoding objects,
	as we assume that the object being encoded came from a trusted source.
	if it was decoded from data, it would've been verified on decode.

	this method is annotated with {@link OverrideOnly}
	because it performs no logging on its own.
	use {@link EncodeContext#encodeWith(AutoEncoder)}
	to encode and log what is being encoded.
	*/
	@OverrideOnly
	public abstract <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException;

	/**
	if this AutoEncoder encodes into an object with known keys,
	then this method returns those keys.
	if this AutoEncoder encodes into any other type of encoded value,
	or if this AutoEncoder encodes into an object but the keys are not known in advance,
	then this method returns null.

	this method is used in the implementation of {@link MapEncoder}'s,
	which are sometimes desired over regular {@link Encoder}'s.
	*/
	@Override
	public default @Nullable Stream<String> getKeys() {
		return null;
	}

	public static abstract class NamedEncoder<T_Decoded> extends NamedHandler<T_Decoded> implements AutoEncoder<T_Decoded> {

		public NamedEncoder(@NotNull ReifiedType<T_Decoded> type) {
			super(type);
		}

		public NamedEncoder(@NotNull String toString) {
			super(toString);
		}
	}

	public static interface EncoderFactory extends AutoFactory<AutoEncoder<?>> {

		/**
		returns an AutoEncoder which can encode instances of T_HandledType,
		or null if this factory does not know how to encode instances of T_HandledType.
		throws {@link FactoryException} if this factory knows how to encode instances
		of T_HandledType, but some other problem occurs which prevents it from doing so.

		this method used to enforce that it returns AutoEncoder<T_HandledType>,
		but the problem with that is it usually just resulted
		in a lot of unchecked casts for implementors.
		java's generics system just wasn't designed for these kinds of things.

		this method is annotated with {@link OverrideOnly}
		because it performs no logging on its own.
		use {@link FactoryContext#tryCreateEncoder(EncoderFactory)}
		to create an encoder using this factory and log it at the same time.
		*/
		@Override
		@OverrideOnly
		public abstract <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException;
	}

	public static abstract class NamedEncoderFactory extends NamedFactory<AutoEncoder<?>> implements EncoderFactory {

		public NamedEncoderFactory() {}

		public NamedEncoderFactory(@NotNull String toString) {
			super(toString);
		}
	}
}