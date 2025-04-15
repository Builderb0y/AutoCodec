package builderb0y.autocodec.decoders;

import java.util.function.Supplier;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.AbstractDecodeContext;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class DecodeContext<T_Encoded>
extends AbstractDecodeContext<
	T_Encoded,
	DecodeException,
	DecodeContext<T_Encoded>
> {

	public static final @NotNull ObjectArrayFactory<DecodeContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(DecodeContext.class).generic();

	public DecodeContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data data,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, parent, path, data, ops);
	}

	public DecodeContext(@NotNull AbstractDecodeContext<T_Encoded, ?, ?> context) {
		this(context.autoCodec, context.parent, context.path, context.data, context.ops);
	}

	@Override
	public @NotNull DecodeContext<T_Encoded> newContext(
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data input
	) {
		return new DecodeContext<>(this.autoCodec, parent, path, input, this.ops);
	}

	@Override
	public @NotNull DecodeException newException(@NotNull Supplier<@NotNull String> messageSupplier) {
		return new DecodeException(messageSupplier);
	}

	@Override
	public <T_Decoded> T_Decoded decodeWith(@NotNull AutoDecoder<T_Decoded> decoder) throws DecodeException {
		return this.logger().decode(decoder, this);
	}

	//////////////////////////////// paths ////////////////////////////////

	public static interface DecodePath {

		public abstract void appendTo(@NotNull StringBuilder builder);
	}

	public static enum RootDecodePath implements DecodePath {
		INSTANCE;

		@Override
		public void appendTo(@NotNull StringBuilder builder) {
			builder.append("<root>");
		}

		@Override
		public String toString() {
			return "<root>";
		}
	}

	public static record ObjectDecodePath(@NotNull String memberName) implements DecodePath {

		@Override
		public void appendTo(@NotNull StringBuilder builder) {
			if (!builder.isEmpty()) builder.append('.');
			builder.append(this.memberName);
		}

		@Override
		public String toString() {
			return this.memberName;
		}
	}

	public static record ArrayDecodePath(int index) implements DecodePath {

		@Override
		public void appendTo(@NotNull StringBuilder builder) {
			builder.append('[').append(this.index).append(']');
		}

		@Override
		public String toString() {
			return Integer.toString(this.index);
		}
	}
}