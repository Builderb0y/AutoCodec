package builderb0y.autocodec.decoders;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.AbstractDecodeContext;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.ListData;
import builderb0y.autocodec.data.MapData;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class DecodeContext<T_Encoded> extends AbstractDecodeContext<T_Encoded, DecodeException> {

	public static final @NotNull ObjectArrayFactory<DecodeContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(DecodeContext.class).generic();

	public DecodeContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data<T_Encoded> input,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, parent, path, input, ops);
	}

	public DecodeContext(@NotNull AbstractDecodeContext<T_Encoded, ?> context) {
		this(context.autoCodec, context.parent, context.path, context.input, context.ops);
	}

	public @NotNull DecodeContext<T_Encoded> input(@NotNull Data<T_Encoded> input) {
		return this.input == input ? this : new DecodeContext<>(this.autoCodec, this.parent, this.path, input, this.ops);
	}

	public @NotNull DecodeContext<T_Encoded> input(@NotNull Data<T_Encoded> input, @NotNull DecodePath nextPath) {
		return new DecodeContext<>(this.autoCodec, this, nextPath, input, this.ops);
	}

	public @NotNull DecodeContext<T_Encoded> input(@NotNull String memberName, @NotNull Data<T_Encoded> member) {
		return this.input(member, new ObjectDecodePath(memberName));
	}

	public @NotNull DecodeContext<T_Encoded> input(int index, @NotNull Data<T_Encoded> element) {
		return this.input(element, new ArrayDecodePath(index));
	}

	@Override
	public @NotNull DecodeException notA(@NotNull String type) {
		return new DecodeException(() -> this.pathToStringBuilder().append(" is not a ").append(type).append(": ").append(this.input).toString());
	}

	@Override
	public @NotNull DecodeContext<T_Encoded> getElement(int index) {
		ListData<T_Encoded> list = this.tryAsList();
		if (list != null) {
			return this.input(index, list.value.get(index));
		}
		return this.input(index, this.empty());
	}

	@Override
	public @NotNull DecodeContext<T_Encoded> getMember(@NotNull String key) {
		MapData<T_Encoded> map = this.tryAsMap();
		if (map != null) {
			Data<T_Encoded> value = map.value.get(this.createString(key));
			if (value != null) {
				return this.input(key, value);
			}
		}
		return this.input(key, this.empty());
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