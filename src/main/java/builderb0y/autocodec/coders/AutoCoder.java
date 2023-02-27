package builderb0y.autocodec.coders;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

/**
analogous to {@link Codec}; this class acts as a convenience
combo of {@link AutoEncoder} and {@link AutoDecoder}.
the simplest implementation is {@link Coder},
which simply delegates to an {@link AutoEncoder} and an {@link AutoDecoder}.
*/
public interface AutoCoder<T_Decoded> extends AutoEncoder<T_Decoded>, AutoDecoder<T_Decoded> {

	public static final @NotNull ObjectArrayFactory<AutoCoder<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoCoder.class).generic();

	public static <T_Decoded> @NotNull AutoCoder<T_Decoded> of(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) {
		return new Coder<>(encoder, decoder);
	}

	/**
	adapts this AutoCoder to work on a different type. specifically type T_To.
	the encodeMapper's purpose is to convert instances of T_To into T_Decoded
	so that this AutoCoder can handle them. the decodeMapper does the opposite,
	it takes the output of this AutoCoder when decoding (the output being an
	instance of T_Decoded), and converts it to an instance of T_To to be returned
	as the decoding result.
	at the time of writing this, the newType parameter is unused by the default implementation.
	it is provided for subclasses to use if they choose to override this method.
	*/
	public default <T_To> @NotNull AutoCoder<T_To> mapCoder(
		@NotNull ReifiedType<T_To> newType,
		@NotNull HandlerMapper<@Nullable T_To, @Nullable T_Decoded> encodeMapper,
		@NotNull HandlerMapper<@Nullable T_Decoded, @Nullable T_To> decodeMapper
	) {
		return new AutoCoder<>() {

			@Override
			public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_To> context) throws EncodeException {
				try {
					return context.input(encodeMapper.apply(context.input)).encodeWith(AutoCoder.this);
				}
				catch (EncodeException | Error exception) {
					throw exception;
				}
				catch (Throwable throwable) {
					throw new EncodeException(throwable);
				}
			}

			@Override
			public <T_Encoded> @Nullable T_To decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
				try {
					return decodeMapper.apply(context.decodeWith(AutoCoder.this));
				}
				catch (DecodeException | Error exception) {
					throw exception;
				}
				catch (Throwable throwable) {
					throw new DecodeException(throwable);
				}
			}

			@Override
			public String toString() {
				return AutoCoder.this + " <-> (" + encodeMapper + ", " + decodeMapper + ')';
			}
		};
	}

	/**
	same as {@link #mapCoder(ReifiedType, HandlerMapper, HandlerMapper)},
	but allows you to provide names for the mapper functions.
	this can be particularly useful for logging when the mapper
	functions are lambda expressions, since lambda expressions
	don't override {@link Object#toString()}. the provided names
	are only used by the returned AutoCoder's toString() method,
	and has no effect on how the returned AutoCoder codes things.
	at the time of writing this, the newType parameter is unused by the default implementation.
	it is provided for subclasses to use if they choose to override this method.
	*/
	public default <T_To> @NotNull AutoCoder<T_To> mapCoder(
		@NotNull ReifiedType<T_To> newType,
		@NotNull String encodeMapperName,
		@NotNull HandlerMapper<@Nullable T_To, @Nullable T_Decoded> encodeMapper,
		@NotNull String decodeMapperName,
		@NotNull HandlerMapper<@Nullable T_Decoded, @Nullable T_To> decodeMapper
	) {
		return new AutoCoder<>() {

			@Override
			public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_To> context) throws EncodeException {
				try {
					return context.input(encodeMapper.apply(context.input)).encodeWith(AutoCoder.this);
				}
				catch (EncodeException | Error exception) {
					throw exception;
				}
				catch (Throwable throwable) {
					throw new EncodeException(throwable);
				}
			}

			@Override
			public <T_Encoded> @Nullable T_To decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
				try {
					return decodeMapper.apply(context.decodeWith(AutoCoder.this));
				}
				catch (DecodeException | Error exception) {
					throw exception;
				}
				catch (Throwable throwable) {
					throw new DecodeException(throwable);
				}
			}

			@Override
			public String toString() {
				return AutoCoder.this + " <-> (" + encodeMapperName + ", " + decodeMapperName + ')';
			}
		};
	}

	public static abstract class NamedCoder<T_Decoded> extends NamedHandler<T_Decoded> implements AutoCoder<T_Decoded> {

		public NamedCoder(@NotNull ReifiedType<T_Decoded> handledType) {
			super(handledType);
		}

		public NamedCoder(@NotNull String toString) {
			super(toString);
		}
	}
}