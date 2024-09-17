package builderb0y.autocodec.integration;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.util.ObjectOps;

public record DFU2AutoCoder<T_Decoded>(

	Codec<T_Decoded> codec,

	/**
	if true, partial results will be promoted to full results;
	the message of partial results will be logged.

	if false, partial results will throw an {@link EncodeException}
	or {@link DecodeException}.

	the default value of this component is false.
	*/
	boolean allowPartial,

	/**
	if true, it is assumed that {@link #codec}
	can handle null and empty inputs.
	such inputs will be passed to the codec.

	if false, it is assumed that {@link #codec}
	only accepts non-null and non-empty inputs,
	and will return an error data result
	when a null or empty input is encountered.
	such inputs will NOT be passed to the coder.

	the default value of this component is false.
	*/
	boolean nullSafe
)
implements AutoCoder<T_Decoded> {

	/**
	the components of this record are subject to change.
	use {@link #DFU2AutoCoder(Codec)} instead,
	then call the extra methods like {@link #nullSafe(boolean)}
	to tweak individual components.
	*/
	@Deprecated
	@SuppressWarnings("DeprecatedIsStillUsed")
	public DFU2AutoCoder {}

	@SuppressWarnings("deprecation")
	public DFU2AutoCoder(Codec<T_Decoded> codec) {
		this(codec, false, false);
	}

	@SuppressWarnings("deprecation")
	public DFU2AutoCoder<T_Decoded> allowPartial(boolean allowPartial) {
		return new DFU2AutoCoder<>(this.codec, allowPartial, this.nullSafe);
	}

	@SuppressWarnings("deprecation")
	public DFU2AutoCoder<T_Decoded> nullSafe(boolean nullSafe) {
		return new DFU2AutoCoder<>(this.codec, this.allowPartial, nullSafe);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty() && !this.nullSafe) return null;
		return context.logger().unwrapLazy(
			this.codec.parse(context.ops, context.input),
			this.allowPartial,
			DecodeException::new
		);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		if (context.object == null && !this.nullSafe) return context.empty();
		return context.logger().unwrapLazy(
			this.codec.encodeStart(context.ops, context.object),
			this.allowPartial,
			EncodeException::new
		);
	}

	@Override
	public @Nullable Stream<@NotNull String> getKeys() {
		return this.codec instanceof MapCodec.MapCodecCodec<T_Decoded> codec ? codec.codec().keys(ObjectOps.INSTANCE).map((Object object) -> {
			if (object instanceof String string) return string;
			else throw new IllegalArgumentException(codec + " has a key that is not a String: " + object);
		}) : null;
	}

	@Override
	public boolean hasKeys() {
		return this.codec instanceof MapCodec.MapCodecCodec<T_Decoded>;
	}
}